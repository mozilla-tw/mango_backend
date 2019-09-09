package org.mozilla.msrp.platform.mission

import org.mozilla.msrp.platform.mission.qualifier.DailyMissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc

interface MissionRepository {
    /**
     * Get mission list for audience group with the given groupId
     * @param groupId id of the audience group
     * @return a list of mission for the corresponding group
     */
    fun getMissionsByGroupId(groupId: String): List<MissionDoc>

    fun createMission(createData: MissionCreateData): MissionDoc

    fun groupMissions(groupId: String, groupItems: List<MissionGroupItemData>): List<MissionReferenceDoc>

    fun getJoinStatus(uid: String, missionType: String, mid: String): MissionJoinDoc?
    fun joinMission(joinDoc: MissionJoinDoc): MissionJoinDoc
    fun quitMission(joinDoc: MissionJoinDoc): Boolean

    fun findJoinedMissionsByPing(uid: String, ping: String): List<MissionDoc>

    fun getDailyMissionProgress(uid: String, mid: String): DailyMissionProgressDoc?
    fun updateDailyMissionProgress(progressDoc: MissionProgressDoc)
}
