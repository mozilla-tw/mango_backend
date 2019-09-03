package org.mozilla.msrp.platform.profile;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * FirefoxAccountClient talks to Firefox Account endpoints
 *
 * */
public interface FirefoxAccountClient {
    @POST("v1/token")
    Call<FxaTokenResponse> token(@Body FxaTokenRequest request);

    @GET("v1/profile")
    Call<FxaProfileResponse> profile(@Header("Authorization") String bearer);
}