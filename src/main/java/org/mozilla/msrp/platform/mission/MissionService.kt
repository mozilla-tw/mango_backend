package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.firestore.stringToLocalDateTime
import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.MissionQualifier
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import java.time.*
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
                    .filter { isMissionAvailableForShowing(uid, it, zone) }
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
    private fun isMissionAvailableForShowing(uid:String, mission: MissionDoc, zone: ZoneId): Boolean {
        val missionType = mission.missionType
        val mid = mission.mid

        val logInfo = "uid=$uid, type=$missionType, mid=$mid, zone=$zone"
        val result = checkMissionJoinableState(uid, mission.missionType, mission.mid, zone)

        return when (result) {
            JoinableState.AlreadyJoined -> true
            JoinableState.Joinable -> true
            else -> {
                log.info("mission not available for showing, $logInfo, status=$result")
                false
            }
        }
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
        return when (checkMissionJoinableState(uid, missionType, mid, zone)) {
            JoinableState.NotFound -> {
                log.info("mission not found, $logInfo")
                MissionJoinResult.Error("mission not found", HttpStatus.NOT_FOUND)
            }

            JoinableState.ReachQuota -> {
                log.info("mission join quota reached, $logInfo")
                MissionJoinResult.Error("mission reach join quota", HttpStatus.FORBIDDEN)
            }

            JoinableState.BeforeJoinPeriod -> {
                log.info("not open for join, $logInfo")
                MissionJoinResult.Error("mission not open", HttpStatus.FORBIDDEN)
            }

            JoinableState.AfterJoinPeriod -> {
                log.info("exceed join period, $logInfo")
                MissionJoinResult.Error("mission closed", HttpStatus.GONE)
            }

            JoinableState.AlreadyJoined -> {
                log.info("already joined, $logInfo")
                MissionJoinResult.Error("already joined", HttpStatus.CONFLICT)
            }

            JoinableState.IllegalMissionFormat -> {
                log.error("illegal mission format, $logInfo")
                MissionJoinResult.Error(
                        "illegal mission format",
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
            }

            else -> {
                val joinResult = missionRepository.joinMission(uid, missionType, mid)
                missionQualifier.updateProgress(uid, mid, MissionType.from(missionType), zone)
                log.info("join mission, $logInfo, state=${joinResult.status}")
                MissionJoinResult.Success(joinResult.mid, joinResult.status)
            }
        }
    }

    private fun checkMissionJoinableState(
            uid: String,
            missionType: String,
            mid: String,
            zone: ZoneId
    ): JoinableState {

        // Is exist
        val mission = missionRepository.findMission(missionType, mid)
                ?: return JoinableState.NotFound

        // Is reach join quota
        val joinCount = missionRepository.getJoinCount(missionType, mid)
        log.info("current join count=$joinCount")
        if (joinCount >= mission.joinQuota) {
            return JoinableState.ReachQuota
        }

        val clientDateTime: LocalDateTime = LocalDateTime.ofInstant(Instant.now(clock), zone)

        // Is before the first joinable date
        val joinStartDateTime = getLocalDateTime(mission.joinStartDate)
                ?: return JoinableState.IllegalMissionFormat

        if (clientDateTime.isBefore(joinStartDateTime)) {
            return JoinableState.BeforeJoinPeriod
        }

        // Is exceed the last joinable date
        val joinEndDateTime = getLocalDateTime(mission.joinEndDate)
                ?: return JoinableState.IllegalMissionFormat

        if (clientDateTime.isAfter(joinEndDateTime)) {
            return JoinableState.AfterJoinPeriod
        }

        // Already joined
        val joinStatus = missionRepository.getJoinStatus(uid, missionType, mid)
        if (joinStatus != null && joinStatus != JoinStatus.New) {
            return JoinableState.AlreadyJoined
        }

        return JoinableState.Joinable
    }

    private fun getLocalDateTime(dateTimeString: String): LocalDateTime? {
        return try {
            stringToLocalDateTime(dateTimeString)

        } catch (e: DateTimeParseException) {
            log.error(NestedExceptionUtils.buildMessage("illegal date time format, string=$dateTimeString", e))
            return null
        }
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

    private enum class JoinableState {
        Joinable,
        ReachQuota,
        Expired,
        NotFound,
        AlreadyJoined,
        IllegalMissionFormat,
        BeforeJoinPeriod,
        AfterJoinPeriod,
    }
}
