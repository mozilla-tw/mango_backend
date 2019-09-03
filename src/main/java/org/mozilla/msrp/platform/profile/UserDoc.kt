package org.mozilla.msrp.platform.profile.data

import java.util.*

data class UserDoc(
        val firebase_uid: String,
        val firefox_uid: String?,
        val email: String,
        val created_timestamp: Long,
        val updated_timestamp: Long,
        val version: Int = 1,
        val uid: String = UUID.randomUUID().toString())
