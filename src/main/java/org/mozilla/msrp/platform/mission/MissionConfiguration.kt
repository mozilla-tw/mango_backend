package org.mozilla.msrp.platform.mission

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.Locale

@Configuration
open class MissionConfiguration {

    @Bean
    open fun missionMessageSource(): ResourceBundleMessageSource {
        return ResourceBundleMessageSource().apply {
            setBasename("messages/missions")
            setDefaultEncoding(Charsets.UTF_8.name())
        }
    }

    @Bean
    open fun locale(): Locale {
        return LocaleContextHolder.getLocale()
    }
}
