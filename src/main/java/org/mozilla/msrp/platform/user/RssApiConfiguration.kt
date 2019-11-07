package org.mozilla.msrp.platform.user

import com.google.cloud.firestore.Firestore
import org.mozilla.msrp.platform.util.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import java.util.concurrent.ExecutionException
import javax.inject.Named

@Configuration
open class RssApiConfiguration {

    private val log = logger()

    @Bean
    @DependsOn("Firestore")
    open fun provideRssApiInfo(firestore: Firestore): RssApiInfo? {
        log.info(" --- Bean Creation RssApiInfo ---")
        try {
            // asynchronously retrieve all users
            val query = firestore.collection("settings").get()
            val querySnapshot = query.get()
            val documents = querySnapshot.documents
            for (document in documents) {

                val google = document.getString("rss_google_api")
                        ?: throw IllegalStateException("RSS API for Google not set")
                val liputan6 = document.getString("rss_liputan6_api")
                        ?: throw IllegalStateException("RSS API for Liputan6 not set")
                val detik = document.getString("rss_detik_api")
                        ?: throw IllegalStateException("RSS API for Detik not set")

                log.info("Get RssApiInfo settings --- success ---$google/$liputan6/$detik")
                return RssApiInfo(google, liputan6, detik)
            }
        } catch (e: InterruptedException) {
            log.error("Get RssApiInfo settings -- failed :$e")
        } catch (e: ExecutionException) {
            log.error("Get RssApiInfo settings -- failed :$e")
        }

        log.error("Get RssApiInfo settings -- failed, shouldn't reach this line --- ")
        return null
    }
}

class RssApiInfo(
        val google: String,
        val liputan6: String,
        val detik: String
)
