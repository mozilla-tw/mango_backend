package org.mozilla.msrp.platform.user.data

data class UserActivityDoc(
        val userDocId: String,
        val timestamp: Long,
        val action: String,
        val version: Int = 1
)