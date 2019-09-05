package org.mozilla.msrp.platform.profile

import okhttp3.ResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing
import org.mozilla.msrp.platform.common.mock
import org.mozilla.msrp.platform.common.onRetrofitExecute
import org.springframework.http.HttpStatus
import retrofit2.Call
import retrofit2.Response


class FirefoxAccountServiceTest {

    lateinit var firefoxAccountService: FirefoxAccountService

    @Mock
    lateinit var authClient: FirefoxAccountClient

    @Mock
    lateinit var profileClient: FirefoxAccountClient

    @Before
    fun setUP() {
        authClient = mock(FirefoxAccountClient::class.java)
        profileClient = mock(FirefoxAccountClient::class.java)
        firefoxAccountService = FirefoxAccountService(
            authClient,
            profileClient,
            FirefoxAccountServiceInfo("", "", "", "")
        )

    }

    @Test
    fun getTokenSuccess() {

        val fxaTokenRequest = firefoxAccountService.genFxaTokenRequest("")

        val accessToken = "token"

        onRetrofitExecute(authClient.token(fxaTokenRequest))
            ?.thenReturn(Response.success(FxaTokenResponse(accessToken, "")))

        val response = firefoxAccountService.token(fxaTokenRequest)

        assertTrue(response == accessToken)

    }

    @Test
    fun getTokenFail() {

        val fxaTokenRequest = firefoxAccountService.genFxaTokenRequest("")

        onRetrofitExecute(authClient.token(fxaTokenRequest))
            ?.thenReturn(Response.error(400, mock(ResponseBody::class.java)))

        val response = firefoxAccountService.token(fxaTokenRequest)

        assertTrue(response == null)

    }

    @Test
    fun getProfileSuccess() {
        val profileResponse = FxaProfileResponse()

        val bearer = "bear i-am-a-fake-bearer"

        onRetrofitExecute(profileClient.profile(bearer))
            ?.thenReturn(Response.success(profileResponse))

        val response = firefoxAccountService.profile(bearer)

        assertTrue(response == profileResponse)
    }

    @Test
    fun getProfileFail() {

        val bearer = "bear i-am-a-fake-bearer"

        onRetrofitExecute(profileClient.profile(bearer))
            ?.thenReturn(Response.error(400, mock(ResponseBody::class.java)))

        val response = firefoxAccountService.profile(bearer)

        assertTrue(response == null)
    }
}
