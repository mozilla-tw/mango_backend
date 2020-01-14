package org.mozilla.msrp.platform.common.property;

import javax.inject.Named;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Named
public class PlatformProperties {

    private final Environment env;

    public PlatformProperties(Environment env) {this.env = env;}

    @Nullable
    public String getConfigDbFirebaseProjectId() {
        return env.getProperty("CONFIG_DB_GCP_PROJECT_ID");
    }
}
