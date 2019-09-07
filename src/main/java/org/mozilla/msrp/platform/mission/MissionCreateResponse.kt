package org.mozilla.msrp.platform.mission

sealed class MissionCreateResponse {
    class Success(val result: List<MissionCreateResult>) : MissionCreateResponse()
    class Error(val msg: String) : MissionCreateResponse()
}