package org.mozilla.msrp.platform.mission.qualifier

import org.mozilla.msrp.platform.mission.MissionRepository
import org.mozilla.msrp.platform.mission.MissionType
import org.mozilla.msrp.platform.util.logger
import java.time.*
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Named


@Named
class DailyMissionQualifier(val clock: Clock = Clock.systemUTC()) {

    private val log = logger()

    @Inject
    lateinit var missionRepository: MissionRepository

    fun updateProgress(uid: String, mid: String, zone: ZoneId): MissionProgressDoc {
        val latestRecord = missionRepository.getDailyMissionProgress(uid, mid)

        val newProgress = latestRecord?.let {
            checkIn(it, zone)
        } ?: createNewRecord(uid, mid)

        if (newProgress != latestRecord) {
            log.info("insert new progress $newProgress")
            missionRepository.updateDailyMissionProgress(newProgress)
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
                joinData = now,
                timestamp = now,
                missionType = MissionType.DailyMission.identifier,
                currentDayCount = 1
        )
    }

    private fun checkIn(
            progress: DailyMissionProgressDoc,
            zone: ZoneId
    ): MissionProgressDoc {
        val lastCheckInDate = Instant.ofEpochMilli(progress.timestamp).atZone(zone)
        val now = clock.instant().atZone(zone)

        val diffDays = getDifferenceInDays(lastCheckInDate, now)

        log.info("now: $now, last: $lastCheckInDate, diff=$diffDays")

        return when {
            diffDays >= 20L -> restartProgress(progress)
            diffDays >= 10L -> advanceProgress(progress)
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
        return progress.copy(joinData = now, timestamp = now, currentDayCount = 1)
    }

    private fun advanceProgress(progress: DailyMissionProgressDoc): DailyMissionProgressDoc {
        log.info("advance progress")
        val now = Instant.now().toEpochMilli()
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
}
