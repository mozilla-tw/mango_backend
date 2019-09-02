package org.mozilla.msrp.platform.mission

import javax.inject.Inject
import javax.inject.Named
import java.util.ArrayList
import java.util.stream.Collectors

@Named
internal class MissionService @Inject
constructor(private val missionRepository: MissionRepository) {

    private// TODO: Verify user status
    val isSuspiciousUser: Boolean
        get() = false

    fun getMissionsByGroupId(groupId: String): List<Mission> {
        return if (isSuspiciousUser) {
            ArrayList()
        } else this.missionRepository.getMissionsByGroupId(groupId).stream()
                .filter(Predicate<MissionDoc> { this.isMissionAvailable(it) })
                .map<Mission>(Function<MissionDoc, Mission> { this.convertToMission(it) })
                .collect<List<Mission>, Any>(Collectors.toList())

        // TODO: Aggregate client-facing mission data
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

        return Mission(missionDoc.mid,
                name,
                description,
                missionDoc.endpoint,
                missionDoc.pings)
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
        return missionList.stream()
                .map<MissionDoc>(Function<MissionCreateData, MissionDoc> { missionRepository.createMission(it) })
                .map<Mission>(Function<MissionDoc, Mission> { this.convertToMission(it) })
                .collect<List<Mission>, Any>(Collectors.toList())
    }

    fun groupMissions(groupId: String, groupItems: List<MissionGroupItemData>): List<MissionReferenceDoc> {
        return missionRepository.groupMissions(groupId, groupItems)
    }

    /**
     * Join user to the mission
     * @param uid user id
     * @param mid mission id
     * @return updated mission json for client
     */
    fun joinMission(uid: String, missionType: String, mid: String): MissionJoinResponse {
        val (_, status) = missionRepository.joinMission(uid, missionType, mid)
        return MissionJoinResponse(mid, status)
    }
}
