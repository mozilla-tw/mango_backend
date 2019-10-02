package org.mozilla.msrp.platform.redward

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.DateTimeException
import java.time.ZoneId
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
                        @RequestParam(value = "tz") tz: String,
                        @RequestParam(value = "mid") mid: String,
                        @RequestAttribute("uid") uid: String): ResponseEntity<RedeemResponse>? {
        val zoneId = createZone(tz)
                ?: return ResponseEntity(RedeemResponse.Fail("Timezone is not correct"), HttpStatus.BAD_REQUEST)

        return when (val redeemResult = rewardRepository.redeem(missionType, mid, uid, zoneId)) {
            is RedeemResult.Success -> {
                logger.info(redeemResult.debugInfo)
                ResponseEntity(RedeemResponse.Success(redeemResult.rewardCouponDoc), HttpStatus.OK)
            }
            is RedeemResult.UsedUp -> {
                logger.info(redeemResult.debugInfo)
                ResponseEntity(RedeemResponse.Fail("No reward left"), HttpStatus.NOT_FOUND)
            }
            is RedeemResult.NotReady -> {
                logger.info(redeemResult.debugInfo)
                ResponseEntity(RedeemResponse.Fail("Not ready to redeem"), HttpStatus.FORBIDDEN)
            }
            is RedeemResult.InvalidReward -> {
                logger.info(redeemResult.debugInfo)
                ResponseEntity(RedeemResponse.Fail("Invalid reward"), HttpStatus.BAD_REQUEST)
            }
            is RedeemResult.Failure -> {
                logger.error(redeemResult.debugInfo)
                ResponseEntity(RedeemResponse.Fail("Not able to redeem"), HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    private fun createZone(timezone: String): ZoneId? {
        return try {
            ZoneId.of(timezone)

        } catch (e: DateTimeException) {
            logger.info("unsupported timezone=$timezone")
            null
        }
    }

    @RequestMapping(value = ["/api/v1/admin/coupon"], method = [RequestMethod.POST])
    internal fun uploadCoupons(@RequestParam token: String,
                               @RequestParam("couponName") couponName: String,
                               @RequestParam("file") file: MultipartFile,
                               @RequestParam("missionType") missionType: String,
                               @RequestParam("mid") mid: String): ResponseEntity<CouponUploadResponse> {
        if (JwtHelper.verify(token) !== JwtHelper.ROLE_MSRP_ADMIN) {
            return ResponseEntity(CouponUploadResponse.Fail("No Permission"), HttpStatus.UNAUTHORIZED)
        }
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