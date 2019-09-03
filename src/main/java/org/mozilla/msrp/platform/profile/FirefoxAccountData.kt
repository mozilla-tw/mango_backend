package org.mozilla.msrp.platform.profile


class FxaTokenResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val scope: String? = null,
    val expires_in: Int? = null,
    val auth_at: Long? = null
)

class FxaTokenRequest(
    val client_id: String,
    val client_secret: String,
    val code: String,
    val grant_type: String = "authorization_code",
    val ttl: Int = 3600)

class FxaProfileResponse(
    val email: String? = null,
    val locale: String? = null,
    val amrValues: List<String>? = null,
    val twoFactorAuthentication: Boolean? = null,
    val uid: String? = null,
    val avatar: String? = null,
    val avatarDefault: Boolean? = null
)
