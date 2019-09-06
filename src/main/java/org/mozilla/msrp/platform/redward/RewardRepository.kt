package org.mozilla.msrp.platform.redward

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import lombok.extern.log4j.Log4j2
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.mission.JoinStatus
import org.mozilla.msrp.platform.mission.MissionJoinDoc
import org.mozilla.msrp.platform.mission.MissionRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named

sealed class RedeemResult() {
    class Success(val rewardCouponDoc: RewardCouponDoc) : RedeemResult()
    class UsedUp(val message: String) : RedeemResult()
    class NotReady(val message: String) : RedeemResult()
    class Failure(val message: String) : RedeemResult()
}

@Named
@Log4j2
class RewardRepository @Inject constructor(

    private val missionRepository: MissionRepository,
    private val firestore: Firestore) {

    private val clock: Clock = Clock.systemUTC()

    /**
     * Return a Reward document for ther user.
     * If the user/mission is not eligible for redeem, return null
     *
     * @param mid mission id
     * @param uid user id
     *
     * @return RedeemResult
     * */
    fun redeem(mid: String, uid: String): RedeemResult? {

        //TODO: remove hard code , we only have daily mission
        val missionType = "mission_daily"       // get the mission type from client?
        val rewardType = "reward_coupon"        // get the rewardType from mission_daily document?

        // TODO: extract all firebase related class here, and move business logic to a domain class
        val trans: ApiFuture<RedeemResult> = firestore.runTransaction { transaction ->

            // search for the mission and see its redeem status.
            val missionJoinDoc = missionRepository.getMissionJoinDoc(uid, missionType, mid)
            when (missionJoinDoc?.status) {
                JoinStatus.Redeemed -> {
                    val rewardDocId = missionJoinDoc.rewardDocId
                        ?: // shouldn't happen, data error
                        return@runTransaction RedeemResult.Failure("MissionJoinDoc's status is Redeemed, but RewardDocId is null ${info(missionJoinDoc)}")

                    // return its related RewardDoc, currently we only have Coupon-Reward
                    val rewardCouponDoc = getRewardDoc(rewardType, rewardDocId)
                        ?: // shouldn't happen, data error
                        return@runTransaction RedeemResult.Failure("Found RewardDocId $rewardDocId but no Doc is found ${info(missionJoinDoc)}")

                    return@runTransaction RedeemResult.Success(rewardCouponDoc)
                }

                JoinStatus.Complete -> {

                    // update RewardDoc
                    val rewardDocId = requestReward(rewardType)?.rid
                        ?: // shouldn't happen, data error
                        return@runTransaction RedeemResult.UsedUp("No reward left ${info(missionJoinDoc)}")
                    consumeRewardDoc(rewardType, uid, mid, rewardDocId, transaction)

                    // update MissionJoinDoc
                    val success = missionRepository.updateMissionJoinDocAfterRedeem(uid, missionType, mid, rewardDocId, transaction)
                    if (success) {

                        // query again and return the rewardDoc
                        val rewardDocAgain = getRewardDoc(rewardType, rewardDocId)
                            ?: // shouldn't happen, data error
                            return@runTransaction RedeemResult.Failure("Can't find RewardDoc after update ${info(missionJoinDoc)}")
                        return@runTransaction RedeemResult.Success(rewardDocAgain)

                    } else {
                        return@runTransaction RedeemResult.Failure("Can't update MissionJoinDoc ${info(missionJoinDoc)}")
                    }
                }
                else -> {
                    // mission in state other than `Completed` or `Redeemed` are not eligible for redeem
                    return@runTransaction RedeemResult.NotReady("Not ready to redeem for mission ${missionJoinDoc?.mid}, user ${missionJoinDoc?.uid}\"")
                }
            }
        }
        return trans.get()
    }

    private fun info(missionJoinDoc: MissionJoinDoc) =
        "[INFO: mission ${missionJoinDoc.mid}, user ${missionJoinDoc.uid}]"


    // consume that reward with user and mission ids
    private fun consumeRewardDoc(rewardType: String, uid: String, mid: String, docId: String, transaction: Transaction) {
        val document = firestore.collection(rewardType)
            .document(docId)
        transaction.update(document, mapOf(
            "uid" to uid,
            "mid" to mid,
            "updated_timestamp" to clock.instant().toEpochMilli()
        ))
    }

    // find a reward that has no user bind to it for that reward type.
    private fun requestReward(rewardType: String): RewardCouponDoc? {
        return firestore.collection(rewardType)
            .whereEqualTo("uid", "")
            .limit(1)
            .getResultsUnchecked().getOrNull(0)
            ?.toObject(RewardCouponDoc::class.java)
    }

    private fun getRewardDoc(couponType: String, docId: String): RewardCouponDoc? {
        return firestore.collection(couponType)
            .document(docId)
            .getUnchecked()
            .toObject(RewardCouponDoc::class.java)
    }

}

