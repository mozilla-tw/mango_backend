package org.mozilla.msrp.platform.firestore

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.UncheckedExecutionException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.CancellationException

fun DocumentSnapshot.areFieldsPresent(fieldNames: List<String>): Boolean {
    return fieldNames.none { this.get(it) == null }
}

fun DocumentSnapshot.checkAbsentFields(fieldNames: List<String>): List<String> {
    return fieldNames.filter { this.get(it) == null }
}

/** Extensions for read/write from/to Firestore */

fun Query.getResultsUnchecked(): List<QueryDocumentSnapshot> {
   return get().getUnchecked().documents
           .apply { FirestoreReadWriteCounter.incRead(size) }
}

fun DocumentReference.getUnchecked(): DocumentSnapshot {
    return get().getUnchecked()
            .apply { FirestoreReadWriteCounter.incRead(1) }
}

fun DocumentReference.setUnchecked(
        obj: Any,
        mapper: ObjectMapper? = null,
        options: SetOptions? = null
): WriteResult {

    val newObj = mapper?.convertValue(obj, Map::class.java) ?: obj

    val operation = options?.let { set(newObj, options) } ?: set(newObj)
    return operation.getUnchecked()
            .apply { FirestoreReadWriteCounter.incWrite(1) }
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

@Throws(DateTimeParseException::class)
fun stringToLocalDateTime(localDateTimeString: String): LocalDateTime {
    return LocalDateTime.parse(localDateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
