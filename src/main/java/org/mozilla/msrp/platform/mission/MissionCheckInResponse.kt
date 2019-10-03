package org.mozilla.msrp.platform.mission

sealed class MissionCheckInResponse {
    class Success(val result: MutableList<MissionListItem>) : MissionCheckInResponse()
    class Error(val msg: String) : MissionCheckInResponse()
}
