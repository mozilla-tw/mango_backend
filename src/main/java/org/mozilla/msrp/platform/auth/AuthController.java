package org.mozilla.msrp.platform.auth;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.msrp.platform.PlatformApplication;
import org.mozilla.msrp.platform.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
public class AuthController {
    private AuthRepository authRepository;

    @Autowired
    public AuthController(AuthRepository repo) {
        authRepository = repo;
    }

    @RequestMapping("/done")
    String done(@RequestParam(value = "jwt") String jwt) {
        return this.toString() + "jwt is here [" + jwt + "], closing the webview";
    }


    @RequestMapping("/login")
    String login(@RequestParam(value = "code") String code,
                 @RequestParam(value = "state") String oldFbUid,
                 HttpServletResponse httpResponse) throws IOException, JSONException {

        // read system settings
        String fxAclientId = PlatformApplication.FXA_CLIENT_ID;
        String fxTokenEp = PlatformApplication.FXA_EP_TOKEN;
        String fxVerifyEp = PlatformApplication.FXA_EP_VERIFY;
        String fxAsecret = PlatformApplication.FXA_CLIENT_SECRET;


        try {
            // token: fxCode -> fxToken
            JSONObject fxTokenJson = new JSONObject()
                    .put("client_id", fxAclientId)
                    .put("grant_type", "authorization_code")
                    .put("ttl", 3600)
                    .put("client_secret", fxAsecret)
                    .put("code", code);
            // println("fxTokenJson===$fxTokenJson")
            String fxTokenRes = HttpUtil.post(fxTokenEp, fxTokenJson);
            // println("fxTokenRes===$fxTokenRes")

            JSONObject fxJsonObject = new JSONObject(fxTokenRes);
            String fxToken = fxJsonObject.getString("access_token");
            //println("fxToken===$fxToken")

            // verify: token -> fxuid
            JSONObject verifyJson = new JSONObject();
            verifyJson.put("token", fxToken);
            String fxVerifyRes = HttpUtil.post(fxVerifyEp, verifyJson);
            String fxUid = new JSONObject(fxVerifyRes).getString("user");
//            println("fxUid===$fxUid")

            authRepository.promoteUserDocument(oldFbUid, fxUid);

            // create custom token (jwt) for Firebase client SDK
            HashMap<String, String> additionalClaims = new HashMap<String, String>();
            additionalClaims.put("fxuid", fxUid);
            additionalClaims.put("oldFbUid", oldFbUid);
            String customToken = authRepository.createCustomToken(fxUid, additionalClaims);
            httpResponse.sendRedirect("/done?jwt=$customToken&at=$fxToken&email=nechen@mozilla.com");

            return "done";

        } catch (Exception e) {
//            println("Exceptiontion===$e")
            return "exception===" + e.getLocalizedMessage();
        }
    }
}
