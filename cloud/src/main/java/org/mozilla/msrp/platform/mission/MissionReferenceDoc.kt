package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.annotation.IgnoreExtraProperties
import java.util.Optional


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
        val mid: String = "",
        val type: String = ""
) {

    companion object {

        @JvmStatic
        fun fromDocument(snapshot: DocumentSnapshot): Optional<MissionReferenceDoc> {
            return if (snapshot.areFieldsPresent(listOf("mid", "type"))) {
                Optional.ofNullable(snapshot.toObject(MissionReferenceDoc::class.java))
            } else {
                Optional.empty()
            }
        }

        @JvmStatic
        fun getTargetMissions(ref: MissionReferenceDoc, firestore: Firestore): Query {
            return firestore.collection(ref.type).whereEqualTo("mid", ref.mid)
        }
    }
}
