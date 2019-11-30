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


    @GetMapping("/alert")
    ResponseEntity alert() {
        Metrics.event(Metrics.EVENT_USER_SUSPENDED, "");
        Metrics.event(Metrics.EVENT_USER_BIND_FAIL, "");
        Metrics.event(Metrics.EVENT_REDEEM_FAIL, "");
        Metrics.event(Metrics.EVENT_REDEEM_CONSUMED, "");
        Metrics.event(Metrics.EVENT_MISSION_JOINED, "");
        Metrics.event(Metrics.EVENT_MISSION_CHECK_IN, "");
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            // mock slow request, do nothing
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("fake alert");
    }

}