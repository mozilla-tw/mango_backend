package org.mozilla.msrp.platform.profile;

import org.json.JSONObject;

import java.io.IOException;

interface Retrofit {
    String load(JSONObject fxTokenJson) throws IOException;
}