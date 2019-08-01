package org.mozilla.msrp.platform.auth

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.msrp.platform.util.HttpUtil
import org.mozilla.msrp.platform.PlatformApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import java.io.IOException

import org.springframework.web.bind.annotation.RequestMethod.GET
import java.text.SimpleDateFormat
import java.util.*

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

            promoteUserDocument(oldFbUid, fxUid)

            // create custom token (jwt) for Firebase client SDK
            val additionalClaims = HashMap<String, Any>()
            additionalClaims["fxuid"] = fxUid
            additionalClaims["oldFbUid"] = oldFbUid
            val auth = FirebaseAuth.getInstance()
            val customToken = auth.createCustomToken(fxUid, additionalClaims)
            httpResponse.sendRedirect("/done?jwt=$customToken&at=$fxToken&email=nechen@mozilla.com")

            return "done"

        } catch (e: Exception) {
            println("Exception===$e")
            return "exception===" + e.localizedMessage
        }
    }

    private fun promoteUserDocument(oldFbUid: String, fxUid: String) {
        val db = FirestoreClient.getFirestore()
        val users = db.collection("users")

        // if there's an user document with uid == fxuid , remove current one
        val fxUserDocumentId = findUserDocumentIdByUid(users, fxUid)
        println("fxUserDocumentId======$fxUserDocumentId")

        if (fxUserDocumentId != null) {
            val dying = findUserDocumentIdByUid(users, oldFbUid)
            println("dying======$dying")

            if (dying != null) {
                users.document(dying).delete()
                return
            }
        }

        val documentId = findUserDocumentIdByUid(users, oldFbUid) ?: return

        println("oldFbUid======$oldFbUid")
        println("oldFbUid======$fxUid")
        println("documentId======$documentId")

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val docData = HashMap<String, Any>()
        docData["isfxa"] = true
        docData["uid"] = fxUid
        docData["update_date"] = currentDate
        docData["update_reason"] = "sign_in_fxa"
        users.document(documentId).set(docData, SetOptions.merge())
    }

    private fun findUserDocumentIdByUid(users: CollectionReference, fxUid: String): String? {
        val documents = users.whereEqualTo("uid", fxUid).get().get().documents
        if (documents.size >= 1) {
            return documents[0].id
        } else {
            return null
        }
    }
}