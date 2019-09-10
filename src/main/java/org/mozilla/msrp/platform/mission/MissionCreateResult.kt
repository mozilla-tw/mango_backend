package org.mozilla.msrp.platform.mission

sealed class MissionCreateResult {
    class Success(
            val mid: String,
            val title: String,
            val description: String,
            val expiredDate: Long,
            val events: List<String>,
            val endpoint: String,
            val minVersion: Int,
            val missionParams: Map<String, Any>
    ) : MissionCreateResult()

    class Error(val missionName: String, val error: String) : MissionCreateResult()
}
