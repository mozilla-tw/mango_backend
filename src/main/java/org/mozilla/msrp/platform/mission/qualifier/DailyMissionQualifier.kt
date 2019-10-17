package org.mozilla.msrp.platform.mission.qualifier

import org.mozilla.msrp.platform.common.isProd
import org.mozilla.msrp.platform.mission.JoinStatus
import org.mozilla.msrp.platform.mission.MissionRepository
import org.mozilla.msrp.platform.mission.MissionType
import org.mozilla.msrp.platform.util.getDayDifference
import org.mozilla.msrp.platform.util.getMinuteOfDayDifference
import org.mozilla.msrp.platform.util.logger
import org.springframework.context.MessageSource
import org.springframework.core.env.Environment
import java.time.*
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@Named
class DailyMissionQualifier(private val clock: Clock = Clock.systemUTC()) {

    private val log = logger()

    @Inject
    lateinit var missionRepository: MissionRepository

    @Inject
    lateinit var missionMessageSource: MessageSource

    @Inject
    lateinit var environment: Environment

    fun updateProgress(uid: String, mid: String, zone: ZoneId, locale: Locale): MissionProgressDoc {
        val params = missionRepository.getDailyMissionParams(mid)

        // DocumentSnapshot#toObject() will map numbers to Long if the target field is declared as Any
        // Since we declare params as Map<String, Any>, we need to convert to Long here
        val totalDays = (params["totalDays"] as Long).toInt()

        log.info("params=$params")

        val latestRecord = missionRepository.getDailyMissionProgress(uid, mid)

        val newProgress = latestRecord?.let {
            checkIn(it, zone)
        } ?: createNewRecord(uid, mid, totalDays)

        if (newProgress != latestRecord) {
            log.info("insert new progress $newProgress, totalDays=$totalDays")

            val messages = params["message"] as? ArrayList<*>
            val messageId = messages?.getOrNull(newProgress.currentDayCount - 1)
            val message = messageId?.let {
                missionMessageSource.getMessage(it.toString(), null, locale)
            }
            newProgress.dailyMessage = message ?: ""
            newProgress.totalDays = totalDays

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
            mid: String,
            totalDays: Int
    ): DailyMissionProgressDoc {
        val now = clock.instant().toEpochMilli()
        return DailyMissionProgressDoc(
                uid = uid,
                mid = mid,
                joinDate = now,
                timestamp = now,
                missionType = MissionType.DailyMission.identifier,
                currentDayCount = 0,
                totalDays = totalDays
        )
    }

    private fun checkIn(
            progress: DailyMissionProgressDoc,
            zone: ZoneId
    ): DailyMissionProgressDoc {
        if (progress.currentDayCount >= progress.totalDays) {
            return noProgress(progress)
        }

        if (progress.currentDayCount == 0) {
            return advanceProgress(progress)
        }

        val lastCheckInDate = Instant.ofEpochMilli(progress.timestamp).atZone(zone)
        val now = clock.instant().atZone(zone)

        return when (actionResolver().resolve(lastCheckInDate, now)) {
            Action.Restart -> restartProgress(progress)
            Action.Advance -> advanceProgress(progress)
            Action.NoAction -> noProgress(progress)
            Action.Illegal -> illegalProgress(progress)
        }
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

    private fun actionResolver(): ProgressActionResolver {
        return if (environment.isProd) {
            ProductionResolver()
        } else {
            NightlyResolver()
        }
    }

    interface ProgressActionResolver {
        fun resolve(start: ZonedDateTime, end: ZonedDateTime): Action
    }

    enum class Action {
        Advance,
        Restart,
        NoAction,
        Illegal
    }

    class ProductionResolver : ProgressActionResolver {
        override fun resolve(start: ZonedDateTime, end: ZonedDateTime): Action {
            val diffDays = getDayDifference(start, end)
            return when {
                diffDays >= 2 -> Action.Restart
                diffDays >= 1 -> Action.Advance
                diffDays >= 0 -> Action.NoAction
                else -> Action.Illegal
            }
        }
    }

    class NightlyResolver : ProgressActionResolver {
        override fun resolve(start: ZonedDateTime, end: ZonedDateTime): Action {
            val diffMinutes = getMinuteOfDayDifference(start, end)
            return when {
                diffMinutes >= 2 -> Action.Restart
                diffMinutes >= 1 -> Action.Advance
                diffMinutes >= 0 -> Action.NoAction
                else -> Action.Illegal
            }
        }
    }
}
