package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonValue

class MissionCheckInResult(
        @JsonValue val fields: Map<String, Any>
)
