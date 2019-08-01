package org.mozilla.msrp.platform.auth

import org.json.JSONException
import org.json.JSONObject
import org.mozilla.msrp.platform.PlatformApplication
import org.mozilla.msrp.platform.util.HttpUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class AuthController {

    @Autowired
    lateinit var authRepository: AuthRepository

    @RequestMapping("/done", method = [GET])
    fun done(@RequestParam(value = "jwt") jwt: String): String {
        return this.toString() + "jwt is here [" + jwt + "], closing the webview"
    }

    @RequestMapping("/login", method = [GET])
    @Throws(IOException::class, JSONException::class)
    fun login(@RequestParam(value = "code") code: String,
              @RequestParam(value = "state") oldFbUid: String,
              httpResponse: HttpServletResponse): String {

        // read system settings
        val fxAclientId = PlatformApplication.FXA_CLIENT_ID
        val fxTokenEp = PlatformApplication.FXA_EP_TOKEN
        val fxVerifyEp = PlatformApplication.FXA_EP_VERIFY
        val fxAsecret = PlatformApplication.FXA_CLIENT_SECRET


        try {
            // token: fxCode -> fxToken
            val fxTokenJson = JSONObject()
                    .put("client_id", fxAclientId)
                    .put("grant_type", "authorization_code")
                    .put("ttl", 3600)
                    .put("client_secret", fxAsecret)
                    .put("code", code)
            println("fxTokenJson===$fxTokenJson")
            val fxTokenRes = HttpUtil.post(fxTokenEp, fxTokenJson)
            println("fxTokenRes===$fxTokenRes")

            val fxJsonObject = JSONObject(fxTokenRes)
            val fxToken = fxJsonObject.getString("access_token")
            println("fxToken===$fxToken")

            // verify: token -> fxuid
            val verifyJson = JSONObject().run {
                put("token", fxToken)
            }
            val fxVerifyRes = HttpUtil.post(fxVerifyEp, verifyJson)
            val fxUid = JSONObject(fxVerifyRes).getString("user")
            println("fxUid===$fxUid")

            authRepository.promoteUserDocument(oldFbUid, fxUid)

            // create custom token (jwt) for Firebase client SDK
            val additionalClaims = HashMap<String, Any>()
            additionalClaims["fxuid"] = fxUid
            additionalClaims["oldFbUid"] = oldFbUid
            val customToken = authRepository.createCustomToken(fxUid, additionalClaims)
            httpResponse.sendRedirect("/done?jwt=$customToken&at=$fxToken&email=nechen@mozilla.com")

            return "done"

        } catch (e: Exception) {
            println("Exception===$e")
            return "exception===" + e.localizedMessage
        }
    }
}