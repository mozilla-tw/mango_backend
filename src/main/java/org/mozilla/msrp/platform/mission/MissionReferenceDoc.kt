package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.annotation.IgnoreExtraProperties
import org.mozilla.msrp.platform.firestore.areFieldsPresent

/**
 * Document in group collection, containing information that can be used
 * to refer to the document in the mission collection
 *
 * Use the below statement to refer to the mission document
 * collection(type).whereEqualsTo("mid", mid)
 *
 * @param mid mission id
 * @param type mission type, this will also be the name of mission collection
 */
@IgnoreExtraProperties
data class MissionReferenceDoc(
        val endpoint: String = ""
) {

    companion object {

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): MissionReferenceDoc? {
            return if (snapshot.areFieldsPresent(listOf("endpoint"))) {
                snapshot.toObject(MissionReferenceDoc::class.java)
            } else {
                null
            }
        }
    }
}
