package org.mozilla.msrp.platform.firestore

import org.springframework.web.servlet.HandlerInterceptor

import javax.inject.Named
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Named
class FirestoreInterceptor : HandlerInterceptor {

    @Throws(Exception::class)
    override fun preHandle(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            handler: Any?
    ): Boolean {
        FirestoreReadWriteCounter.reset()
        return true
    }

    @Throws(Exception::class)
    override fun afterCompletion(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            handler: Any?, ex: Exception?
    ) {
        FirestoreReadWriteCounter.consume()
    }
}
