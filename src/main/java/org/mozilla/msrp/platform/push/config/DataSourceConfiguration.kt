package org.mozilla.msrp.platform.push.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.mozilla.msrp.platform.push.config.DatabaseInfo
import org.springframework.context.annotation.Bean
import javax.inject.Named

@Named
class DataSourceConfiguration {

    @Bean(destroyMethod = "close")
    fun provideDataSource(databaseInfo: DatabaseInfo): HikariDataSource {
        val config = HikariConfig()

        config.jdbcUrl = java.lang.String.format("jdbc:postgresql:///%s", databaseInfo.databaseName)
        config.username = databaseInfo.username // e.g. "root", "postgres"
        config.password = databaseInfo.password // e.g. "my-password"
        config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
        config.addDataSourceProperty("cloudSqlInstance", databaseInfo.instanceConnectionName)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(10000); // 10 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes


        return HikariDataSource(config)
    }

}
