package org.mozilla.msrp.platform.user

import okhttp3.ResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mozilla.msrp.platform.common.onRetrofitExecute
import retrofit2.Response


class FirefoxAccountServiceTest {

    lateinit var firefoxAccountService: FirefoxAccountService

    @Mock
    lateinit var authClient: FirefoxAccountClient

    @Mock
    lateinit var profileClient: FirefoxAccountClient

    @Before
    fun setUP() {

        MockitoAnnotations.initMocks(this)

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

        val bearer = "Bearer i-am-a-fake-bearer"

        onRetrofitExecute(profileClient.profile(bearer))
            ?.thenReturn(Response.success(profileResponse))

        val response = firefoxAccountService.profile(bearer)

        assertTrue(response == profileResponse)
    }

    @Test
    fun getProfileFail() {

        val bearer = "Bearer i-am-a-fake-bearer"

        onRetrofitExecute(profileClient.profile(bearer))
            ?.thenReturn(Response.error(400, mock(ResponseBody::class.java)))

        val response = firefoxAccountService.profile(bearer)

        assertTrue(response == null)
    }
}
