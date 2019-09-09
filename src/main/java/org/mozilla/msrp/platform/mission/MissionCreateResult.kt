package org.mozilla.msrp.platform.mission

data class MissionCreateResult(
        val mid: String,
        val title: String,
        val description: String,
        val expiredDate: Long,
        val events: List<String>,
        val endpoint: String,
        val min_version: Int
)
