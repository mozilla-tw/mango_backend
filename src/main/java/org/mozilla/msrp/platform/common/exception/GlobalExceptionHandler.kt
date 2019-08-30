package org.mozilla.msrp.platform.common.exception

import org.mozilla.msrp.platform.util.logger
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    val log = logger()

    /**
     * Default handler
     */
    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: Exception): ResponseEntity<ErrorResponseMessage> {
        val msg = NestedExceptionUtils.buildMessage("", e)
        log.info("global uncaught exception: {}", msg)
        return ResponseEntity(
                ErrorResponseMessage("internal server error"),
                HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}