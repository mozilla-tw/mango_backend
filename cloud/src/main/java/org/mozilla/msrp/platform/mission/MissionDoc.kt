package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
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
        var nameId: String = "",
        var descriptionId: String = ""
) {
    companion object {
        private const val KEY_MID = "mid"
        private const val KEY_NAME_ID = "nameId"
        private const val KEY_DESCRIPTION_ID = "descriptionId"

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): Optional<MissionDoc> {
            return if (snapshot.areFieldsPresent(listOf(KEY_MID, KEY_NAME_ID, KEY_DESCRIPTION_ID))) {
                Optional.ofNullable(snapshot.toObject(MissionDoc::class.java))
            } else {
                Optional.empty()
            }
        }
    }
}
