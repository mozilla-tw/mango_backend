package org.mozilla.msrp.platform.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is a helper method for getting Logger in Kotlin classes
 * since @Log4j2 annotation doesn't work with Kotlin
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
