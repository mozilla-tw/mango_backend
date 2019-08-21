package org.mozilla.msrp.platform.mission;

import java.util.List;

public interface MissionRepository {
    /**
     * Get mission list for audience group with the given groupId
     * @param groupId id of the audience group
     * @return a list of mission for the corresponding group
     */
    List<MissionDoc> getMissionsByGroupId(String groupId);
}
