package org.mozilla.msrp.platform.mission

/**
 * (All fields are just draft and are subject to change)
 * Client-facing mission class, this class should contains
 * 1. mid
 * 2. name, description in user's language (if support)
 * 3. status such as join, dropped, etc
 * 4. progress
 */
data class MissionListItem(
        val mid: String,
        val title: String,
        val description: String,
        val endpoint: String,
        val events: List<String>,
        val expiredDate: String,
        val status: JoinStatus,
        val minVersion: Int,
        val progress: Map<String, Any>,
        val important: Boolean
)
