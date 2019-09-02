package org.mozilla.msrp.platform.common;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * Type-safe configuration properties which are setup in application.yml file.
 */
@Data // getter and setter are mandatory for ConfigurationProperties
@Log4j2
@Named
@ConfigurationProperties("platform")
public class PlatformProperties {
    private String firebaseProjectId;

    @PostConstruct
    public void printProperties() {
        log.info("PlatformProperties is initialized: {}", toString());
    }
}
