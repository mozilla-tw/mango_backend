package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.annotation.IgnoreExtraProperties
import org.mozilla.msrp.platform.firestore.checkAbsentFields
import org.mozilla.msrp.platform.firestore.stringToLocalDateTime
import org.mozilla.msrp.platform.util.logger
import java.time.*

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
        val missionParams: Map<String, Any> = emptyMap(),
        var rewardType: String = "", // the Firestore collection for reward inventory
        val joinQuota: Int = -1
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
        private const val KEY_REWARD_TYPE = "rewardType"

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): MissionDoc? {
            val lostFields = snapshot.checkAbsentFields(getEssentialFields())
            return if (lostFields.isEmpty()) {
                snapshot.toObject(MissionDoc::class.java)
            } else {
                logger().info("convert to mission doc failed, path=${snapshot.reference.path}, absent=$lostFields")
                null
            }
        }

        private fun getEssentialFields(): List<String> {
            return listOf(
                    KEY_MID,
                    KEY_NAME_ID,
                    KEY_DESCRIPTION_ID,
                    KEY_MISSION_TYPE,
                    KEY_PINGS,
                    KEY_EXPIRED_DATE,
                    KEY_MISSION_PARAMS,
                    KEY_START_DATE,
                    KEY_JOIN_END_DATE,
                    KEY_JOIN_START_DATE,
                    KEY_REWARD_TYPE
            )
        }
    }
}

val MissionDoc.missionTypeEnum: MissionType
    get() = MissionType.from(missionType)

fun MissionDoc.isExpired(clock: Clock, zone: ZoneId): Boolean {
    val expiredDate = stringToLocalDateTime(expiredDate)

    // Current we only support missions that start/expired at a specific utc time.
    val isUtcBasedMission = true

    return if (isUtcBasedMission) {
        val currentServer = clock.instant()
        val expiredUtc = expiredDate.toInstant(ZoneOffset.UTC)
        val isExpired = currentServer == expiredUtc || currentServer.isAfter(expiredUtc)
        logger().info("isExpired=$isExpired, expiredDate=${expiredDate.toInstant(ZoneOffset.UTC)}, currentServerUtc=$currentServer")
        isExpired

    } else {
        val currentClient = LocalDateTime.ofInstant(Instant.now(clock), zone)
        currentClient == expiredDate || currentClient.isAfter(expiredDate)
    }
}

fun MissionDoc.isBeforeJoinPeriod(clock: Clock, zone: ZoneId): Boolean {
    val startDate = stringToLocalDateTime(joinStartDate)

    // Current we only support missions that start/expired at a specific utc time.
    val isUtcBasedMission = true

    val isBeforeJoinStart: Boolean
    if (isUtcBasedMission) {
        val currentServer = clock.instant()
        val startUtc = startDate.toInstant(ZoneOffset.UTC)
        isBeforeJoinStart = currentServer.isBefore(startUtc)

        logger().info("isBeforeJoinPeriod=$isBeforeJoinStart, startUtc=$startUtc, currentServerUtc=$currentServer")

    } else {
        // TODO: Not test yet
        val currentClient = LocalDateTime.ofInstant(Instant.now(clock), zone)
        isBeforeJoinStart = currentClient.isBefore(startDate)
    }

    return isBeforeJoinStart
}

fun MissionDoc.isAfterJoinPeriod(clock: Clock, zone: ZoneId): Boolean {
    val endDate = stringToLocalDateTime(joinEndDate)

    // Current we only support missions that start/expired at a specific utc time.
    val isUtcBasedMission = true

    val isAfterJoinEnd: Boolean
    if (isUtcBasedMission) {
        val currentServer = clock.instant()
        val endUtc = endDate.toInstant(ZoneOffset.UTC)
        isAfterJoinEnd = currentServer == endUtc || currentServer.isAfter(endUtc)

        logger().info("isAfterJoinPeriod=$isAfterJoinEnd, endUtc=$endUtc, currentServerUtc=$currentServer")

    } else {
        // TODO: Not test yet
        val currentClient = LocalDateTime.ofInstant(Instant.now(clock), zone)
        isAfterJoinEnd = currentClient == endDate || currentClient.isAfter(endDate)
    }

    return isAfterJoinEnd
}

fun MissionDoc.isJoinPeriod(clock: Clock, zone: ZoneId): Boolean {
    val startDate = stringToLocalDateTime(joinStartDate)
    val endDate = stringToLocalDateTime(joinEndDate)

    // Current we only support missions that start/expired at a specific utc time.
    val isUtcBasedMission = true

    val isAfterJoinStart: Boolean
    val isBeforeJoinEnd: Boolean
    return if (isUtcBasedMission) {
        val currentServer = clock.instant()
        val startUtc = startDate.toInstant(ZoneOffset.UTC)
        val endUtc = endDate.toInstant(ZoneOffset.UTC)
        isAfterJoinStart = currentServer == startUtc || currentServer.isAfter(startUtc)
        isBeforeJoinEnd = currentServer.isBefore(endUtc)
        val result = isAfterJoinStart && isBeforeJoinEnd
        logger().info("isJoinPeriod=$result, startUtc=$startUtc, endUtc=$endUtc, currentServerUtc=$currentServer")
        result

    } else {
        val currentClient = LocalDateTime.ofInstant(Instant.now(clock), zone)
        isAfterJoinStart = currentClient.isAfter(startDate)
        isBeforeJoinEnd = currentClient.isBefore(endDate)
        isAfterJoinStart && isBeforeJoinEnd
    }
}
