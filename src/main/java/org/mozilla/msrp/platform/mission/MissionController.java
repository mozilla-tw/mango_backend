package org.mozilla.msrp.platform.mission;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.time.DateTimeException;
import java.time.ZoneId;

import static org.springframework.web.bind.annotation.RequestMethod.*;

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
    @RequestMapping(value = "/api/v1/group/{groupId}/missions", method = GET)
    public ResponseEntity<MissionListResponse> getMissionByGroupId(
            @PathVariable("groupId") String groupId,
            @RequestAttribute("uid") String uid) {
        List<MissionListItem> missions = missionService.getMissionsByGroupId(uid, groupId);

        return ResponseEntity.ok(new MissionListResponse.Success(missions));
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
    @RequestMapping(value = "/api/v1/missions", method = POST)
    public ResponseEntity<MissionCreateResponse> createMission(
            @RequestBody MissionCreateRequest request) {

        MissionCreateResult result = missionService.createMissions(request.getMissions());

        if (result instanceof MissionCreateResult.Success) {
            MissionCreateResult.Success successResult = (MissionCreateResult.Success) result;
            return ResponseEntity.ok(new MissionCreateResponse.Success(successResult.getResults()));

        } else {
            MissionCreateResult.Error errorResult = (MissionCreateResult.Error) result;
            return ResponseEntity.badRequest()
                    .body(new MissionCreateResponse.Error(errorResult.getResults()));
        }
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
    @RequestMapping(value = "/api/v1/group/{groupId}/missions")
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
    @RequestMapping(value = "/api/v1/missions/{missionType}/{mid}", method = POST)
    public ResponseEntity<MissionJoinResponse> joinMission(
            @PathVariable("missionType") String missionType,
            @PathVariable("mid") String mid,
            @RequestParam("tz") String timezone,
            @RequestAttribute("uid") String uid) {

        ZoneId zone = createZone(timezone);
        if (zone == null) {
            return ResponseEntity.badRequest().body(
                    new MissionJoinResponse.Error("unsupported timezone"));
        }

        MissionJoinResult result = missionService.joinMission(uid, missionType, mid, zone);
        if (result instanceof MissionJoinResult.Success) {
            MissionJoinResult.Success successResult = (MissionJoinResult.Success) result;
            return ResponseEntity.ok(new MissionJoinResponse.Success(successResult));

        } else {
            MissionJoinResult.Error errorResult = (MissionJoinResult.Error) result;
            return ResponseEntity.status(errorResult.getCode())
                    .body(new MissionJoinResponse.Error(errorResult.getMessage()));
        }
    }

    @RequestMapping(value = "/api/v1/missions/{missionType}/{mid}", method = DELETE)
    public ResponseEntity<MissionQuitResponse> quitMission(
            @PathVariable("missionType") String missionType,
            @PathVariable("mid") String mid,
            @RequestAttribute("uid") String uid) {
        return missionService.quitMission(uid, missionType, mid).toEntityResponse();
    }

    /**
     * Check-in missions that are interest in the give ping
     *
     * Response (daily mission as example)
     * {
     *     "result": [
     *          "mid": "3zpBONndZxBE76J6ZJl1",
     *          "joinDate": 1567572602095,
     *          "missionType": "mission_daily",
     *          "progress": {
     *              "currentDayCount": 1
     *          }
     *     ]
     * }
     *
     * Invalid timezone
     * {
     *     "error": "unsupported timezone"
     * }
     *
     * @param ping ping used in Firebase Analytics
     * @param timezone user's timezone in
     */
    @RequestMapping(value = "/api/v1/ping/{ping}", method = PUT)
    public ResponseEntity<MissionCheckInResponse> checkInMissionsByPing(
            @PathVariable("ping") String ping,
            @RequestParam("tz") String timezone,
            @RequestAttribute("uid") String uid) {

        ZoneId zone = createZone(timezone);
        if (zone == null) {
            return ResponseEntity.badRequest()
                    .body(new MissionCheckInResponse.Error("unsupported timezone"));
        }

        log.info("ping={}, timezone={}", ping, zone);

        List<MissionCheckInResult> results = missionService.checkInMissions(uid, ping, zone);

        return ResponseEntity.ok(new MissionCheckInResponse.Success(results));
    }

    private ZoneId createZone(String timezone) {
        try {
            return ZoneId.of(timezone);

        } catch (DateTimeException e) {
            log.info("unsupported timezone=" + timezone);
            return null;
        }
    }
}
