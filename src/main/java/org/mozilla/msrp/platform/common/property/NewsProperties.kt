package org.mozilla.msrp.platform.common.property

import lombok.Data
import lombok.extern.log4j.Log4j2
import org.mozilla.msrp.platform.util.logger
import org.springframework.boot.context.properties.ConfigurationProperties

import javax.annotation.PostConstruct
import javax.inject.Named

/**
 * Type-safe configuration properties which are setup in application.yml file.
 */
@Named
@ConfigurationProperties("news")
class NewsProperties {
    private val log = logger()

    var cacheSize: Long = 100L
    var cacheTtl: Long = 15L

    @PostConstruct
    fun printProperties() {
        log.info("NewsProperties is initialized: {}", toString())
    }
}
