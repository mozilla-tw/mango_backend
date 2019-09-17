package org.mozilla.msrp.platform.user.data


data class UserActivityDoc(
        val userDocId: String,
        val updated_timestamp: Long,
        val status: String,
        val version: Int = 1
) {
    companion object {
        const val KEY_USER_DOC_ID = "userDocId"
    }
}

data class UserDoc(
        var uid: String? = null,
        var firebase_uid: String? = null,
        var firefox_uid: String? = null,
        var created_timestamp: Long? = null,
        var updated_timestamp: Long? = null,
        var status: String? = null,
        var email: String? = null,
        val version: Int = 1
) {

    companion object {
        const val KEY_UID = "uid"
        const val KEY_FIREBASE_UID = "firebase_uid"
        const val KEY_FIREFOX_UID = "firefox_uid"
        const val KEY_CREATED_TIMESTAMP = "created_timestamp"
        const val KEY_UPDATED_TIMESTAMP = "updated_timestamp"
        const val KEY_STATUS = "status"
        const val KEY_EMAIL = "email"
        const val KEY_VERSION = "version"

        const val STATUS_SIGN_IN = "sign-in"
        const val STATUS_DEPRECATED = "deprecated"
        const val STATUS_SUSPEND = "suspend"
    }
}
