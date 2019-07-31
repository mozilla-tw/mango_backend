package org.mozilla.msrp.platform;

import com.google.firebase.auth.FirebaseAuthException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

public class HttpUtil {


    @NotNull
    public static String post(String endpoint, JSONObject jsonObj) throws IOException {
        URL url = new URL(endpoint);
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

                return response.toString();
            }

        } else {
            return "fail:" + connection.getResponseMessage();
        }
    }
}
