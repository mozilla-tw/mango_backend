package org.mozilla.msrp.platform.mission

sealed class MissionCreateResponse {
    class Success(val result: List<MissionCreateResultItem>) : MissionCreateResponse()
    class Error(val result: List<MissionCreateFailedItem>) : MissionCreateResponse()
}