package org.mozilla.msrp.platform.mission

sealed class MissionListResponse {
    class Success(val result: List<MissionListItem>) : MissionListResponse()
    class Error(val msg: String) : MissionListResponse()
}
