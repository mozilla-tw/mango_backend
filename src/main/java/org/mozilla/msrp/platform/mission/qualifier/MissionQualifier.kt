package org.mozilla.msrp.platform.mission.qualifier

import org.mozilla.msrp.platform.mission.MissionType
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@Named
class MissionQualifier {

    private val log: Logger = logger()

    @Inject
    lateinit var dailyMissionQualifier: DailyMissionQualifier

    fun updateProgress(
            uid: String,
            mid: String,
            missionType: MissionType,
            zone: ZoneId,
            locale: Locale
    ): MissionProgressDoc? {
        return when (missionType) {
            MissionType.DailyMission -> updateDailyMissionProgress(uid, mid, zone, locale)
            else -> handleUnknownMissionType(mid, missionType)
        }
    }

    fun clearProgress(uid: String, mid: String, missionType: MissionType) {
        when (missionType) {
            MissionType.DailyMission -> clearDailyMissionProgress(uid, mid)
            else -> handleUnknownMissionType(mid, missionType)
        }
    }

    private fun updateDailyMissionProgress(uid: String, mid: String, zone: ZoneId, locale: Locale): MissionProgressDoc? {
        return dailyMissionQualifier.updateProgress(uid, mid, zone, locale)
    }

    private fun clearDailyMissionProgress(uid: String, mid: String) {
        dailyMissionQualifier.clearProgress(uid, mid)
    }

    private fun handleUnknownMissionType(
            mid: String,
            missionType: MissionType
    ): MissionProgressDoc? {
        log.error("no qualifier found for mission mid=$mid, type=$missionType")
        return null
    }

    fun getProgress(uid: String, mid: String, missionType: MissionType): MissionProgressDoc? {
        return when (missionType) {
            MissionType.DailyMission -> getDailyMissionProgress(uid, mid)
            else -> handleUnknownMissionType(mid, missionType)
        }
    }

    private fun getDailyMissionProgress(uid: String, mid: String): MissionProgressDoc? {
        return dailyMissionQualifier.getProgress(uid, mid)
    }
}
