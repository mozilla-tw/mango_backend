package org.mozilla.msrp.platform.mission

sealed class MissionCreateResult {
    class Success(val results: List<MissionCreateResultItem>) : MissionCreateResult()
    class Error(val results: List<MissionCreateFailedItem>) : MissionCreateResult()
}

data class MissionCreateResultItem(
        val mid: String,
        val title: String,
        val description: String,
        val startDate: String,
        val joinStartDate: String,
        val joinEndDate: String,
        val expiredDate: String,
        val events: List<String>,
        val endpoint: String,
        val minVersion: Int,
        val missionParams: Map<String, Any>,
        val rewardType: String,
        val joinQuota: Int
)

data class MissionCreateFailedItem(
        val missionName: String,
        val error: String
)
