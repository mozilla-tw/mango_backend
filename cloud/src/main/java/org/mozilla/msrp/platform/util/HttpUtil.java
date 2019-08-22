package org.mozilla.msrp.platform.util;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {


    @NotNull
    public static String post(String endpoint, JSONObject jsonObj, String bearer) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");

        if (jsonObj != null) {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            byte[] outputBytes = jsonObj.toString().getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(outputBytes);
            os.close();
        }
        if (bearer != null) {
            connection.setRequestProperty("Authorization", "Bearer " + bearer);
            System.out.println("bearer===" + bearer);

        }

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
