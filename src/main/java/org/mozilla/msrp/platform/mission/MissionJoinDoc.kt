package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.annotation.JsonValue

data class MissionJoinDoc(
        var uid: String = "",
        var missionType: String = "",
        var mid: String = "",
        var status: JoinStatus = JoinStatus.New
)

enum class JoinStatus(@JsonValue val status: Int) {
    New(0),
    Joined(1),
    Complete(2),
    Redeemed(3)
}

fun JoinStatus.canTransferToJoin(): Boolean {
    return status == JoinStatus.New.status
}
