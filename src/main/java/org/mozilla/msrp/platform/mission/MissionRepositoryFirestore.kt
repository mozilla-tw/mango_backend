package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.*
import javax.inject.Inject
import javax.inject.Named

@Named
class MissionRepositoryFirestore @Inject internal constructor(
        private val firestore: Firestore
) : MissionRepository {

    override fun getMissionsByGroupId(groupId: String): List<MissionDoc> {
        return getMissionRefsByGroupId(groupId).mapNotNull { getMissionsByRef(it) }
    }

    private fun getMissionRefsByGroupId(groupId: String): List<MissionReferenceDoc> {
        val query = firestore.collection(groupId)

        return query.waitResults.mapNotNull {
            MissionReferenceDoc.fromDocument(it)
        }
    }

    /**
     * @throws MissionDatabaseException
     */
    private fun getMissionsByRef(ref: MissionReferenceDoc): MissionDoc? {
        val endpoint = ref.endpoint
        return if (endpoint.startsWith("/")) {
            val docPath = endpoint.substring(1)
            val snapshot = firestore.document(docPath).getUnchecked()
            MissionDoc.fromDocument(snapshot)
        } else {
            null
        }
    }

    /**
     * @throws MissionDatabaseException
     */
    override fun createMission(createData: MissionCreateData): MissionDoc {
        val docRef = firestore.collection(createData.missionType).document()
        val doc = MissionDoc(
                mid = docRef.id,
                missionName = createData.missionName,
                titleId = createData.titleId,
                descriptionId = createData.descriptionId,
                missionType = createData.missionType
        )

        docRef.set(doc).getUnchecked()

        return doc
    }

    override fun groupMissions(
            groupId: String,
            groupItems: List<MissionGroupItemData>
    ): List<MissionReferenceDoc> {
        return groupItems.map { convertToReferenceDoc(groupId, it) }
    }

    /**
     * @throws MissionDatabaseException
     */
    private fun convertToReferenceDoc(
            groupId: String,
            groupItem: MissionGroupItemData
    ): MissionReferenceDoc {
        val doc = MissionReferenceDoc(groupItem.endpoint)
        firestore.collection(groupId).document().set(doc).getUnchecked()
        return doc
    }

    /**
     * This will insert a document to /missionType/mid/users/
     *
     * @throws MissionDatabaseException
     */
    override fun joinMission(
            uid: String,
            missionType: String,
            mid: String
    ): MissionJoinDoc {
        val joinDoc = MissionJoinDoc(uid = uid, status = "join")

        firestore.collection(missionType)
                .document(mid)
                .collection("users")
                .document()
                .set(joinDoc)
                .getUnchecked()

        return joinDoc
    }
}

private val Query.waitResults: List<QueryDocumentSnapshot>
    get() {
        return get().getUnchecked().documents
    }

private fun DocumentReference.getUnchecked(): DocumentSnapshot {
    return get().getUnchecked()
}
