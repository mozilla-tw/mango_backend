package org.mozilla.msrp.platform;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class PlatformApplication {

    public static String FXA_CLIENT_ID;
    public static String FXA_CLIENT_SECRET;
    public static String FXA_EP_AUTH;
    public static String FXA_EP_TOKEN;
    public static String FXA_EP_VERIFY;

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fetchSecretAfterStartup() {
        System.out.println("fetchSecretAfterStartup  -- started --- ");
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setProjectId("rocket-dev01")
                    .build();

            FirebaseApp.initializeApp(options);

            Firestore db = FirestoreClient.getFirestore();

            // asynchronously retrieve all users
            ApiFuture<QuerySnapshot> query = db.collection("settings").get();
            // ...
            // query.get() blocks on response
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                FXA_CLIENT_ID = document.getString("fxa_client_id");
                FXA_CLIENT_SECRET = document.getString("fxa_client_secret");
                FXA_EP_AUTH = document.getString("fxa_ep_auth");
                FXA_EP_TOKEN = document.getString("fxa_ep_token");
                FXA_EP_VERIFY = document.getString("fxa_ep_verify");
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("getSecrets error :" + e.getLocalizedMessage());
        }
        System.out.println("fetchSecretAfterStartup -- ended --- ");

    }


}
