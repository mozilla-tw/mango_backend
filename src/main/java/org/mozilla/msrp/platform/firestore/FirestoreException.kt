package org.mozilla.msrp.platform.firestore

import org.springframework.core.NestedRuntimeException

class FirestoreException : NestedRuntimeException {

    constructor(msg: String? = null, cause: Throwable?) : super(msg, cause)
    constructor(msg: String) : super(msg)
}
