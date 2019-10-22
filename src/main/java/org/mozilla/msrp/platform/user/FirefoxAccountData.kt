package org.mozilla.msrp.platform.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class FxaTokenResponse(
    val access_token: String? = null
)

class FxaTokenRequest(
    val client_id: String,
    val client_secret: String,
    val code: String,
    val grant_type: String = "authorization_code",
    val ttl: Int = 3600)


@JsonIgnoreProperties(ignoreUnknown = true)
class FxaProfileResponse(
    val email: String? = null,
    val uid: String? = null
)
