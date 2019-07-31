package org.mozilla.msrp.platform;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jdk.nashorn.internal.runtime.JSONFunctions;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class AuthController {


    @RequestMapping(value = "/login", method = GET)
    public String login(@RequestParam(value = "code") String authorization_code, HttpServletResponse httpResponse) throws IOException, JSONException {

        String fxAclientId = PlatformApplication.FXA_CLIENT_ID;
        String fxAtokenEp = PlatformApplication.FXA_EP_TOKEN;
        String fxAverifyEp = PlatformApplication.FXA_EP_VERIFY;
        String fxAsecret = PlatformApplication.FXA_CLIENT_SECRET;

        JSONObject jsonObj = new JSONObject()
                .put("client_id", fxAclientId)
                .put("grant_type", "authorization_code")
                .put("ttl", 3600)
                .put("client_secret", fxAsecret)
                .put("code", authorization_code);

        System.out.println("jsonObj===" + jsonObj);
        try {
            URL url = new URL(fxAtokenEp);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            byte[] outputBytes = jsonObj.toString().getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(outputBytes);
            os.close();
            System.out.println("connection.getResponseCode()===" + connection.getResponseCode());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
                System.out.println("1===");
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        System.out.println("line===" + responseLine);

                        response.append(responseLine.trim());
                    }
                    System.out.println("res:" + response.toString());
                    return response.toString();
                }

            } else {
                return "fail:" + connection.getResponseMessage();
            }

        } catch (Exception e) {
            System.out.println("Exception===" + e.toString());

            return "exception:" + e.getLocalizedMessage();
        }
    }
}