package org.mozilla.msrp.platform.auth;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.msrp.platform.PlatformApplication;
import org.mozilla.msrp.platform.util.HttpUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;

/*
* Firefox Account endpoint API. Current we are using stable dev.
* See document: https://developer.mozilla.org/en-US/docs/Mozilla/Tech/Firefox_Accounts/Introduction#OAuth_2.0_API
* and API https://github.com/mozilla/fxa-auth-server/blob/master/fxa-oauth-server/docs/api.md#post-v1verify
*
*
* */
@Service
class FirefoxAccountServiceImpl {

    String fxAclientId = PlatformApplication.FXA_CLIENT_ID;
    String fxTokenEp = PlatformApplication.FXA_EP_TOKEN;
    String fxVerifyEp = PlatformApplication.FXA_EP_VERIFY;
    String fxAsecret = PlatformApplication.FXA_CLIENT_SECRET;

    // use authorization code to exchange for access token
    String authorization(String code) throws IOException, JSONException {
        // token: fxCode -> fxToken
        JSONObject fxTokenJson = new JSONObject()
                .put("client_id", fxAclientId)
                .put("grant_type", "authorization_code")
                .put("ttl", 3600)
                .put("client_secret", fxAsecret)
                .put("code", code);
        // println("fxTokenJson===$fxTokenJson")
        String fxTokenRes = HttpUtil.post(fxTokenEp, fxTokenJson);
        JSONObject fxJsonObject = new JSONObject(fxTokenRes);
        return fxJsonObject.getString("access_token");
    }

    // use fx access token to get fx uid, could be combine with /profile endpoint
    String verify(String fxToken) throws JSONException, IOException {

        JSONObject verifyJson = new JSONObject();
        verifyJson.put("token", fxToken);
        String fxVerifyRes = HttpUtil.post(fxVerifyEp, verifyJson);
        return new JSONObject(fxVerifyRes).getString("user");
    }

    // use fx access token to get user email.
    String profile(String bearer) {

        return "dummy@email.com";
    }

}
