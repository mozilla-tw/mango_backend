package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MissionCheckInResponse (
    val result: List<MissionCheckInResult>? = null,
    val error: String? = null
) {
    companion object {
        @JvmStatic
        fun error(msg: String): MissionCheckInResponse {
            return MissionCheckInResponse(error = msg)
        }

        @JvmStatic
        fun body(result: List<MissionCheckInResult>): MissionCheckInResponse {
            return MissionCheckInResponse(result = result)
        }
    }
}
