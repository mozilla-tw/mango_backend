package org.mozilla.msrp.platform.common

import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import java.util.*

fun MessageSource.getMessageOrNull(id: String, locale: Locale, vararg args: String = emptyArray()): String? {
    return try {
        getMessage(id, args, locale)
    } catch (e: NoSuchMessageException){
        null
    }
}

fun MessageSource.getMessageOrEmpty(id: String, locale: Locale, vararg args: String = emptyArray()): String {
    return try {
        getMessage(id, args, locale)
    } catch (e: NoSuchMessageException){
        ""
    }
}
