package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.Transaction
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
    fun findMission(missionType: String, mid: String): MissionDoc?

    fun groupMissions(groupId: String, groupItems: List<MissionGroupItemData>): List<MissionReferenceDoc>

    fun getJoinStatus(uid: String, missionType: String, mid: String): JoinStatus?
    fun setJoinStatus(status: JoinStatus, uid: String, missionType: String, mid: String)

    fun joinMission(uid: String, missionType: String, mid: String): MissionJoinDoc

    fun getMissionJoinDoc(uid: String, missionType: String, mid: String): MissionJoinDoc?

    fun quitMission(uid: String, missionType: String, mid: String): Boolean

    fun findJoinedMissionsByPing(uid: String, ping: String): List<MissionDoc>

    fun getDailyMissionParams(mid: String): Map<String, Any>
    fun getDailyMissionProgress(uid: String, mid: String): DailyMissionProgressDoc?
    fun updateDailyMissionProgress(progressDoc: MissionProgressDoc)
    fun clearDailyMissionProgress(uid: String, mid: String)

    fun isImportantMission(missionType: String, mid: String): Boolean
    fun updateMissionJoinDocAfterRedeem(uid: String, missionType: String, mid: String, rewardDocId: String, transaction: Transaction): Boolean

    fun getJoinCount(missionType: String, mid: String): Int
    fun getProgress(missionType: String, mid: String): List<DailyMissionProgressDoc>
}
