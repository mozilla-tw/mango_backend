package org.mozilla.msrp.platform.mission

import java.lang.RuntimeException
import java.util.concurrent.ExecutionException

class MissionDatabaseException(cause: Throwable) : RuntimeException(cause)

/**
 * Catch Firestore exceptions and abstract as Mission exception
 */
fun <T> catchFirestoreException(block: () -> T) :T {
    try {
        return block()

    } catch (e: InterruptedException) {
        throw MissionDatabaseException(e)

    } catch (e: ExecutionException) {
        throw MissionDatabaseException(e)
    }
}
