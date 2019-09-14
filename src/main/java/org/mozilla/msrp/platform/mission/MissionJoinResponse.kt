package org.mozilla.msrp.platform.mission

import org.springframework.http.HttpStatus

sealed class MissionJoinResponse {
    class Success(val result: MissionJoinResult) : MissionJoinResponse()
    class Error(val message: String) : MissionJoinResponse()
}

sealed class MissionJoinResult {
    class Success(val mid: String, val status: JoinStatus) : MissionJoinResult()
    class Error(val message: String, val code: HttpStatus) : MissionJoinResult()
}
