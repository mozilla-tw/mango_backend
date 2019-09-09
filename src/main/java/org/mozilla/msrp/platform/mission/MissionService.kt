package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.mission.qualifier.MissionQualifier
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
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

    private val log: Logger = logger()

    fun getMissionsByGroupId(uid: String, groupId: String): List<MissionListItem> {
        return if (isSuspiciousUser) {
            ArrayList()

        } else {
            // TODO: Aggregate client-facing mission data
            this.missionRepository.getMissionsByGroupId(groupId)
                    .filter { isMissionAvailable(it) }
                    .map { aggregateMissionListItem(uid, it) }
        }
    }

    private fun isMissionAvailable(mission: MissionDoc): Boolean {
        // TODO: Expired, Reach join quota, etc
        return true
    }

    private fun aggregateMissionListItem(uid: String, missionDoc: MissionDoc): MissionListItem {
        // TODO: String & L10N
        val name = getStringById(missionDoc.titleId)
        val description = getStringById(missionDoc.descriptionId)

        val joinDoc = missionRepository.getJoinStatus(uid, missionDoc.missionType, missionDoc.mid)

        // TODO: Aggregate mission progress

        return MissionListItem(
                mid = missionDoc.mid,
                title = name,
                description = description,
                endpoint = missionDoc.endpoint,
                events = missionDoc.interestPings,
                expiredDate = missionDoc.expiredDate,
                status = joinDoc?.status,
                min_version = missionDoc.min_version
        )
    }

    /**
     * Get localized string by string id
     * @param id string id
     * @return localized string (if any)
     */
    private fun getStringById(id: String): String {
        // TODO: A way to store the mapping of id to string
        // TODO: A way to support multiple languages
        // TODO: Possible solution is a custom MessageSource
        return "string of id $id"
    }

    fun createMissions(missionList: List<MissionCreateData>): List<MissionCreateResult> {
        return missionList
                .map { missionRepository.createMission(it) }
                .map {
                    MissionCreateResult(
                            mid = it.mid,
                            title = getStringById(it.titleId),
                            description = getStringById(it.descriptionId),
                            expiredDate = it.expiredDate,
                            events = it.interestPings,
                            endpoint = it.endpoint,
                            min_version = it.min_version
                    )
                }
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
        val existed = missionRepository.getJoinStatus(uid, missionType, mid)

        existed ?: return joinWithNewRecord(uid, missionType, mid)

        return if (existed.status.canTransferToJoin()) {
            val updated = existed.copy(status = JoinStatus.Joined)
            missionRepository.joinMission(updated)
            MissionJoinResponse.Success(updated.uid, updated.status)

        } else {
            log.info("cannot join mission, uid=$uid, type=$missionType, mid=$mid, record=$existed")
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
        missionRepository.joinMission(newRecord)
        return MissionJoinResponse.Success(newRecord.uid, newRecord.status)
    }

    fun quitMission(
            uid: String,
            missionType: String,
            mid: String
    ) : MissionQuitResponse {

        val existed = missionRepository.getJoinStatus(uid, missionType, mid)

        log.info("quit mission $existed")

        existed ?: return MissionQuitResponse.Error("mission not exist", HttpStatus.NOT_FOUND)

        if (existed.status != JoinStatus.Joined) {
            return MissionQuitResponse.Error("not joined", HttpStatus.CONFLICT)
        }

        missionRepository.quitMission(existed)
        return MissionQuitResponse.Success(mid = existed.mid, status = existed.status)
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

        return missions.mapNotNull { missionDoc ->
            val mid = missionDoc.mid
            val type = missionDoc.missionTypeEnum

            log.info("update progress, mid=$mid, type=$type")

            missionQualifier.updateProgress(uid, mid, type, zone)

        }.map { progressDoc ->
            MissionCheckInResult(progressDoc.toResponseFields())
        }
    }
}
