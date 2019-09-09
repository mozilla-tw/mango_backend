package org.mozilla.msrp.platform.mission

sealed class MissionJoinResponse {
    class Success(val mid: String, val status: JoinStatus) : MissionJoinResponse()
    class Error(val error: String) : MissionJoinResponse()
}
