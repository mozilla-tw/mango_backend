package org.mozilla.msrp.platform.mission.qualifier

import org.mozilla.msrp.platform.mission.JoinStatus
import org.mozilla.msrp.platform.mission.MissionRepository
import org.mozilla.msrp.platform.mission.MissionType
import org.mozilla.msrp.platform.util.logger
import org.springframework.context.MessageSource
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@Named
class DailyMissionQualifier(val clock: Clock = Clock.systemUTC()) {

    private val log = logger()

    @Inject
    lateinit var missionRepository: MissionRepository

    @Inject
    lateinit var missionMessageSource: MessageSource

    @Inject
    lateinit var locale: Locale

    fun updateProgress(uid: String, mid: String, zone: ZoneId): MissionProgressDoc {
        val params = missionRepository.getDailyMissionParams(mid)

        // DocumentSnapshot#toObject() will map numbers to Long if the target field is declared as Any
        // Since we declare params as Map<String, Any>, we need to convert to Long here
        val totalDays = (params["totalDays"] as Long).toInt()

        log.info("params=$params")

        val latestRecord = missionRepository.getDailyMissionProgress(uid, mid)

        val newProgress = latestRecord?.let {
            checkIn(it, zone)
        } ?: createNewRecord(uid, mid)

        if (newProgress != latestRecord) {
            log.info("insert new progress $newProgress, totalDays=$totalDays")

            val messages = params["message"] as? ArrayList<*>
            val messageId = messages?.getOrNull(newProgress.currentDayCount - 1)
            val message = messageId?.let {
                missionMessageSource.getMessage(it.toString(), null, locale)
            }
            newProgress.dailyMessage = message ?: ""

            missionRepository.updateDailyMissionProgress(newProgress)
        }

        log.info("progress: current=${newProgress.currentDayCount}, total=$totalDays")
        if (newProgress.currentDayCount == totalDays) {

            missionRepository.setJoinStatus(
                    JoinStatus.Complete,
                    uid,
                    MissionType.DailyMission.identifier,
                    mid
            )
            log.info("mission complete!!")
        }

        return newProgress
    }

    private fun createNewRecord(
            uid: String,
            mid: String
    ): DailyMissionProgressDoc {
        val now = clock.instant().toEpochMilli()
        return DailyMissionProgressDoc(
                uid = uid,
                mid = mid,
                joinDate = now,
                timestamp = now,
                missionType = MissionType.DailyMission.identifier,
                currentDayCount = 1
        )
    }

    private fun checkIn(
            progress: DailyMissionProgressDoc,
            zone: ZoneId
    ): DailyMissionProgressDoc {
        val lastCheckInDate = Instant.ofEpochMilli(progress.timestamp).atZone(zone)
        val now = clock.instant().atZone(zone)

        val diffDays = getDifferenceInDays(lastCheckInDate, now)

        log.info("now: $now, last: $lastCheckInDate, diff=$diffDays")

        return when {
            diffDays >= 2L -> restartProgress(progress)
            diffDays >= 1L -> advanceProgress(progress)
            diffDays >= 0L -> noProgress(progress)
            else -> illegalProgress(progress)
        }
    }

    private fun getDifferenceInDays(dateStart: ZonedDateTime, dateEnd: ZonedDateTime): Long {
        return getDateDifference(dateStart, dateEnd, ChronoUnit.DAYS)
    }

    private fun getDateDifference(
            dateStart: ZonedDateTime,
            dateEnd: ZonedDateTime,
            @Suppress("SameParameterValue") unit: ChronoUnit
    ): Long {
        return unit.between(dateStart, dateEnd)
    }

    private fun restartProgress(progress: DailyMissionProgressDoc): DailyMissionProgressDoc {
        log.info("restart progress")
        val now = Instant.now().toEpochMilli()
        return progress.copy(joinDate = now, timestamp = now, currentDayCount = 1)
    }

    private fun advanceProgress(progress: DailyMissionProgressDoc): DailyMissionProgressDoc {
        log.info("advance progress")
        val now = clock.instant().toEpochMilli()
        val newDayCount = progress.currentDayCount + 1
        return progress.copy(timestamp = now, currentDayCount = newDayCount)
    }

    private fun noProgress(progress: DailyMissionProgressDoc): DailyMissionProgressDoc {
        log.info("no progress")
        return progress
    }

    private fun illegalProgress(progress: DailyMissionProgressDoc): DailyMissionProgressDoc {
        log.info("illegal progress")
        return progress
    }

    fun getProgress(uid: String, mid: String): MissionProgressDoc? {
        return missionRepository.getDailyMissionProgress(uid, mid)
    }

    fun clearProgress(uid: String, mid: String) {
        missionRepository.clearDailyMissionProgress(uid, mid)
    }
}
