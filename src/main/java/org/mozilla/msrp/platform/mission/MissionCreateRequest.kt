package org.mozilla.msrp.platform.mission

class MissionCreateRequest {
    lateinit var missions: List<MissionCreateData>
}

data class MissionCreateData(
        val missionName: String,
        val titleId: String,
        val descriptionId: String,
        val missionType: String,
        val pings: List<String>,
        val expiredDate: Long,
        val min_version: Int
)