package org.mozilla.msrp.platform.mission;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
class MissionService {

    private MissionRepository missionRepository;

    @Inject
    MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    List<Mission> getMissionsByGroupId(String groupId) {
        if (isSuspiciousUser()) {
            return new ArrayList<>();
        }

        // TODO: Aggregate client-facing mission data
        return this.missionRepository.getMissionsByGroupId(groupId).stream()
                .filter(this::isMissionAvailable)
                .map(this::convertToMission)
                .collect(Collectors.toList());
    }

    private boolean isSuspiciousUser() {
        // TODO: Verify user status
        return false;
    }

    private boolean isMissionAvailable(MissionDoc mission) {
        // TODO: Expired, Reach join quota, etc
        return true;
    }

    private Mission convertToMission(MissionDoc missionDoc) {
        // TODO: String & L10N
        String name = getStringById(missionDoc.getTitleId());
        String description = getStringById(missionDoc.getDescriptionId());

        // TODO: Aggregate mission progress

        return new Mission(missionDoc.getMid(),
                name,
                description,
                missionDoc.getEndpoint());
    }

    /**
     * Get localized string by string id
     * @param id string id
     * @return localized string (if any)
     */
    private String getStringById(String id) {
        // TODO: A way to store the mapping of id to string
        // TODO: A way to support multiple languages
        // TODO: Possible solution is a custom MessageSource
        return "string of id " + id;
    }

    List<Mission> createMissions(List<MissionCreateData> missionList) {
        return missionList.stream()
                .map(missionRepository::createMission)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToMission)
                .collect(Collectors.toList());
    }

    List<MissionReferenceDoc> groupMissions(String groupId, List<MissionGroupItemData> groupItems) {
        return missionRepository.groupMissions(groupId, groupItems);
    }

    /**
     * Join user to the mission
     * @param uid user id
     * @param mid mission id
     * @return updated mission json for client
     */
    MissionJoinResponse joinMission(String uid, String missionType, String mid) {
        MissionJoinDoc joinDoc = missionRepository.joinMission(uid, missionType, mid);
        return new MissionJoinResponse(mid, joinDoc.getStatus());
    }
}
