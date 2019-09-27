package org.mozilla.msrp.platform.redward

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.mission.JoinStatus
import org.mozilla.msrp.platform.mission.MissionJoinDoc
import org.mozilla.msrp.platform.mission.MissionRepository
import org.mozilla.msrp.platform.mission.isExpired
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Named

sealed class RedeemResult(val debugInfo: String) {
    class Success(val rewardCouponDoc: RewardCouponDoc, debugMessage: String) : RedeemResult(debugMessage)
    class UsedUp(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class NotReady(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class InvalidReward(val message: String, debugMessage: String) : RedeemResult(debugMessage)
    class Failure(val message: String, debugMessage: String) : RedeemResult(debugMessage)
}

@Named
open class RewardRepository @Inject constructor(

        private val missionRepository: MissionRepository,
        private val firestore: Firestore) {

    @Inject
    lateinit var clock: Clock

    /**
     * Return a RedeemResult for the user.
     * If the user/mission is not eligible for redeem, return null
     *
     * @param mid mission id
     * @param uid user id
     *
     * @return RedeemResult
     * */
    fun redeem(missionType: String, mid: String, uid: String, zoneId: ZoneId): RedeemResult {

        // check if the reward is expired
        val findMission = missionRepository.findMission(missionType, mid)

        val rewardType: String = findMission?.rewardType
                ?: // should be the name of the reward collection
                return RedeemResult.InvalidReward(
                        "Can't find Reward Type",
                        "Can't find Reward Type for missionType[$missionType] mid[$mid] uid[$uid]")

        try {

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

                        if (findMission.isExpired(clock, zoneId)) {
                            return@runTransaction RedeemResult.InvalidReward(
                                    "Reward Expired",
                                    "Reward Expired for missionType[$missionType] mid[$mid] uid[$uid]")
                        }
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
        } catch (e: InterruptedException) {
            return RedeemResult.Failure(
                    "Not able to redeem",
                    "[Redeem][Error][missionType[$missionType] mid[$mid] uid[$uid]]====$e")

        } catch (e: ExecutionException) {
            return RedeemResult.Failure(
                    "Not able to redeem",
                    "[Redeem][Error][missionType[$missionType] mid[$mid] uid[$uid]]====$e")
        }
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

}

