package org.mozilla.msrp.platform.metrics

import org.mozilla.msrp.platform.util.logger

class VerticalMetrics {

    companion object {
        const val EVENT_CACHE_MISSED = "cache_missed"
        const val EVENT_CACHE_EXPIRED = "cache_expired"
        const val EVENT_CACHE_EXCEPTION= "cache_exception"

        @JvmStatic
        fun event(key: String, value: String = "") {
            logger().info("$key==$value")
        }
    }
}