package org.mozilla.msrp.platform.user.data


data class UserDoc(
        var uid: String? = null,
        var firebase_uid: String? = null,
        var firefox_uid: String? = null,
        var created_timestamp: Long? = null,
        var updated_timestamp: Long? = null,
        var status: String? = null,
        var email: String? = null,
        val version: Int = 1
)
