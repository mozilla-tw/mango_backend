package org.mozilla.msrp.platform.mission

enum class MissionType(val identifier: String = "") {
    DailyMission("mission_daily"),
    Unknown();

    companion object {
        fun from(missionType: String): MissionType {
            return MissionType.values().find { it.identifier == missionType } ?: Unknown
        }
    }
}
