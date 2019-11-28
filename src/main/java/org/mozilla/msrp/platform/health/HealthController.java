package org.mozilla.msrp.platform.health;

import org.mozilla.msrp.platform.metrics.Metrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    ResponseEntity isHealthy() {
        return ResponseEntity.ok("healthy");
    }


    @GetMapping("/EVENT_USER_SUSPENDED")
    ResponseEntity EVENT_USER_SUSPENDED() {
        Metrics.event(Metrics.EVENT_USER_SUSPENDED, "");
        return ResponseEntity.ok("EVENT_USER_SUSPENDED");
    }


    @GetMapping("/EVENT_USER_BIND_FAIL")
    ResponseEntity EVENT_USER_BIND_FAIL() {
        Metrics.event(Metrics.EVENT_USER_BIND_FAIL, "");
        return ResponseEntity.ok("EVENT_USER_BIND_FAIL");
    }

    @GetMapping("/EVENT_REDEEM_FAIL")
    ResponseEntity EVENT_REDEEM_FAIL() {
        Metrics.event(Metrics.EVENT_REDEEM_FAIL, "");
        return ResponseEntity.ok("EVENT_REDEEM_FAIL");
    }


    @GetMapping("/EVENT_REDEEM_CONSUMED")
    ResponseEntity EVENT_REDEEM_CONSUMED() {
        Metrics.event(Metrics.EVENT_REDEEM_CONSUMED, "");
        return ResponseEntity.ok("EVENT_REDEEM_CONSUMED");
    }


    @GetMapping("/EVENT_MISSION_JOINED")
    ResponseEntity EVENT_MISSION_JOINED() {
        Metrics.event(Metrics.EVENT_MISSION_JOINED, "");
        return ResponseEntity.ok("EVENT_MISSION_JOINED");
    }

    @GetMapping("/EVENT_MISSION_CHECK_IN")
    ResponseEntity EVENT_MISSION_CHECK_IN() {
        Metrics.event(Metrics.EVENT_MISSION_CHECK_IN, "");
        return ResponseEntity.ok("EVENT_MISSION_CHECK_IN");
    }
}