package org.mozilla.msrp.platform.common

import org.springframework.core.env.Environment

val Environment.isDev: Boolean
    get() {
        return this.activeProfiles.any { it == "dev" }
    }

val Environment.isStableDev: Boolean
    get() {
        return this.activeProfiles.any { it == "stable" }
    }

val Environment.isNightly: Boolean
    get() {
        return this.activeProfiles.any { it == "nightly" }
    }

val Environment.isProd: Boolean
    get() {
        return this.activeProfiles.any { it.contains("prod", true) }
    }
