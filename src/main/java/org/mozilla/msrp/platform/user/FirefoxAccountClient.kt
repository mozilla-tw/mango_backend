package org.mozilla.msrp.platform.user

import retrofit2.Call
import retrofit2.http.*

/**
 * FirefoxAccountClient talks to Firefox Account endpoints
 *
 */
interface FirefoxAccountClient {
    @POST("v1/token")
    fun token(@Body request: FxaTokenRequest): Call<FxaTokenResponse>

    @GET("v1/profile")
    fun profile(@Header("Authorization") bearer: String): Call<FxaProfileResponse>
}