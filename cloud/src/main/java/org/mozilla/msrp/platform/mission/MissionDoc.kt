package org.mozilla.msrp.platform.mission

/**
 * (All fields are just draft and are subject to change)
 * Mission retrieved from persistent layer
 */
data class MissionDoc(
        val mid: String,
        val nameId: String,
        val descriptionId: String
) {
    companion object {
        const val KEY_MID = "mid"
        const val KEY_NAME_ID = "nameId"
        const val KEY_DESCRIPTION_ID = "descriptionId"
    }
}
