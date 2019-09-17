package org.mozilla.msrp.platform.user

import org.json.JSONException
import org.springframework.stereotype.Service

import javax.inject.Inject
import java.io.IOException
import javax.inject.Named

/**
 * FirefoxAccountService prepare for the data to send to FirefoxAccountClient
 *
 * */
@Service
class FirefoxAccountService @Inject constructor(
    @Named("FxaAuth") var authClient: FirefoxAccountClient,
    @Named("FxaProfile") var profileClient: FirefoxAccountClient,
    private var firefoxAccountServiceInfo: FirefoxAccountServiceInfo) {


    @Throws(IOException::class, JSONException::class)
    fun token(fxaTokenRequest: FxaTokenRequest): String? {
        // token: fxCode -> fxToken

        val response = authClient.token(fxaTokenRequest).execute()
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            body.access_token
        } else {
            null
        }
    }

    fun genFxaTokenRequest(authorizationCode: String): FxaTokenRequest {
        val clientId = firefoxAccountServiceInfo.clientId

        val clientSecret = firefoxAccountServiceInfo.clientSecret

        return FxaTokenRequest(
            clientId,
            clientSecret,
            authorizationCode)
    }

    @Throws(JSONException::class, IOException::class)
    fun profile(bearerHeader: String): FxaProfileResponse? {

        val response = profileClient.profile(bearerHeader).execute()

        val body = response.body()

        return if (response.isSuccessful) {
            body
        } else {
            null
        }

    }
}
