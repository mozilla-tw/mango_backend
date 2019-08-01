package org.mozilla.msrp.platform

import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.HashMap

import org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
class AuthController {
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
        val fxAtokenEp = PlatformApplication.FXA_EP_TOKEN
        val fxAverifyEp = PlatformApplication.FXA_EP_VERIFY
        val fxAsecret = PlatformApplication.FXA_CLIENT_SECRET


        try {
            // token: code -> token
            val fxaTokenJson = JSONObject()
                    .put("client_id", fxAclientId)
                    .put("grant_type", "authorization_code")
                    .put("ttl", 3600)
                    .put("client_secret", fxAsecret)
                    .put("code", code)
            println("fxaTokenJson===$fxaTokenJson")
            val fxAtokenRes = HttpUtil.post(fxAtokenEp, fxaTokenJson)
            println("fxAtokenRes===$fxAtokenRes")
            val fxJsonObject = JSONObject(fxAtokenRes)
            val fxAtoken = fxJsonObject.getString("access_token")

            // verify: token -> uid
            val verifyJson = JSONObject().run {
                put("token", fxAtoken)
            }
            val fxVerifyRes = HttpUtil.post(fxAverifyEp, verifyJson)
            println("fxVerifyRes===$fxVerifyRes")

            // create custom token (jwt) for Firebase client SDK
            val additionalClaims = HashMap<String, Any>()
            val fxUid = JSONObject(fxVerifyRes).getString("user")
            additionalClaims["fxuid"] = fxUid
            additionalClaims["oldFbUid"] = oldFbUid
            val auth = FirebaseAuth.getInstance()
            val customToken = auth.createCustomToken(fxUid, additionalClaims)
            httpResponse.sendRedirect("/done?jwt=$customToken&at=$fxAtoken&email=nechen@mozilla.com")

            return "done"

        } catch (e: Exception) {
            println("Exception===$e")
            return "exception===" + e.localizedMessage
        }
    }


}