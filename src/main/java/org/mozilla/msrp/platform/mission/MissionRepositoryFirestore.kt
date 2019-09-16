package org.mozilla.msrp.platform.mission

import com.fasterxml.jackson.databind.ObjectMapper

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.Transaction
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.firestore.parentCollection
import org.mozilla.msrp.platform.firestore.parentDocument
import org.mozilla.msrp.platform.firestore.setUnchecked
import org.mozilla.msrp.platform.firestore.toObject
import com.google.cloud.firestore.*
import org.mozilla.msrp.platform.common.firebase.DistributedCounter
import org.mozilla.msrp.platform.common.firebase.getCounter
import org.mozilla.msrp.platform.common.firebase.setupCounter
import org.mozilla.msrp.platform.mission.qualifier.DailyMissionProgressDoc
import org.mozilla.msrp.platform.mission.qualifier.ProgressType
import org.mozilla.msrp.platform.mission.qualifier.MissionProgressDoc
import org.mozilla.msrp.platform.util.logger
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named

@Named
class MissionRepositoryFirestore @Inject internal constructor(
        private val firestore: Firestore
) : MissionRepository {

    private val log = logger()

    @Inject
    lateinit var clock: Clock

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
                startDate = createData.startDate,
                joinStartDate = createData.joinStartDate,
                joinEndDate = createData.joinEndDate,
                expiredDate = createData.expiredDate,
                interestPings = createData.pings,
                minVersion = createData.minVersion,
                missionParams = createData.missionParams ?: emptyMap(),
                rewardType = createData.rewardType,
                joinQuota = createData.joinQuota
        )

        docRef.setUnchecked(doc)
        docRef.setupCounter(COUNTER_JOIN_USERS, 8)

        return doc
    }

    override fun findMission(missionType: String, mid: String): MissionDoc? {
        return firestore.collection(missionType)
                .findDocumentsByMid(mid)
                .firstOrNull()
                ?.let { MissionDoc.fromDocument(it) }
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

    override fun setJoinStatus(status: JoinStatus, uid: String, missionType: String, mid: String) {
        firestore.collection(missionType)
                .document(mid)
                .collection("users")
                .findDocumentsByUid(uid)
                .firstOrNull()
                ?.reference
                ?.setUnchecked(mapOf("status" to status.status), mapper, SetOptions.merge())
    }

    override fun joinMission(uid: String, missionType: String, mid: String): MissionJoinDoc {
        val missionRef = firestore.collection(missionType).document(mid)
        val path = missionRef.collection("users")

        val oldRecordSnapshot = path
                .findDocumentsByUid(uid)
                .firstOrNull()

        val oldRecord = oldRecordSnapshot?.toObject(MissionJoinDoc::class.java, mapper)
        val newRecordPath = oldRecord ?.let { oldRecordSnapshot.reference } ?: path.document()

        val newRecord = oldRecord?.copy(status = JoinStatus.Joined)
                ?: MissionJoinDoc(uid, missionType, mid, JoinStatus.Joined)

        newRecordPath.setUnchecked(newRecord, mapper)
        missionRef.getJoinUserCounter()?.increase()

        return newRecord
    }

    // TODO: extract method
    /**
     * Get the mission doc. We might want the reward doc link from it
     * */
    override fun getMissionJoinDoc(uid: String, missionType: String, mid: String): MissionJoinDoc? {
        val path = firestore.collection(missionType)
            .document(mid)
            .collection("users")

        val oldRecordSnapshot = path
            .findDocumentsByUid(uid)
            .firstOrNull()

        return oldRecordSnapshot?.toObject(MissionJoinDoc::class.java, mapper)
    }

    // return true if there's a mission document found
    override fun updateMissionJoinDocAfterRedeem(
        uid: String,
        missionType: String,
        mid: String,
        rewardDocId: String,
        transaction: Transaction): Boolean {

        val path = firestore.collection(missionType)
            .document(mid)
            .collection("users")


        val oldRecordSnapshot = path
            .findDocumentsByUid(uid)
            .firstOrNull()

        if (oldRecordSnapshot != null) {
            val document = firestore.collection(missionType)
                .document(mid)
                .collection("users")
                .document(oldRecordSnapshot.id)

            transaction.update(document,
                mapOf("status" to JoinStatus.Redeemed.status,
                    "rewardDocId" to rewardDocId,
                    "updated_timestamp" to clock.instant().toEpochMilli()
                ))
            return true
        }
        return false
    }

    override fun quitMission(uid: String, missionType: String, mid: String): Boolean {
        val missionRef = firestore.collection(missionType).document(mid)

        val joinRecord = missionRef
                .collection("users")
                .findDocumentsByUid(uid)
                .firstOrNull()

        val result = joinRecord?.reference?.delete()

        if (result != null) {
            missionRef.getJoinUserCounter()?.decrease()
            return true
        }
        return false
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

    private fun Query.findDocumentsByMid(mid: String): List<QueryDocumentSnapshot> {
        return this.whereEqualTo("mid", mid).getResultsUnchecked()
    }

    override fun getDailyMissionParams(mid: String): Map<String, Any> {
        val params = firestore.collection(MissionType.DailyMission.identifier)
                .whereEqualTo("mid", mid)
                .getResultsUnchecked()
                .firstOrNull()

        return params?.let { MissionDoc.fromDocument(it)?.missionParams } ?: emptyMap()
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

        val resultDoc = result?.toObject(DailyMissionProgressDoc::class.java, mapper)

        if (resultDoc?.progressType == ProgressType.Clear) {
            return null
        }

        return resultDoc
    }

    override fun updateDailyMissionProgress(progressDoc: MissionProgressDoc) {
        val collection = getDailyMissionCollection()
        log.info("firestore path: ${collection.path}")

        collection.document().setUnchecked(progressDoc, mapper)
    }

    override fun clearDailyMissionProgress(uid: String, mid: String) {
        val collection = getDailyMissionCollection()
        val now = clock.instant().toEpochMilli()
        val clearDoc = DailyMissionProgressDoc(
                uid = uid,
                mid = mid,
                joinDate = 0,
                timestamp = now,
                missionType = MissionType.DailyMission.identifier,
                currentDayCount = 0,
                progressType = ProgressType.Clear
        )
        collection.document().setUnchecked(clearDoc, mapper)
    }

    override fun isImportantMission(missionType: String, mid: String): Boolean {
        val dataMap = firestore.collection("important_mission")
                .orderBy("created_timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .getResultsUnchecked()
                .firstOrNull()
                ?.data ?: return false

        return dataMap["missionType"] == missionType && dataMap["mid"] == mid
    }

    override fun getJoinCount(missionType: String, mid: String): Int {
        return firestore.collection(missionType)
                .document(mid)
                .getJoinUserCounter()
                ?.count ?: 0
    }

    private fun getDailyMissionCollection() =
            firestore.collection("${MissionType.DailyMission.identifier}_progress")

    private fun DocumentReference.getJoinUserCounter(): DistributedCounter? {
        return this.getCounter(COUNTER_JOIN_USERS)
    }

    companion object {
        private const val COUNTER_JOIN_USERS = "joinUsers"
    }
}
