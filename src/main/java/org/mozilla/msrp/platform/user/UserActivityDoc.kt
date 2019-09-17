package org.mozilla.msrp.platform.user.data

data class UserActivityDoc(
        val userDocId: String,
        val timestamp: Long,
        val status: String,
        val version: Int = 1
)