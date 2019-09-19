package org.mozilla.msrp.platform.mission.qualifier

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Basic fields for each progress record
 */
interface MissionProgressDoc {
    var uid: String
    var mid: String
    var joinDate: Long
    var timestamp: Long
    var missionType: String
    var progressType: ProgressType

    fun toProgressResponse(): Map<String, Any> {
        return mutableMapOf<String, Any>(
                "joinDate" to joinDate
        ).apply {
            putAll(getProgressFields())
        }
    }

    @JsonIgnore
    fun getProgressFields(): Map<String, Any>
}

enum class ProgressType(@JsonValue val status: Int) {
    /** Record with action=update means this record is an update to the previous one */
    Update(0),
    /** Record with action=clear means user had quited the mission, and all records before this one
     * becomes invalid. */
    Clear(1),
}