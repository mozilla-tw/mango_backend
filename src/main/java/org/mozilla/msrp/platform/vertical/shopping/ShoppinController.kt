package org.mozilla.msrp.platform.vertical.shopping

import org.mozilla.msrp.platform.vertical.game.UiModel
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
class ShoppinController @Inject constructor(private val voucherRepository: VoucherRepository) {

    @RequestMapping("/api/v1/shopping/coupon")
    internal fun coupon(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getCoupons()
    }


    @RequestMapping("/api/v1/shopping/voucher")
    internal fun voucher(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getVouchers()
    }


    @RequestMapping("/api/v1/shopping/deal")
    internal fun deal(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getDeals()
    }
}
