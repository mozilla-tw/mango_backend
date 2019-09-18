package org.mozilla.msrp.platform.mission.qualifier

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Progress fields for daily mission
 */
data class DailyMissionProgressDoc(
        override var uid: String = "",
        override var mid: String = "",
        override var joinDate: Long = 0L,
        override var timestamp: Long = 0L,
        override var missionType: String = "",
        override var progressType: ProgressType = ProgressType.Update,
        var currentDayCount: Int = 0,
        var totalDays: Int = 0,
        @JsonIgnore var dailyMessage: String = ""
): MissionProgressDoc {

    override fun getProgressFields(): Map<String, Any> {
        return mapOf(
                "currentDayCount" to currentDayCount,
                "message" to dailyMessage,
                "totalDays" to totalDays
        )
    }
}
