package org.mozilla.msrp.platform.push.config

import com.google.cloud.firestore.Firestore
import org.mozilla.msrp.platform.util.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import java.util.concurrent.ExecutionException

@Configuration
open class PostgresDbConfiguration {

    private val log = logger()

    @DependsOn("Firestore")
    @Bean("PostgresDbInfo")
    open fun providePostgresDbInfo(firestore: Firestore): DatabaseInfo? {
        log.info(" --- Bean Creation PostgresDbInfo ---")
        try {
            // asynchronously retrieve all users
            val query = firestore.collection("settings").get()
            val querySnapshot = query.get()
            val documents = querySnapshot.documents
            for (document in documents) {

                val username = document.getString("postgres_username")
                        ?: throw IllegalStateException("postgres_username not set")
                val password = document.getString("postgres_password")
                        ?: throw IllegalStateException("postgres_password not set")
                val databaseName = document.getString("postgres_db_name")
                        ?: throw IllegalStateException("postgres_database_name not set")
                val instanceConnectionName = document.getString("postgres_db_conn")
                        ?: throw IllegalStateException("postgres_db_conn not set")

                log.info("Get PostgresDbInfo settings --- success ---$instanceConnectionName")
                return DatabaseInfo(username, password, databaseName, instanceConnectionName)
            }
        } catch (e: InterruptedException) {
            log.error("Get PostgresDbInfo settings -- failed :$e")
        } catch (e: ExecutionException) {
            log.error("Get PostgresDbInfo settings -- failed :$e")
        }

        log.error("Get PostgresDbInfo settings -- failed, shouldn't reach this line --- ")
        return null
    }
}

class DatabaseInfo(
        val username: String,
        val password: String,
        val databaseName: String,
        val instanceConnectionName: String
)
