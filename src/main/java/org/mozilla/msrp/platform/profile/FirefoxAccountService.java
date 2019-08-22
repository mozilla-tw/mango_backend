package org.mozilla.msrp.platform.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.msrp.platform.util.HttpUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
class FirefoxAccountService {

    @Inject
    FirefoxAccountServiceInfo firefoxAccountServiceInfo;

    String authorization(String code) throws IOException, JSONException {
        // token: fxCode -> fxToken
        JSONObject fxTokenJson = new JSONObject()
                .put("client_id", firefoxAccountServiceInfo.getClientId())
                .put("grant_type", "authorization_code")
                .put("ttl", 3600)
                .put("client_secret", firefoxAccountServiceInfo.getClientSecret())
                .put("code", code);

        String fxTokenRes = HttpUtil.post(firefoxAccountServiceInfo.getApiToken(), fxTokenJson, null);
        JSONObject fxJsonObject = new JSONObject(fxTokenRes);
        return fxJsonObject.getString("access_token");
    }

    FxAProfileResponse profile(String fxToken) throws JSONException, IOException {
        String fxaProfileRes = HttpUtil.post(firefoxAccountServiceInfo.getApiProfile(), null, fxToken);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(fxaProfileRes, FxAProfileResponse.class);
    }

}
