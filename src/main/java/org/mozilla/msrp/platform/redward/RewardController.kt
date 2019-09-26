package org.mozilla.msrp.platform.redward

import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.inject.Inject


sealed class RedeemResponse {
    class Success(val rewardCoupon: RewardCouponDoc) : RedeemResponse()
    class Fail(val message: String) : RedeemResponse()
}

sealed class CouponUploadResponse {
    class Success(val message: String) : CouponUploadResponse()
    class Fail(val message: String) : CouponUploadResponse()
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

    @RequestMapping(value = ["/api/v1/redeem/coupon/{couponName}"], method = [RequestMethod.POST])
    internal fun uploadCoupons(@PathVariable("couponName") couponName: String,
                               @RequestParam("file") file: MultipartFile,
                               @RequestParam("missionType") missionType: String,
                               @RequestParam("mid") mid: String): ResponseEntity<CouponUploadResponse> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(CouponUploadResponse.Fail("empty coupon file"))
        }

        val coupons = file.inputStream.bufferedReader().readLines()
        if (coupons.isEmpty()) {
            return ResponseEntity.badRequest().body(CouponUploadResponse.Fail("illegal coupon file format, please " +
                    "separate coupon codes into separated lines"))
        }

        val couponDocs = rewardRepository.uploadCoupons(coupons, couponName, missionType, mid)
        return ResponseEntity.ok(CouponUploadResponse.Success("${couponDocs.size} coupons uploaded"))
    }
}