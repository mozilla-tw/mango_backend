package org.mozilla.msrp.platform.mission;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MissionConfig {

    /**
     * TODO: Remove this after we have a appropriate place to globally initialize Firebase
     */
    private FirebaseApp firebaseApp() {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setProjectId("rocket-dev01")
                    .build();

            return FirebaseApp.initializeApp(options, "roger_instance");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore(firebaseApp());
    }
}
