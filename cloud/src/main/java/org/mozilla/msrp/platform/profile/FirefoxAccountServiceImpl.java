package org.mozilla.msrp.platform.profile;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.msrp.platform.PlatformApplication;
import org.mozilla.msrp.platform.util.HttpUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
class FirefoxAccountServiceImpl {

    String fxAclientId = PlatformApplication.FXA_CLIENT_ID;
    String fxTokenEp = PlatformApplication.FXA_EP_TOKEN;
    String fxVerifyEp = PlatformApplication.FXA_EP_VERIFY;
    String fxAsecret = PlatformApplication.FXA_CLIENT_SECRET;


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

    String verify(String fxToken) throws JSONException, IOException {

        JSONObject verifyJson = new JSONObject();
        verifyJson.put("token", fxToken);
        String fxVerifyRes = HttpUtil.post(fxVerifyEp, verifyJson);
        return new JSONObject(fxVerifyRes).getString("user");
    }

    String profile(String bearer) {

        return "dummy@email.com";
    }

}
