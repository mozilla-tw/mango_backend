package org.mozilla.msrp.platform.push.util

import org.mozilla.msrp.platform.metrics.Metrics

class PushAdminMetrics {
    companion object {
        const val PUSH_STMO_ERROR = "push_stmo_error"
        const val PUSH_RECIPEINT_NO_TOKEN = "push_recipeint_no_token"
        const val ENQUEUE_PUBSUB_EXCEPTION = "enqueue_pubsub_exception"
        const val ENQUEUE_PUBSUB_FAILURE = "enqueue_pubsub_failure"
        const val ENQUEUE_PUBSUB_RESULT = "enqueue_pubsub_result"
        const val LOG_PUBSUB_ERROR_QUERY = "log_pubsub_error_query"
        const val LOG_PUBSUB_ERROR_INIT = "log_pubsub_error_init"
        const val LOG_PUBSUB_ERROR_ENEUEUE = "log_pubsub_error_eneueue"
        const val LOG_PUBSUB_SUCCESS = "log_pubsub_success"

        fun event(key: String, value: String = "") {
            Metrics.event(key, value)
        }
    }
}