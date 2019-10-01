package org.mozilla.msrp.platform.redward


class RewardCouponDoc(
    var rid: String? = null,
    var uid: String? = null,
    var mid: String? = null,
    var code: String? = null,
    var expire_date: Long? = null,
    var created_timestamp: Long? = null,
    var updated_timestamp: Long? = null
)



val testReward = RewardCouponDoc(
    rid = "lZuctiWQZcC4F6oo4Zj2",
    uid = "1111",
    mid = "22222",
    code = "C0UP0N2019",
    expire_date = 1567756983,
    created_timestamp = 3333333,
    updated_timestamp = 3333333)
