package org.mozilla.msrp.platform.firestore

import com.google.cloud.firestore.*
import org.mozilla.msrp.platform.mission.getUnchecked

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

fun DocumentReference.setUnchecked(obj: Any): WriteResult {
    return set(obj).getUnchecked()
}


/** Extensions for Collection/Document navigation */

val DocumentReference.parentCollection
    get() = this.parent

val CollectionReference.parentDocument: DocumentReference?
    get() = this.parent
