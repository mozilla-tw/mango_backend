package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.annotation.IgnoreExtraProperties
import org.mozilla.msrp.platform.firestore.areFieldsPresent
import org.mozilla.msrp.platform.util.logger

/**
 * (All fields are just draft and are subject to change)
 * Mission retrieved from persistent layer
 *
 * To support SnapshotDocument#toObject(), we must have a
 * no-arg constructor. Constructors with all parameters having
 * default value can achieve the same effect.
 */
@IgnoreExtraProperties
data class MissionDoc(
        var mid: String = "",
        var missionName: String = "",
        var titleId: String = "",
        var descriptionId: String = "",
        var missionType: String = "",
        var interestPings: List<String> = emptyList(),
        val startDate: String = "",
        val joinStartDate: String = "",
        val joinEndDate: String = "",
        var expiredDate: String = "",
        var minVersion: Int = 0,
        val missionParams: Map<String, Any> = emptyMap()
) {
    val endpoint = "/$missionType/$mid"

    companion object {
        private const val KEY_MID = "mid"
        private const val KEY_NAME_ID = "titleId"
        private const val KEY_DESCRIPTION_ID = "descriptionId"
        private const val KEY_MISSION_TYPE = "missionType"
        private const val KEY_PINGS = "interestPings"
        private const val KEY_EXPIRED_DATE = "expiredDate"
        private const val KEY_MISSION_PARAMS = "missionParams"
        private const val KEY_START_DATE = "startDate"
        private const val KEY_JOIN_START_DATE = "joinStartDate"
        private const val KEY_JOIN_END_DATE = "joinEndDate"

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): MissionDoc? {
            return if (isValidSnapshot(snapshot)) {
                snapshot.toObject(MissionDoc::class.java)
            } else {
                logger().info("convert to mission doc failed")
                null
            }
        }

        private fun isValidSnapshot(snapshot: DocumentSnapshot): Boolean {
            return snapshot.areFieldsPresent(listOf(
                    KEY_MID,
                    KEY_NAME_ID,
                    KEY_DESCRIPTION_ID,
                    KEY_MISSION_TYPE,
                    KEY_PINGS,
                    KEY_EXPIRED_DATE,
                    KEY_MISSION_PARAMS,
                    KEY_START_DATE,
                    KEY_JOIN_END_DATE,
                    KEY_JOIN_START_DATE
            ))
        }
    }
}

val MissionDoc.missionTypeEnum: MissionType
    get() = MissionType.from(missionType)
