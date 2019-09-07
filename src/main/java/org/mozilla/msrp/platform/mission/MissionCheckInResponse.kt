package org.mozilla.msrp.platform.mission

sealed class MissionCheckInResponse {
    class Success(val result: List<MissionCheckInResult>) : MissionCheckInResponse()
    class Error(val msg: String) : MissionCheckInResponse()
}
