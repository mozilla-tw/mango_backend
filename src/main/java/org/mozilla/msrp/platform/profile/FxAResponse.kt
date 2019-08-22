package org.mozilla.msrp.platform.profile

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