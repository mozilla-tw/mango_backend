package org.mozilla.msrp.platform.mission

data class MissionJoinResponse(
        var mid: String,
        val status: JoinStatus
)
