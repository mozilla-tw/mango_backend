package org.mozilla.msrp.platform.mission

sealed class MissionCheckInResponse2 {
    class Success(val result: List<MissionCheckInResult>) : MissionCheckInResponse2()
    class Error(val msg: String) : MissionCheckInResponse2()
}
