package org.mozilla.msrp.platform.mission;

import org.mozilla.msrp.platform.mission.qualifier.DailyMissionProgressDoc;
import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc;

import java.util.List;

public interface MissionRepository {
    /**
     * Get mission list for audience group with the given groupId
     * @param groupId id of the audience group
     * @return a list of mission for the corresponding group
     */
    List<MissionDoc> getMissionsByGroupId(String groupId);

    MissionDoc createMission(MissionCreateData createData);

    List<MissionReferenceDoc> groupMissions(String groupId, List<MissionGroupItemData> groupItems);

    MissionJoinDoc joinMission(String uid, String missionType, String mid);

    List<MissionDoc> findJoinedMissionsByPing(String uid, String ping);

    DailyMissionProgressDoc getDailyMissionProgress(String uid, String mid);
    void updateDailyMissionProgress(MissionProgressDoc progressDoc);
}
