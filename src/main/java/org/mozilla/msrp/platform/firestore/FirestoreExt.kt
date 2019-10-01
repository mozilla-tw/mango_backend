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
import kotlin.math.ceil

const val BATCH_VOLUME = 500

fun DocumentSnapshot.areFieldsPresent(fieldNames: List<String>): Boolean {
    fieldNames.forEach { fieldName ->
        this.get(fieldName) ?: return false
    }
    return true
}

fun DocumentSnapshot.checkAbsentFields(fieldNames: List<String>): List<String> {
    return fieldNames.filter { this.get(it) == null }
}

/** Extensions for read/write from/to Firestore */

fun Query.getResultsUnchecked(): List<QueryDocumentSnapshot> {
   return get().getUnchecked().documents
}

fun DocumentReference.getUnchecked(): DocumentSnapshot {
    return get().getUnchecked()
}

fun DocumentReference.setUnchecked(
        obj: Any,
        mapper: ObjectMapper? = null,
        options: SetOptions? = null
): WriteResult {

    val newObj = mapper?.convertValue(obj, Map::class.java) ?: obj

    return options?.let {
        set(newObj, options).getUnchecked()
    } ?: set(newObj).getUnchecked()
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

fun <T> getBatchIteration(list: List<T>): Int {
    return ceil(list.size / BATCH_VOLUME.toFloat()).toInt()
}
