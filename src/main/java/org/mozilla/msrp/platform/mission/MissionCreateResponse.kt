package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonValue

sealed class MissionCreateResponse {
    class Success(val result: List<MissionCreateResult>) : MissionCreateResponse()
    class Error(msg: String) : MissionCreateResponse()
}