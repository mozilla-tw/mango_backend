package org.mozilla.msrp.platform.mission

import org.springframework.http.HttpStatus

sealed class MissionJoinResponse {
    class Success(val result: MissionJoinResult) : MissionJoinResponse()
    class Error(val message: String, val reason: Int) : MissionJoinResponse()
}

sealed class MissionJoinResult {
    class Success(val mid: String, val status: JoinStatus) : MissionJoinResult()
    class Error(val message: String, val code: HttpStatus, val reason: JoinFailedReason) : MissionJoinResult()
}

enum class JoinFailedReason(val code: Int) {
    Unknown(0),
    NotOpen(1),
    Closed(2),
    AlreadyJoined(3),
    NoQuota(4),
    NotExist(5)
}
