package org.mozilla.msrp.platform.redward

import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject


sealed class RedeemResponse {
    class Success(val rewardCoupon: RewardCouponDoc) : RedeemResponse()
    class Fail(val message: kotlin.String) : RedeemResponse()
}

/**
 * Reward related API
 *
 * */
@RestController
class RedeemController @Inject constructor(val rewardRepository: RewardRepository) {

    private var logger = logger()
    /**
     * Get redeem code if possible.
     * Return the failing reason from the service.
     *
     * */
    @RequestMapping("/api/v1/redeem/{missionType}")
    internal fun redeem(@PathVariable("missionType") missionType: String,
                        @RequestParam(value = "mid") mid: String,
                        @RequestAttribute("uid") uid: String): ResponseEntity<RedeemResponse>? {
        val redeemResult = rewardRepository.redeem(missionType, mid, uid)

        if (redeemResult == null) {
            logger.error("Unexpected error when redeem: Type for missionType[$missionType] mid[$mid] uid[$uid]")
        } else {
            logger.info(redeemResult.debugInfo)
        }

        // TODO: add logging
        return when (redeemResult) {
            is RedeemResult.Success -> ResponseEntity(RedeemResponse.Success(redeemResult.rewardCouponDoc), HttpStatus.OK)
            is RedeemResult.UsedUp -> ResponseEntity(RedeemResponse.Fail("No reward left"), HttpStatus.NOT_FOUND)
            is RedeemResult.NotReady -> ResponseEntity(RedeemResponse.Fail("Not ready to redeem"), HttpStatus.FORBIDDEN)
            is RedeemResult.InvalidRewardType -> ResponseEntity(RedeemResponse.Fail("Invalid reward type"), HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(RedeemResponse.Fail("Not able to redeem"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}