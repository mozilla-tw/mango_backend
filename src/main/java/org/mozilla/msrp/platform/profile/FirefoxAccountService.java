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

    private static String API_AUTH_RESPONSE_KEY_ACCESS_TOKEN = "access_token";
    private static String API_AUTH_REQUEST_KEY_CLIENT_ID = "client_id";
    private static String API_AUTH_REQUEST_KEY_GRANT_TYPE = "grant_type";
    private static String API_AUTH_REQUEST_VALUE_GRANT_TYPE = "authorization_code";
    private static String API_AUTH_REQUEST_KEY_TTL = "ttl";
    private static int API_AUTH_REQUEST_VALUE_TTL = 3600;
    private static String API_AUTH_REQUEST_KEY_CLIENT_SECRET = "client_secret";
    private static String API_AUTH_REQUEST_KEY_CODE = "code";


    @Inject
    FirefoxAccountServiceInfo firefoxAccountServiceInfo;

    @Inject
    ObjectMapper mapper;

    String authorization(String code) throws IOException, JSONException {
        // token: fxCode -> fxToken
        JSONObject fxTokenJson = new JSONObject()
                .put(API_AUTH_REQUEST_KEY_CLIENT_ID, firefoxAccountServiceInfo.getClientId())
                .put(API_AUTH_REQUEST_KEY_GRANT_TYPE, API_AUTH_REQUEST_VALUE_GRANT_TYPE)
                .put(API_AUTH_REQUEST_KEY_TTL, API_AUTH_REQUEST_VALUE_TTL)
                .put(API_AUTH_REQUEST_KEY_CLIENT_SECRET, firefoxAccountServiceInfo.getClientSecret())
                .put(API_AUTH_REQUEST_KEY_CODE, code);

        String fxTokenRes = HttpUtil.post(firefoxAccountServiceInfo.getApiToken(), fxTokenJson, null);
        JSONObject fxJsonObject = new JSONObject(fxTokenRes);
        return fxJsonObject.getString(API_AUTH_RESPONSE_KEY_ACCESS_TOKEN);
    }

    FxAProfileResponse profile(String fxToken) throws JSONException, IOException {
        String fxaProfileRes = HttpUtil.post(firefoxAccountServiceInfo.getApiProfile(), null, fxToken);
        return mapper.readValue(fxaProfileRes, FxAProfileResponse.class);
    }

}
