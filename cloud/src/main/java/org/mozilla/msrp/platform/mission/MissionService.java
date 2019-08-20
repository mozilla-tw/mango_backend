package org.mozilla.msrp.platform.mission;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
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

    private boolean isMissionAvailable(RawMission mission) {
        // TODO: Expired, Reach join quota, etc
        return true;
    }

    private Mission convertToMission(RawMission rawMission) {
        // TODO: String & L10N
        String name = getStringById(rawMission.getNameId());
        String description = getStringById(rawMission.getDescriptionId());

        // TODO: Aggregate mission progress

        return new Mission(rawMission.getMid(),
                name,
                description);
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
}
