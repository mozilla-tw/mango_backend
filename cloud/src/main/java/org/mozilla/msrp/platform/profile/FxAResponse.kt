package org.mozilla.msrp.platform.profile

import java.util.*

// TODO: use this class to create user documents using Firebase Authentication Trigger
data class User(val firebase_uid: String, val firefox_uid: String?, val email: String, val created_timestamp: Long, val updated_timestamp: Long, val version: Int = 1, val uid: String = UUID.randomUUID().toString())

data class FxAProfileResponse(
        val email: String,
        val locale: String,
        val amrValues: List<String>,
        val twoFactorAuthentication: Boolean,
        val uid: String,
        val avatar: String,
        val avatarDefault: Boolean
) {
    // a default constructor is required for Jackson
    constructor() : this("", "", listOf(""), false, "", "", false)
}