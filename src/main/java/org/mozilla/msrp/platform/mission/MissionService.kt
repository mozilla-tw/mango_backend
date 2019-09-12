package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.MissionQualifier
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.http.HttpStatus
import java.time.ZoneId
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

    private val log: Logger = logger()

    fun getMissionsByGroupId(uid: String, groupId: String): List<MissionListItem> {
        return if (isSuspiciousUser) {
            ArrayList()

        } else {
            this.missionRepository.getMissionsByGroupId(groupId)
                    .filter { isMissionAvailable(it) }
                    .map { aggregateMissionListItem(uid, it) }
        }
    }

    private fun isMissionAvailable(mission: MissionDoc): Boolean {
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

    private fun hasString(resId: String): Boolean {
        return try {
            getStringById(resId, Locale.getDefault())
            true

        } catch (e : NoSuchMessageException) {
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

        return MissionListItem(
                mid = missionDoc.mid,
                title = name,
                description = description,
                endpoint = missionDoc.endpoint,
                events = missionDoc.interestPings,
                expiredDate = missionDoc.expiredDate,
                status = joinStatus,
                minVersion = missionDoc.minVersion,
                progress = progress?.toProgressResponse() ?: emptyMap()
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

    fun createMissions(missionList: List<MissionCreateData>): List<MissionCreateResult> {
        return missionList.map { createData ->
            val validation = validateMissionCreateData(createData)
            if (validation.isValid()) {
                val mission = missionRepository.createMission(createData)
                MissionCreateResult.Success(
                        mid = mission.mid,
                        title = mission.titleId,
                        description = mission.descriptionId,
                        expiredDate = mission.expiredDate,
                        events = mission.interestPings,
                        endpoint = mission.endpoint,
                        minVersion = mission.minVersion,
                        missionParams = mission.missionParams
                )

            } else {
                MissionCreateResult.Error(createData.missionName, validation.toString())
            }
        }
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

        if (data.missionParams.isEmpty()) {
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
            mid: String
    ): MissionJoinResponse {
        val joinStatus = missionRepository.getJoinStatus(uid, missionType, mid)

        joinStatus ?: return joinWithNewRecord(uid, missionType, mid)

        return if (joinStatus == JoinStatus.New) {
            val result = missionRepository.joinMission(uid, missionType, mid)
            MissionJoinResponse.Success(result.uid, result.status)

        } else {
            log.info("cannot join mission, uid=$uid, type=$missionType, mid=$mid")
            MissionJoinResponse.Error("cannot join mission")
        }
    }

    private fun joinWithNewRecord(
            uid: String,
            missionType: String,
            mid: String
    ): MissionJoinResponse {

        val newRecord = MissionJoinDoc(
                uid = uid,
                status = JoinStatus.Joined,
                missionType = missionType,
                mid = mid
        )
        log.info("join mission first time $newRecord")
        missionRepository.joinMission(uid, missionType, mid)
        return MissionJoinResponse.Success(newRecord.uid, newRecord.status)
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

        val response = mutableMapOf<String, Any>()
        response.putAll(progress.toResponseFields())
        response["status"] = status

        return MissionCheckInResult(response)
    }
}
