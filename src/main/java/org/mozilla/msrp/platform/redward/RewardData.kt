package org.mozilla.msrp.platform.redward


class RewardCouponDoc(
    var rid: String? = null,
    var uid: String = "",
    var mid: String? = null,
    var display_name: String = "",
    var code: String? = null,
    var expire_date: Long? = null,
    var created_timestamp: Long? = null,
    var updated_timestamp: Long? = null
)
