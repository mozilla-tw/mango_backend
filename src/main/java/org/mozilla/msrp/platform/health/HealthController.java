package org.mozilla.msrp.platform.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RequestMapping("/health")
    ResponseEntity isHealthy() {
        return ResponseEntity.ok("healthy");
    }
}