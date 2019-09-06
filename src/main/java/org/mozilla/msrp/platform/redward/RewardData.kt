package org.mozilla.msrp.platform.redward


class RewardCouponDoc(
    var rid: String? = null,
    var uid: String? = null,
    var mid: String? = null,
    var code: String? = null,
    var campaign: String? = null,
    var title: String? = null,
    var content: String? = null,
    var expire_date: Long? = null,
    var created_timestamp: Long? = null,
    var updated_timestamp: Long? = null
)



val testReward = RewardCouponDoc(
    rid = "lZuctiWQZcC4F6oo4Zj2",
    uid = "1111",
    mid = "22222",
    campaign = "201901",
    code = "C0UP0N2019",
    title = "Your coupon is ready",
    content = "Coupons are valid for a limited time only. We reserve the right to modify or cancel coupons at any time.",
    expire_date = 1567756983,
    created_timestamp = 3333333,
    updated_timestamp = 3333333)
