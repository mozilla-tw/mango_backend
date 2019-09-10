package org.mozilla.msrp.platform.vertical.shopping

import org.mozilla.msrp.platform.vertical.game.UiModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
class ShoppingController @Inject constructor(private val voucherRepository: VoucherRepository) {

    @GetMapping("/api/v1/shopping/coupon")
    internal fun coupon(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getCoupons()
    }


    @GetMapping("/api/v1/shopping/voucher")
    internal fun voucher(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getVouchers()
    }


    @GetMapping("/api/v1/shopping/deal")
    internal fun deal(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return voucherRepository.getDeals()
    }
}