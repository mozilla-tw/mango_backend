package org.mozilla.msrp.platform.firestore

import org.mozilla.msrp.platform.util.logger

object FirestoreReadWriteCounter : ThreadLocal<Pair<Int, Int>>() {

    private val logger = logger()

    override fun initialValue(): Pair<Int, Int> {
        return 0 to 0
    }

    fun incRead(value: Int = 1) {
        get().apply { set(first + value to second) }
    }

    fun incWrite(value: Int = 1) {
        get().apply { set(first to second + value) }
    }

    fun reset() {
        set(0 to 0)
    }

    fun consume() {
        get().apply { logger.info("read=$first, write=$second") }
        reset()
    }
}
