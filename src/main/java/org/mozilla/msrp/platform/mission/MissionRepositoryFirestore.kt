package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.*
import org.mozilla.msrp.platform.firestore.*
import org.mozilla.msrp.platform.mission.qualifier.DailyMissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc
import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named

@Named
class MissionRepositoryFirestore @Inject internal constructor(
        private val firestore: Firestore
) : MissionRepository {

    private val log = logger()

    @Inject
    lateinit var mapper: ObjectMapper

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
                expiredDate = createData.expiredDate,
                interestPings = createData.pings,
                min_version = createData.min_version,
                missionParams = createData.missionParams
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

    override fun getJoinStatus(uid: String, missionType: String, mid: String): JoinStatus? {
        return firestore.collection(missionType)
                .document(mid)
                .collection("users")
                .findDocumentsByUid(uid)
                .firstOrNull()
                ?.toObject(MissionJoinDoc::class.java, mapper)
                ?.status
    }

    override fun joinMission(uid: String, missionType: String, mid: String): MissionJoinDoc {
        val path = firestore.collection(missionType)
                .document(mid)
                .collection("users")

        val oldRecordSnapshot = path
                .findDocumentsByUid(uid)
                .firstOrNull()

        val oldRecord = oldRecordSnapshot?.toObject(MissionJoinDoc::class.java, mapper)

        return oldRecord?.let {
             oldRecord.copy(status = JoinStatus.Joined).apply {
                 oldRecordSnapshot.reference.setUnchecked(this, mapper)
             }

        } ?: run {
            MissionJoinDoc(uid, missionType, mid, JoinStatus.Joined).apply {
                path.document().setUnchecked(this, mapper)
            }
        }
    }

    override fun quitMission(uid: String, missionType: String, mid: String): Boolean {
        val joinRecord = firestore.collection(missionType)
                .document(mid)
                .collection("users")
                .findDocumentsByUid(uid)
                .firstOrNull()

        val result = joinRecord?.reference?.delete()
        return result != null
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

    override fun getDailyMissionProgress(
            uid: String,
            mid: String
    ): DailyMissionProgressDoc? {

        val collection = getDailyMissionCollection()
        val result = collection
                .whereEqualTo("uid", uid)
                .whereEqualTo("mid", mid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .getResultsUnchecked()
                .firstOrNull()

        log.info("progress collection=${collection.path}, docId=${result?.reference?.id}")

        return result?.toObject(DailyMissionProgressDoc::class.java)
    }

    override fun updateDailyMissionProgress(progressDoc: MissionProgressDoc) {
        val collection = getDailyMissionCollection()
        log.info("firestore path: ${collection.path}")

        collection.document().setUnchecked(progressDoc)
    }

    private fun getDailyMissionCollection() =
            firestore.collection("${MissionType.DailyMission.identifier}_progress")
}
