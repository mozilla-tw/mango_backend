package org.mozilla.msrp.platform.profile

import org.json.JSONException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

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
    @Named("FxaProfile") var profileClient: FirefoxAccountClient) {


    @Inject
    private lateinit var firefoxAccountServiceInfo: FirefoxAccountServiceInfo


    @Throws(IOException::class, JSONException::class)
    fun token(authorizationCode: String): String? {
        // token: fxCode -> fxToken
        val clientId = firefoxAccountServiceInfo.clientId

        val clientSecret = firefoxAccountServiceInfo.clientSecret

        val fxaTokenRequest = FxaTokenRequest(
            clientId,
            clientSecret,
            authorizationCode)

        authClient.token(fxaTokenRequest).execute().let {
            val body = it.body()
            if (it.isSuccessful && body != null) {
                return body.access_token

            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "error in Fxa token api ")
            }
        }
    }

    @Throws(JSONException::class, IOException::class)
    fun profile(fxToken: String): FxaProfileResponse {
        profileClient.profile("Bearer: $fxToken").execute().let {
            val body = it.body()
            if (it.isSuccessful && body != null) {
                return body

            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "error in Fxa profile api ")
            }
        }
    }
}
