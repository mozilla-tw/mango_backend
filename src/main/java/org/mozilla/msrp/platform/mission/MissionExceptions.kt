package org.mozilla.msrp.platform.mission

/**
 * Still looking for a way to wrap all FirestoreException thrown during the execution of MissionController into
 * MissionDatabaseException, so global exception handler will only know there's something wrong with the
 * mission database, instead of knowing anything about Firestore
 */
class MissionDatabaseException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(cause: Throwable?) : super(cause)
    constructor(msg: String, cause: Throwable?) : super(msg, cause)
}