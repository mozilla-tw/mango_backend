package org.mozilla.msrp.platform.profile.data

data class AccountActivityDoc(
        val userDocId: String,
        val timestamp: Long,
        val action: String,
        val version: Int = 1
)