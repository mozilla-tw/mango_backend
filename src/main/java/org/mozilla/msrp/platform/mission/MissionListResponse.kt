package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonValue

class MissionListResponse(
    @JsonValue val mission: List<Mission>
)
