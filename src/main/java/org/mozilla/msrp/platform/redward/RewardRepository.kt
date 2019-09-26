package org.mozilla.msrp.platform.redward

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import com.google.cloud.firestore.QueryDocumentSnapshot
import org.mozilla.msrp.platform.firestore.getBatchIteration
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.firestore.stringToLocalDateTime
import org.mozilla.msrp.platform.mission.JoinStatus
import org.mozilla.msrp.platform.mission.MissionDoc
import org.mozilla.msrp.platform.mission.MissionJoinDoc
import org.mozilla.msrp.platform.mission.MissionRepository
import java.time.Clock
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Named

sealed class RedeemResult(val debugInfo: String) {
    class Success(val rewardCouponDoc: RewardCouponDoc, debugMessage: String) : RedeemResult(debugMessage)
    class UsedUp(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class NotReady(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class InvalidRewardType(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class Failure(val message: String, debugMessage: String) : RedeemResult(debugMessage)
}

@Named
open class RewardRepository @Inject constructor(

        private val missionRepository: MissionRepository,
        private val firestore: Firestore) {

    @Inject
    lateinit var clock: Clock

    /**
     * Return a Reward document for ther user.
     * If the user/mission is not eligible for redeem, return null
     *
     * @param mid mission id
     * @param uid user id
     *
     * @return RedeemResult
     * */
    fun redeem(missionType: String, mid: String, uid: String): RedeemResult? {

        val rewardType: String = missionRepository.findMission(missionType, mid)?.rewardType
                ?: // should be "reward_coupon"
                return RedeemResult.InvalidRewardType(
                        "Can't find Reward Type",
                        "Can't find Reward Type for missionType[$missionType] mid[$mid] uid[$uid]")

        // TODO: extract all firebase related class here, and move business logic to a domain class
        val trans: ApiFuture<RedeemResult> = firestore.runTransaction { transaction ->

            // search for the mission and see its redeem status.
            val missionJoinDoc = missionRepository.getMissionJoinDoc(uid, missionType, mid)
            when (missionJoinDoc?.status) {
                JoinStatus.Redeemed -> {
                    val rewardDocId = missionJoinDoc.rewardDocId
                            ?: // shouldn't happen, data error
                            return@runTransaction RedeemResult.Failure(
                                    "Reward format invalid",
                                    "MissionJoinDoc 's status is Redeemed, but RewardDocId is null ${info(missionJoinDoc)}")

                    // return its related RewardDoc, currently we only have Coupon-Reward
                    val rewardCouponDoc = getRewardDoc(rewardType, rewardDocId)
                            ?: // shouldn't happen, data error
                            return@runTransaction RedeemResult.Failure("Can't find your reward", "Found RewardDocId $rewardDocId but no Doc is found ${info(missionJoinDoc)}")

                    return@runTransaction RedeemResult.Success(rewardCouponDoc, "Reward doc ")
                }

                JoinStatus.Complete -> {

                    // update RewardDoc
                    val rewardDocId = requestReward(rewardType, transaction)?.rid
                            ?: // shouldn't happen, data error
                            return@runTransaction RedeemResult.UsedUp(
                                    "No reward left",
                                    "No reward left ${info(missionJoinDoc)}")

                    val rewardDoc = consumeRewardDoc(rewardType, uid, mid, rewardDocId, transaction)

                    // update MissionJoinDoc
                    val success = missionRepository.updateMissionJoinDocAfterRedeem(uid, missionType, mid, rewardDocId, transaction)
                    if (success && rewardDoc != null) {

                        return@runTransaction RedeemResult.Success(rewardDoc, "Reward consumed! ${info(missionJoinDoc)}")

                    } else {
                        return@runTransaction RedeemResult.Failure(
                                "Can't update Mission Progress",
                                "Can't update MissionJoinDoc ${info(missionJoinDoc)}")
                    }
                }
                else -> {
                    // mission in state other than `Completed` or `Redeemed` are not eligible for redeem
                    return@runTransaction RedeemResult.NotReady(
                            "Not ready to redeem",
                            "Not ready to redeem for mission ${missionJoinDoc?.mid}, user ${missionJoinDoc?.uid}")
                }
            }
        }
        return trans.get()
    }

    private fun info(missionJoinDoc: MissionJoinDoc) =
            "[INFO: mission ${missionJoinDoc.mid}, user ${missionJoinDoc.uid}]"


    // consume that reward with user and mission ids
    private fun consumeRewardDoc(rewardType: String, uid: String, mid: String, docId: String, transaction: Transaction): RewardCouponDoc? {
        val document = firestore.collection(rewardType)
                .document(docId)
        val toEpochMilli = clock.instant().toEpochMilli()
        transaction.update(document, mapOf(
                "uid" to uid,
                "mid" to mid,
                "updated_timestamp" to toEpochMilli
        ))
        val rewardCouponDoc = document.getUnchecked().toObject(RewardCouponDoc::class.java)
        rewardCouponDoc?.uid = uid
        rewardCouponDoc?.mid = mid
        rewardCouponDoc?.updated_timestamp = toEpochMilli

        return rewardCouponDoc
    }

    // find a reward that has no user bind to it for that reward type.
    private fun requestReward(rewardType: String, transaction: Transaction): RewardCouponDoc? {

        val query = firestore.collection(rewardType)
                .whereEqualTo("uid", "")
                .limit(1)
        transaction.mutationsSize
        return transaction.get(query)
                .getUnchecked().documents.getOrNull(0)?.toObject(RewardCouponDoc::class.java)
    }

    private fun getRewardDoc(couponType: String, docId: String): RewardCouponDoc? {
        return firestore.collection(couponType)
                .document(docId)
                .getUnchecked()
                .toObject(RewardCouponDoc::class.java)
    }

    fun uploadCoupons(
            coupons: List<String>,
            couponName: String,
            missionType: String,
            mid: String
    ): List<RewardCouponDoc> {
        val collection = firestore.collection(couponName)
        val createdTime = clock.instant().toEpochMilli()

        val mission = missionRepository.findMission(missionType, mid)
                ?: return emptyList()

        clearCollection(collection)

        return coupons.mapByBatch { batchUpdateCoupons(it, collection, mission, createdTime) }.flatten()
    }

    private fun clearCollection(collection: CollectionReference) {
        collection.getResultsUnchecked().mapByBatch { batchDeleteDocuments(it) }
    }

    private fun batchDeleteDocuments(docs: List<QueryDocumentSnapshot>) {
        firestore.runTransaction { tran ->
            docs.map { tran.delete(it.reference) }
        }
    }

    private fun batchUpdateCoupons(
            coupons: List<String>,
            collection: CollectionReference,
            mission: MissionDoc,
            createdTime: Long
    ): List<RewardCouponDoc> {
        return firestore.runTransaction { tran ->
            coupons.map { couponCode ->
                val docRef = collection.document()
                val doc = RewardCouponDoc(
                        rid = docRef.id,
                        mid = mission.mid,
                        code = couponCode,
                        expire_date = stringToLocalDateTime(mission.expiredDate)
                                .toInstant(ZoneOffset.UTC)
                                .toEpochMilli(),
                        created_timestamp = createdTime,
                        updated_timestamp = 0
                )
                tran.set(docRef, doc)
                doc
            }
        }.getUnchecked()
    }

    private fun <T, K> List<T>.mapByBatch(block: (List<T>) -> K): List<K> {
        val nBatch = getBatchIteration(this)
        return this.groupByIndex { it % nBatch }.values.map { block(it) }
    }

    private fun <T, K> List<T>.groupByIndex(selector: (Int) -> K): Map<K, List<T>> {
        return mapIndexed { index, value -> index to value }
                .groupBy { selector(it.first) }
                .mapValues { entry ->
                    entry.value.map { it.second }
                }
    }
}
