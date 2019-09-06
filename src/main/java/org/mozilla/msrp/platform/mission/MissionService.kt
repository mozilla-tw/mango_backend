package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.mission.qualifier.MissionQualifier
import org.mozilla.msrp.platform.util.logger
import org.slf4j.Logger
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

    fun getMissionsByGroupId(groupId: String): List<Mission> {
        return if (isSuspiciousUser) {
            ArrayList()

        } else {
            // TODO: Aggregate client-facing mission data
            this.missionRepository.getMissionsByGroupId(groupId)
                    .filter { isMissionAvailable(it) }
                    .map { convertToMission(it) }
        }
    }

    private fun isMissionAvailable(mission: MissionDoc): Boolean {
        // TODO: Expired, Reach join quota, etc
        return true
    }

    private fun convertToMission(missionDoc: MissionDoc): Mission {
        // TODO: String & L10N
        val name = getStringById(missionDoc.titleId)
        val description = getStringById(missionDoc.descriptionId)

        // TODO: Aggregate mission progress

        return Mission(
                mid = missionDoc.mid,
                title = name,
                description = description,
                endpoint = missionDoc.endpoint,
                events = missionDoc.interestPings
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

    fun createMissions(missionList: List<MissionCreateData>): List<Mission> {
        return missionList
                .map { missionRepository.createMission(it) }
                .map { convertToMission(it) }
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
        val (_, status) = missionRepository.joinMission(uid, missionType, mid)
        return MissionJoinResponse(mid, status)
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
