package org.mozilla.msrp.platform.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Log4j2
@Configuration
public class FirefoxAccountConfiguration {

    @Bean
    @DependsOn({"Firestore"})
    public FirefoxAccountServiceInfo firefoxAccountServiceInfo(Firestore firestore) {
        log.info(" --- Bean Creation firefoxAccountServiceInfo ---");
        try {
            // asynchronously retrieve all users
            ApiFuture<QuerySnapshot> query = firestore.collection("settings").get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {

                String id = document.getString("fxa_client_id");
                String secret = document.getString("fxa_client_secret");
                String token = document.getString("fxa_api_token");
                String profile = document.getString("fxa_api_profile");

                log.info("Get FirefoxAccount settings --- success ---:" + id);
                return new FirefoxAccountServiceInfo(id, secret, token, profile);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Get FirefoxAccount settings -- failed :" + e);
        }
        log.error("Get FirefoxAccount settings -- failed, shouldn't reach this line --- ");
        return null;
    }
}
