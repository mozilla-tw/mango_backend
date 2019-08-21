package org.mozilla.msrp.platform.profile;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Configuration
public class FirebaseConfiguration {

    /**
     * TODO: Remove this after we have a appropriate place to globally initialize Firebase
     */
    @Bean
    public FirefoxAccountServiceInfo FirefoxAccountManagerFactory() {
        System.out.println("FirebaseConfiguration  -- started --- ");
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
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {

                String id = document.getString("fxa_client_id");
                String secret = document.getString("fxa_client_secret");
                String token = document.getString("fxa_api_token");
                String profile = document.getString("fxa_api_profile");

                System.out.println("FirebaseConfiguration  -- success --- ");

                return new FirefoxAccountServiceInfo(id, secret, token, profile);
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            System.out.println("FirebaseConfiguration -- failed :" + e.getLocalizedMessage());
        }
        System.out.println("FirebaseConfiguration -- failed, shouldn't reach this line --- ");
        return null;
    }
}
