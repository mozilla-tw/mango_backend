package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.MissionQualifier
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.http.HttpStatus
import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@Named class MissionService @Inject constructor(
        private val missionRepository: MissionRepository
) {

    // TODO: Verify user status
    private val isSuspiciousUser: Boolean
        get() = false

    @Inject
    lateinit var missionQualifier: MissionQualifier

    @Inject
    lateinit var missionMessageSource: MessageSource

    @Inject
    lateinit var locale: Locale

    @Inject
    lateinit var clock: Clock

    private val log: Logger = logger()

    fun getMissionsByGroupId(uid: String, groupId: String, zone: ZoneId): List<MissionListItem> {
        return if (isSuspiciousUser) {
            ArrayList()

        } else {
            this.missionRepository.getMissionsByGroupId(groupId)
                    .filter { isMissionValid(it) }
                    .filter {
                        val joinStatus = missionRepository.getJoinStatus(uid, it.missionType, it.mid)
                        val joinCount = missionRepository.getJoinCount(it.missionType, it.mid)
                        isMissionAvailableForShowing(uid, it, joinStatus, joinCount, clock, zone)
                    }
                    .map { aggregateMissionListItem(uid, it) }
        }
    }

    private fun isMissionValid(mission: MissionDoc): Boolean {
        if (!hasString(mission.titleId)) {
            log.error("title not defined for mission: ${mission.missionType}/${mission.mid}")
            return false
        }

        if (!hasString(mission.descriptionId)) {
            log.error("description not defined for mission: ${mission.missionType}/${mission.mid}")
            return false
        }

        return true
    }

    /**
     * Is this mission available
     */
    internal fun isMissionAvailableForShowing(
            uid: String,
            mission: MissionDoc,
            joinStatus: JoinStatus?,
            joinCount: Int,
            clock: Clock,
            zone: ZoneId
    ): Boolean {

        val missionType = mission.missionType
        val mid = mission.mid

        val logInfo = "isMissionAvailableForShowing: mid=$mid, type=$missionType, uid=$uid, zone=$zone"

        val missionState = try {
            getMissionJoinState(mission, joinStatus, joinCount, clock, zone)

        } catch (e: DateTimeParseException) {
            log.error("$logInfo, msg=illegal mission format")
            return false
        }

        log.info("$logInfo, state=$missionState")

        if (missionState.reachQuota) {
            if (!missionState.isJoined) {
                log.info("$logInfo, msg=quota reached")
                return false
            }
        }

        if (missionState.isBeforeJoinPeriod) {
            log.info("$logInfo, msg=before join start")
            return false
        }

        if (missionState.isAfterJoinPeriod) {
            if (!missionState.isJoined) {
                log.info("$logInfo, msg=after join end")
                return false
            }
        }

        if (missionState.isExpired) {
            if (!missionState.isComplete) {
                log.info("$logInfo, msg=expired")
                return false
            }
        }

        return true
    }

    private fun hasString(resId: String): Boolean {
        return try {
            getStringById(resId, Locale.getDefault())
            true

        } catch (e: NoSuchMessageException) {
            false
        }
    }

    private fun aggregateMissionListItem(uid: String, missionDoc: MissionDoc): MissionListItem {
        val name = getStringById(missionDoc.titleId, locale)
        val description = getStringById(missionDoc.descriptionId, locale)

        val joinStatus = missionRepository.getJoinStatus(
                uid,
                missionDoc.missionType,
                missionDoc.mid
        )?.let { it } ?: JoinStatus.New

        val progress = missionQualifier.getProgress(
                uid,
                missionDoc.mid,
                missionDoc.missionTypeEnum
        )

        val important = missionRepository.isImportantMission(missionDoc.missionType, missionDoc.mid)

        return MissionListItem(
                mid = missionDoc.mid,
                title = name,
                description = description,
                endpoint = missionDoc.endpoint,
                events = missionDoc.interestPings,
                expiredDate = missionDoc.expiredDate,
                status = joinStatus,
                minVersion = missionDoc.minVersion,
                progress = progress?.toProgressResponse() ?: emptyMap(),
                important = important,
                missionType = missionDoc.missionType
        )
    }

    /**
     * Get localized string by string id
     * @param id string id
     * @return localized string (if any)
     */
    private fun getStringById(id: String, locale: Locale, vararg args: String = emptyArray()): String {
        return missionMessageSource.getMessage(id, args, locale)
    }

    fun createMissions(missionList: List<MissionCreateData>): MissionCreateResult {
        val invalidItems = missionList
                .map { it.missionName to validateMissionCreateData(it) }
                .filterNot { it.second.isValid() }
                .map { MissionCreateFailedItem(it.first, it.second.toString()) }

        if (invalidItems.isNotEmpty()) {
            return MissionCreateResult.Error(invalidItems)
        }

        val results = missionList.map { createData ->
            val mission = missionRepository.createMission(createData)
            MissionCreateResultItem(
                    mid = mission.mid,
                    title = mission.titleId,
                    description = mission.descriptionId,
                    startDate = mission.startDate,
                    joinStartDate = mission.joinStartDate,
                    joinEndDate = mission.joinEndDate,
                    expiredDate = mission.expiredDate,
                    events = mission.interestPings,
                    endpoint = mission.endpoint,
                    minVersion = mission.minVersion,
                    missionParams = mission.missionParams,
                    rewardType = mission.rewardType,
                    joinQuota = mission.joinQuota
            )
        }

        return MissionCreateResult.Success(results)
    }

    private fun validateMissionCreateData(data: MissionCreateData): ValidationResult {
        val result = ValidationResult()

        val type = MissionType.from(data.missionType)
        if (type == MissionType.Unknown) {
            result.addError("unrecognized mission type ${data.missionType}")
        }

        if (data.pings.isEmpty()) {
            result.addError("empty pings")
        }

        if (data.missionParams?.isEmpty() != false) {
            result.addError("no mission parameter specified")
        }

        return result
    }

    fun groupMissions(
            groupId: String,
            groupItems: List<MissionGroupItemData>
    ): List<MissionReferenceDoc> {
        return missionRepository.groupMissions(groupId, groupItems)
    }

    /**
     * Join user to the mission
     * @param uid user id
     * @param mid mission id
     * @return updated mission json for client
     */
    fun joinMission(
            uid: String,
            missionType: String,
            mid: String,
            zone: ZoneId
    ): MissionJoinResult {
        val logInfo = "uid=$uid, type=$missionType, mid=$mid, zone=$zone"

        val mission = missionRepository.findMission(missionType, mid) ?: run {
            log.info("mission not found, $logInfo")
            return MissionJoinResult.Error("mission not found", HttpStatus.NOT_FOUND)
        }

        val joinStatus = missionRepository.getJoinStatus(uid, missionType, mid)
        val joinCount = missionRepository.getJoinCount(missionType, mid)

        val missionState = try {
            getMissionJoinState(mission, joinStatus, joinCount, clock, zone)

        } catch (e: DateTimeParseException) {
            log.error("illegal mission format, $logInfo")
            return MissionJoinResult.Error(
                    "illegal mission format",
                    HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        if (missionState.reachQuota) {
            log.info("mission join quota reached, $logInfo")
            return MissionJoinResult.Error("mission reach join quota", HttpStatus.FORBIDDEN)
        }

        if (missionState.isBeforeJoinPeriod) {
            log.info("not open for join, $logInfo")
            return MissionJoinResult.Error("mission not open", HttpStatus.FORBIDDEN)
        }

        if (missionState.isAfterJoinPeriod) {
            log.info("exceed join period, $logInfo")
            return MissionJoinResult.Error("mission closed", HttpStatus.GONE)
        }

        if (missionState.isJoined) {
            log.info("already joined, $logInfo")
            return MissionJoinResult.Error("already joined", HttpStatus.CONFLICT)
        }

        // Available for join
        val joinResult = missionRepository.joinMission(uid, missionType, mid)
        missionQualifier.updateProgress(uid, mid, MissionType.from(missionType), zone)
        log.info("join mission, $logInfo, state=${joinResult.status}")
        return MissionJoinResult.Success(joinResult.mid, joinResult.status)
    }

    private data class MissionJoinState(
        var reachQuota: Boolean = false,

        var isBeforeJoinPeriod: Boolean = false,
        var isOpenedForJoin: Boolean = false,
        var isAfterJoinPeriod: Boolean = false,

        var isExpired: Boolean = false,
        var isJoined: Boolean = false,
        var isComplete: Boolean = false
    )

    private fun getMissionJoinState(
            mission: MissionDoc,
            joinStatus: JoinStatus?,
            joinCount: Int,
            clock: Clock,
            zone: ZoneId
    ): MissionJoinState {

        val missionState = MissionJoinState()

        missionState.reachQuota = joinCount >= mission.joinQuota

        missionState.isBeforeJoinPeriod = mission.isBeforeJoinPeriod(clock, zone)
        missionState.isOpenedForJoin = mission.isJoinPeriod(clock, zone)
        missionState.isAfterJoinPeriod = mission.isAfterJoinPeriod(clock, zone)

        missionState.isExpired = mission.isExpired(clock, zone)

        missionState.isJoined = joinStatus?.let { it != JoinStatus.New } ?: false

        missionState.isComplete = joinStatus == JoinStatus.Complete
                || joinStatus == JoinStatus.Redeemed

        return missionState
    }

    fun quitMission(
            uid: String,
            missionType: String,
            mid: String
    ) : MissionQuitResponse {

        val status = missionRepository.getJoinStatus(uid, missionType, mid)

        log.info("quit mission $status")

        status ?: return MissionQuitResponse.Error("mission not exist", HttpStatus.NOT_FOUND)

        if (status != JoinStatus.Joined) {
            return MissionQuitResponse.Error("not joined", HttpStatus.CONFLICT)
        }

        missionRepository.quitMission(uid, missionType, mid)
        missionQualifier.clearProgress(uid, mid, MissionType.from(missionType))
        return MissionQuitResponse.Success(mid = mid, status = JoinStatus.New)
    }

    /**
     * Check missions interest to the given ping
     */
    fun checkInMissions(
            uid: String,
            ping: String,
            zone: ZoneId
    ): List<MissionCheckInResult> {

        val missions = missionRepository.findJoinedMissionsByPing(uid, ping)
        log.info("ping=$ping, missions=${missions.map { "${it.missionType}/${it.mid}" }}")

        return missions
                .filter { isJoined(uid, it.missionType, it.mid) }
                .mapNotNull { updateProgress(uid, it.missionType, it.mid, zone) }
                .map { convertToCheckInResult(uid, it.missionType, it.mid, it) }
    }

    private fun isJoined(uid: String, missionType: String, mid: String): Boolean {
        return missionRepository.getJoinStatus(uid, missionType, mid) == JoinStatus.Joined
    }

    private fun updateProgress(
            uid: String,
            missionType: String,
            mid: String,
            zone: ZoneId
    ): MissionProgressDoc? {

        log.info("update progress, mid=$mid, type=$missionType")
        return missionQualifier.updateProgress(uid, mid, MissionType.from(missionType), zone)
    }

    private fun convertToCheckInResult(
            uid: String,
            missionType: String,
            mid: String,
            progress: MissionProgressDoc
    ): MissionCheckInResult {
        val status = missionRepository.getJoinStatus(uid, missionType, mid)?.status ?: 0

        return MissionCheckInResult(mapOf(
                "mid" to mid,
                "missionType" to missionType,
                "status" to status,
                "progress" to progress.toProgressResponse()
        ))
    }
}
