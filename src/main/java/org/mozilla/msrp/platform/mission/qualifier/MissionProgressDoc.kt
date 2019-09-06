package org.mozilla.msrp.platform.mission.qualifier

/**
 * Basic fields for each progress record
 */
interface MissionProgressDoc {
    var uid: String
    var mid: String
    var joinDate: Long
    var timestamp: Long
    var missionType: String

    fun toResponseFields(): Map<String, Any> {
        return mutableMapOf(
                "mid" to mid,
                "joinDate" to joinDate,
                "missionType" to missionType,
                "progress" to getProgressFields()
        )
    }

    fun getProgressFields(): Map<String, Any>
}
