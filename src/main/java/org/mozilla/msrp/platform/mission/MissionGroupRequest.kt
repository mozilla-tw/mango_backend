package org.mozilla.msrp.platform.mission

class MissionGroupRequest {
    lateinit var missions: List<MissionGroupItemData>
}

class MissionGroupItemData {
    lateinit var endpoint: String
}