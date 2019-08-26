package org.mozilla.msrp.platform.mission;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class MissionController {

    @Inject
    private MissionService missionService;

    /**
     * Fetch user requested missions, and aggregate data that is needed by client
     *
     * Request
     * GET /group/{groupId}/missions
     *
     * Response
     * [
     *  {
     *      "mid": "wsF1OHt3CrtrGbAyT8xo",
     *      "title": "...",
     *      "description": "...",
     *      "endpoint": "/mission_daily/wsF1OHt3CrtrGbAyT8xo"
     *  },
     *  {
     *      "mid": "5quMPsVLh0wims22pstL",
     *      "title": "...",
     *      "description": "...",
     *      "endpoint": "/mission_one_shot/wsF1OHt3CrtrGbAyT8xo"
     *  },
     * ]
     *
     * @param groupId id for audience group
     * @return Client-facing mission list
     */
    @RequestMapping(value = "/group/{groupId}/missions", method = GET)
    public ResponseEntity<List<Mission>> getGroupMissions(@PathVariable("groupId") String groupId) {
        List<Mission> missions = missionService.getMissionsByGroupId(groupId);

        return new ResponseEntity<>(missions, HttpStatus.OK);
    }
}
