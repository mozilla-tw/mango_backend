package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
import org.mozilla.msrp.platform.firestore.areFieldsPresent
import java.util.Optional

/**
 * (All fields are just draft and are subject to change)
 * Mission retrieved from persistent layer
 *
 * To support SnapshotDocument#toObject(), we must have a
 * no-arg constructor. Constructors with all parameters having
 * default value can achieve the same effect.
 */
data class MissionDoc(
        var mid: String = "",
        var missionName: String = "",
        var titleId: String = "",
        var descriptionId: String = "",
        var missionType: String = ""
) {
    val endpoint = "/$missionType/$mid"

    companion object {
        private const val KEY_MID = "mid"
        private const val KEY_NAME_ID = "titleId"
        private const val KEY_DESCRIPTION_ID = "descriptionId"
        private const val KEY_MISSION_TYPE = "missionType"

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): Optional<MissionDoc> {
            return if (isValidSnapshot(snapshot)) {
                Optional.ofNullable(snapshot.toObject(MissionDoc::class.java))
            } else {
                Optional.empty()
            }
        }

        private fun isValidSnapshot(snapshot: DocumentSnapshot): Boolean {
            return snapshot.areFieldsPresent(listOf(
                    KEY_MID,
                    KEY_NAME_ID,
                    KEY_DESCRIPTION_ID,
                    KEY_MISSION_TYPE
            ))
        }
    }
}
