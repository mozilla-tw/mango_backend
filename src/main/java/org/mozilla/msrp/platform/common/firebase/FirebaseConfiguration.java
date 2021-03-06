package org.mozilla.msrp.platform.common.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.log4j.Log4j2;
import org.mozilla.msrp.platform.common.property.PlatformProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.IOException;

@Log4j2
@Configuration
public class FirebaseConfiguration {

    /**
     * The Application Default Credentials are available if running in Google App Engine.
     * Otherwise, the environment variable GOOGLE_APPLICATION_CREDENTIALS must be defined pointing to a file defining the credentials.
     *
     * @return an instance of {@link GoogleCredentials}
     */
    @Bean
    public GoogleCredentials googleApplicationCredentials() throws IOException {
        log.info(" --- Bean Creation GoogleCredentials ---");
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        log.info("GoogleCredentials is created successfully.");
        return credentials;
    }

    @Bean("FirebaseApp")
    public FirebaseApp firebaseApp(GoogleCredentials credentials, PlatformProperties platformProperties) {
        log.info(" --- Bean Creation firebaseApp ---");
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info(" --- Bean Creation firebaseApp skip---");

            FirebaseApp firebaseApp = FirebaseApp.getApps().get(0);
            log.info(" --- Bean Creation firebaseApp skip---" + firebaseApp.hashCode());
            firebaseApp.delete();
        }
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId(platformProperties.getFirebaseProjectId())
                .build();
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
        log.info(" --- Bean Creation firebaseApp done---" + firebaseApp.hashCode());

        return firebaseApp;
    }

    @Bean("Firestore")
    public Firestore firestore(FirebaseApp app) {
        log.info(" --- Bean Creation Firestore ---" + app.hashCode());
        Firestore firestore = FirestoreClient.getFirestore();
        log.info(" --- Bean Creation Firestore ---done:" + firestore.hashCode());

        return firestore;
    }

    @Bean
    @DependsOn({"FirebaseApp"})
    public Storage cloudStorage() {
        log.info(" --- Bean Creation cloudStorage ---");

        return StorageOptions.getDefaultInstance().getService();
    }
}
