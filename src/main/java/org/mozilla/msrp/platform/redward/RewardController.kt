package org.mozilla.msrp.platform.redward

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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


    /**
     * Get redeem code if possible.
     * Return the failing reason from the service.
     *
     * */
    @RequestMapping("/api/v1/reward")
    internal fun redeem(@RequestParam(value = "mid") mid: String,
                        @RequestAttribute("uid") uid: String): ResponseEntity<RedeemResponse>? {

        // TODO: add logging
        return when (val redeemResult = rewardRepository.redeem(mid, uid)) {
            is RedeemResult.Success -> ResponseEntity(RedeemResponse.Success(redeemResult.rewardCouponDoc), HttpStatus.OK)
            is RedeemResult.UsedUp -> ResponseEntity(RedeemResponse.Fail("No reward left"), HttpStatus.NOT_FOUND)
            is RedeemResult.NotReady -> ResponseEntity(RedeemResponse.Fail("Not ready to redeem"), HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(RedeemResponse.Fail("Not able to redeem"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


}