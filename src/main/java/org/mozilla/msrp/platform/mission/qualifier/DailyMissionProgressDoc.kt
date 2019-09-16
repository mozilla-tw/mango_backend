package org.mozilla.msrp.platform.mission.qualifier

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
        var currentDayCount: Int = 0
): MissionProgressDoc {

    override fun getProgressFields(): Map<String, Any> {
        return mapOf(
                "currentDayCount" to currentDayCount
        )
    }
}
