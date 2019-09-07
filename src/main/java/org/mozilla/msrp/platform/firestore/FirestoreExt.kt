package org.mozilla.msrp.platform.firestore

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.UncheckedExecutionException
import org.mozilla.msrp.platform.util.logger
import java.util.concurrent.CancellationException

fun DocumentSnapshot.areFieldsPresent(fieldNames: List<String>): Boolean {
    fieldNames.forEach { fieldName ->
        this.get(fieldName) ?: return false
    }
    return true
}

/** Extensions for read/write from/to Firestore */

fun Query.getResultsUnchecked(): List<QueryDocumentSnapshot> {
   return get().getUnchecked().documents
}

fun DocumentReference.getUnchecked(): DocumentSnapshot {
    return get().getUnchecked()
}

fun DocumentReference.setUnchecked(obj: Any, mapper: ObjectMapper? = null): WriteResult {
    return mapper?.let {
        val map = it.convertValue(obj, Map::class.java)
        set(map).getUnchecked()
    } ?: set(obj).getUnchecked()
}

fun <T> DocumentSnapshot.toObject(classType: Class<T>, mapper: ObjectMapper? = null): T? {
    return mapper?.convertValue(data, classType) ?: toObject(classType)
}


/** Extensions for Collection/Document navigation */

val DocumentReference.parentCollection
    get() = this.parent

val CollectionReference.parentDocument: DocumentReference?
    get() = this.parent

/** Extensions for handling and re-throwing exceptions thrown by Firestore */
fun <T> ApiFuture<T>.getUnchecked(): T {
    return try {
        Futures.getUnchecked(this)

    } catch (e: CancellationException) {
        throw FirestoreException(cause = e)

    } catch (e: UncheckedExecutionException) {
        throw FirestoreException(cause = e)
    }
}
