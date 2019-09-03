package org.mozilla.msrp.platform.mission

import com.google.cloud.firestore.*
import org.mozilla.msrp.platform.firestore.*
import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named

@Named
class MissionRepositoryFirestore @Inject internal constructor(
        private val firestore: Firestore
) : MissionRepository {

    private val log = logger()

    override fun getMissionsByGroupId(groupId: String): List<MissionDoc> {
        return getMissionRefsByGroupId(groupId).mapNotNull { getMissionsByRef(it) }
    }

    private fun getMissionRefsByGroupId(groupId: String): List<MissionReferenceDoc> {
        val query = firestore.collection(groupId)

        return query.getResultsUnchecked().mapNotNull {
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
                missionType = createData.missionType,
                interestPings = createData.pings
        )

        docRef.setUnchecked(doc)

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
        firestore.collection(groupId).document().setUnchecked(doc)
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
                .setUnchecked(joinDoc)

        return joinDoc
    }

    override fun findJoinedMissionsByPing(uid: String, ping: String): List<MissionDoc> {
        return firestore.collectionGroup("users")
                .findDocumentsByUid(uid)
                .mapNotNull { getParentMissionDoc(it) }
                .filter { it.interestPings.contains(ping) }
    }

    private fun getParentMissionDoc(joinDoc: QueryDocumentSnapshot): MissionDoc? {
        val missionSnapshot = joinDoc.reference
                .parentCollection
                .parentDocument
                ?.getUnchecked()

        return missionSnapshot?.let {
            return MissionDoc.fromDocument(it)

        } ?: run {
            log.warn("failed to resolve parent document for join doc: ${joinDoc.reference.path}")
            null
        }
    }

    private fun Query.findDocumentsByUid(uid: String): List<QueryDocumentSnapshot> {
        return this.whereEqualTo("uid", uid).getResultsUnchecked()
    }
}
