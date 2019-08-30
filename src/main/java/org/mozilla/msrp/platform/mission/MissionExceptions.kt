package org.mozilla.msrp.platform.mission

import com.google.api.core.ApiFuture
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.UncheckedExecutionException
import java.lang.RuntimeException
import java.util.concurrent.CancellationException

class MissionDatabaseException(cause: Throwable) : RuntimeException(cause)

/**
 * This function first convert checked exception into either CancellationException or UncheckedExecutionException,
 * then abstract them into higher-level MissionDatabaseException
 */
fun <T> ApiFuture<T>.getUnchecked(): T {
    return try {
        Futures.getUnchecked(this)

    } catch (e: CancellationException) {
        throw MissionDatabaseException(e)

    } catch (e: UncheckedExecutionException) {
        throw MissionDatabaseException(e)
    }
}
