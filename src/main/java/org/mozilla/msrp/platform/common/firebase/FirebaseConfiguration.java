package org.mozilla.msrp.platform.common.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.log4j.Log4j2;
import org.mozilla.msrp.platform.PlatformProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Log4j2
@Configuration
public class FirebaseConfiguration {

    /**
     * The Application Default Credentials are available if running in Google Compute Engine.
     * Otherwise, the environment variable GOOGLE_APPLICATION_CREDENTIALS must be defined pointing to a file defining the credentials.
     *
     * @return an instance of {@link GoogleCredentials}
     */
    @Bean
    public GoogleCredentials googleApplicationCredentials() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        log.info("GoogleCredentials is created successfully.");
        return credentials;
    }

    @Bean
    public Firestore firestore(GoogleCredentials credentials, PlatformProperties platformProperties) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId(platformProperties.getFirebaseProjectId())
                .build();
        FirebaseApp.initializeApp(options);
        return FirestoreClient.getFirestore();
    }
}