package org.mozilla.msrp.platform.mission;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Log4j2
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

    /**
     * Create missions
     *
     * Request body
     * {
     *  "missions": [
     *      {
     *          "mid": "wsF1OHt3CrtrGbAyT8xo",
     *          "title": "...",
     *          "description": "...",
     *          "endpoint": "/mission_daily/wsF1OHt3CrtrGbAyT8xo"
     *      },
     *      {
     *          "mid": "5quMPsVLh0wims22pstL",
     *          "title": "...",
     *          "description": "...",
     *          "endpoint": "/mission_one_shot/wsF1OHt3CrtrGbAyT8xo"
     *      }
     *  ]
     * }
     *
     * Response
     * [
     *  {
     *      "mid": "wsF1OHt3CrtrGbAyT8xo",
     *      "name": "...",
     *      "description": "..."
     *  },
     *  {
     *      "mid": "5quMPsVLh0wims22pstL",
     *      "name": "...",
     *      "description": "..."
     *  }
     * ]
     *
     * @param request data needed to create mission
     * @return response with created mission in body
     */
    @RequestMapping(value = "/missions", method = POST)
    public ResponseEntity<List<Mission>> createMission(@RequestBody MissionCreateRequest request) {
        List<Mission> missions = missionService.createMissions(request.getMissions());
        return new ResponseEntity<>(missions, HttpStatus.OK);
    }

    /**
     * Create missions
     *
     * Request body
     * {
     *  "missions": [
     *     {
     *         "endpoint": "/mission_daily/ooKv67x8MFzsIr44PfBz"
     *     },
     *     {
     *         "endpoint": "/mission_one_shot/VjTcDnJBHEqlzVjkj8zb"
     *     }
     *  ]
     * }
     *
     * Response
     * [
     *  {
     *      "endpoint": "/mission_daily/ooKv67x8MFzsIr44PfBz"
     *  },
     *  {
     *      "endpoint": "/mission_one_shot/VjTcDnJBHEqlzVjkj8zb"
     *  }
     * ]
     *
     * @param groupId group id
     * @param request contains missions to be grouped
     * @return response with created mission in body
     */
    @RequestMapping(value = "/group/{groupId}/missions")
    public ResponseEntity<List<MissionReferenceDoc>> groupMissions(@PathVariable("groupId") String groupId,
                                                                   @RequestBody MissionGroupRequest request) {
        List<MissionReferenceDoc> body = missionService.groupMissions(groupId, request.getMissions());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /**
     * Join mission
     *
     * Response
     * {
     *      "mid": "",
     *      "status": "join"
     * }
     *
     * @param missionType mission type, which is also the name of the mission collection
     * @param mid mission id
     * @return response indicating mission id and it's new join status
     */
    @RequestMapping(value = "/{missionType}/{mid}", method = POST)
    public ResponseEntity<MissionJoinResponse> joinMission(@PathVariable("missionType") String missionType,
                                                           @PathVariable("mid") String mid) {
        String uid = getUid();

        try {
            MissionJoinResponse result = missionService.joinMission(uid, missionType, mid);
            return new ResponseEntity<>(result, HttpStatus.CREATED);

        } catch (MissionNotFoundException e) {
            log.info("join exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    // TODO: Get user id from bearer token
    private String getUid() {
        return "roger_random_uid";
    }
}
