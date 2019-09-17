package org.mozilla.msrp.platform.user

import com.google.cloud.firestore.*
import com.google.firebase.auth.FirebaseAuth
import org.mozilla.msrp.platform.user.data.UserActivityDoc
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.user.data.UserDoc
import org.mozilla.msrp.platform.util.logger
import org.springframework.stereotype.Repository
import java.time.Clock
import javax.inject.Inject

@Repository
class UserRepository @Inject constructor(firestore: Firestore) {

    private var users: CollectionReference
    private var userActivity: CollectionReference
    private val logger = logger()

    @Inject
    lateinit var clock: Clock


    companion object {
        private const val COLLECTION_USER = "users"
        private const val COLLECTION_USER_ACTIVITY = "user_activity"

        private const val DOC_FIELD_USER_FIREBASE_UID = "firebase_uid"
        private const val DOC_FIELD_USER_FIREFOX_UID = "firefox_uid"
        private const val DOC_FIELD_USER_EMAIL = "email"
        private const val DOC_FIELD_USER_UPDATED_TIMESTAMP = "updated_timestamp"
        private const val DOC_FIELD_USER_STATUS = "status"
        private const val DOC_FIELD_UPDATED_TIMESTAMP = "updated_timestamp"

        private const val ACCOUNT_ACTIVITY_ACTION_SIGN_IN = "sign-in"
        private const val ACCOUNT_ACTIVITY_ACTION_DEPRECATED = "deprecated"
        private const val ACCOUNT_ACTIVITY_ACTION_SUSPEND = "suspend"
        private const val USER_SUSPICIOUS_THRESHOLD = 2
        private const val USER_SUSPICIOUS_WARNING = 1
    }

    init {
        users = firestore.collection(COLLECTION_USER)
        userActivity = firestore.collection(COLLECTION_USER_ACTIVITY)
    }

    fun createCustomToken(fxUid: String?, additionalClaims: Map<String, String>): String? {
        return FirebaseAuth.getInstance().createCustomToken(fxUid, additionalClaims)
    }

    fun signInAndUpdateUserDocument(oldFbUid: String, fxUid: String, email: String): LoginResponse {

        val userDocIdFb = findUserDocumentIdByFbUid(oldFbUid)
                ?: return LoginResponse.Fail("No such user oldFbUid[$oldFbUid]")

        val userDocIdFxA = findUserDocumentIdByFxUid(fxUid)
        logger.info("signInAndUpdateUserDocument=== userDocIdFb[$userDocIdFb]====userDocIdFxA[$userDocIdFxA]")

        // the same FxA is used to login FxA
        if (userDocIdFxA != null) {

            logger.info("userDocIdFxA != null")

            // if the user is deprecated, fail fast
            val userDocFxA = findUserDocumentByFxUid(fxUid)
            if (userDocFxA?.status == ACCOUNT_ACTIVITY_ACTION_SUSPEND) {
                logger.info("UserDoc[$userDocIdFxA] is deprecated")
                return LoginResponse.Fail("UserDoc[$userDocIdFxA] is deprecated")
            }
            // get the sign-in count for the last 7 days
            val signInCountLast7DAYS: Int = signInCountLast7DAYS(userDocIdFxA)
            logger.info("signInCountLast7DAYS [$signInCountLast7DAYS]")

            // the user had two records in the past week. Means this time is the third time.
            // we should now suspend the user.
            if (USER_SUSPICIOUS_THRESHOLD == signInCountLast7DAYS) {
                setUserDocStatus(userDocIdFxA, ACCOUNT_ACTIVITY_ACTION_SUSPEND)
                logUserActivity(userDocIdFxA, ACCOUNT_ACTIVITY_ACTION_SUSPEND)

                logger.info("UserDoc[$userDocIdFxA] has logged in three times per 7 days.")
                return LoginResponse.UserSuspended("UserDoc[$userDocIdFxA] has logged in three times per 7 days.")
            }


            // the user document is the same, means the user sign in FxA in the same device.
            if (userDocIdFxA == userDocIdFb) {
                logger.info("userDocIdFxA == userDocIdFb")

                logUserActivity(userDocIdFxA, ACCOUNT_ACTIVITY_ACTION_SIGN_IN)

            } else {
                logger.info("signIn userDocIdFxA[$userDocIdFxA]")
                // the user document is different, means the user sign in FxA in another device.
                setUserDocStatus(userDocIdFxA, ACCOUNT_ACTIVITY_ACTION_SIGN_IN)
                logUserActivity(userDocIdFxA, ACCOUNT_ACTIVITY_ACTION_SIGN_IN)

                logger.info("deprecate userDocIdFb[$userDocIdFb]")
                // set the old document deprecated
                setUserDocStatus(userDocIdFb, ACCOUNT_ACTIVITY_ACTION_DEPRECATED)
                logUserActivity(userDocIdFb, ACCOUNT_ACTIVITY_ACTION_DEPRECATED)

            }

            // the user had one record in the past week. Means this time is the second time.
            // we should warn the user instead of returning success
            if (USER_SUSPICIOUS_WARNING == signInCountLast7DAYS) {
                logger.info("UserDoc[$userDocIdFxA] has logged in twice per 7 days.")

                return LoginResponse.SuspiciousWarning("UserDoc[$userDocIdFxA] has logged in twice per 7 days.")
            }
            logger.info("UserDoc[$userDocIdFxA] has logged in Successfully")
            return LoginResponse.Success("UserDoc[$userDocIdFxA] has sign in to FxAcc")
        } else {
            logger.info("userDocIdFxA == null")

            val updateData = mapOf(
                    DOC_FIELD_USER_FIREFOX_UID to fxUid,
                    DOC_FIELD_USER_EMAIL to email,
                    DOC_FIELD_USER_UPDATED_TIMESTAMP to clock.millis(),
                    DOC_FIELD_USER_STATUS to ACCOUNT_ACTIVITY_ACTION_SIGN_IN
            )
            users.document(userDocIdFb).set(updateData, SetOptions.merge())

            // add account activity
            logUserActivity(userDocIdFb, ACCOUNT_ACTIVITY_ACTION_SIGN_IN)

            logger.info("UserDoc is promoted [$userDocIdFxA] has logged in")
            return LoginResponse.Success("UserDoc is promoted [$userDocIdFxA] has logged in")
        }
    }

    private fun setUserDocStatus(currentUserDocId: String, status: String) {
        users.document(currentUserDocId).set(
                mapOf(DOC_FIELD_USER_STATUS to status,
                        DOC_FIELD_UPDATED_TIMESTAMP to clock.millis()), SetOptions.merge())
    }


    private fun signInCountLast7DAYS(existingUserDocId: String): Int {
        val now = clock.millis()
        val aWeekAgo = now - 7 * 24 * 60 * 60 * 1000L
        return userActivity.whereEqualTo("userDocId", existingUserDocId)
                .whereEqualTo("action", "sign-in")
                .whereGreaterThan(DOC_FIELD_UPDATED_TIMESTAMP, aWeekAgo)
                .getResultsUnchecked().size
    }

    fun findUserId(fbuid: String, fxuid: String): String? {
        return if (fxuid.isEmpty()) {
            findUserIdByFbUid(fbuid)
        } else {
            findUserIdByFxUid(fxuid)
        }
    }

    private fun findUserIdByFxUid(fxUid: String): String? {
        return users.whereEqualTo(DOC_FIELD_USER_FIREFOX_UID, fxUid)
                .getResultsUnchecked()
                .firstOrNull()
                ?.getString("uid")
    }

    private fun findUserIdByFbUid(fbUid: String): String? {
        return users.whereEqualTo(DOC_FIELD_USER_FIREBASE_UID, fbUid)
                .getResultsUnchecked()
                .firstOrNull()
                ?.getString("uid")
    }

    private fun findUserDocumentIdByFbUid(fbUid: String): String? {
        return findUserDocSnapshot(DOC_FIELD_USER_FIREBASE_UID, fbUid)?.id
    }

    private fun findUserDocumentIdByFxUid(fxUid: String): String? {
        return findUserDocSnapshot(DOC_FIELD_USER_FIREFOX_UID, fxUid)?.id
    }

    private fun findUserDocumentByFxUid(fxUid: String): UserDoc? {
        return findUserDocSnapshot(DOC_FIELD_USER_FIREFOX_UID, fxUid)?.toObject(UserDoc::class.java)
    }

    private fun findUserDocSnapshot(field: String, fxUid: String): QueryDocumentSnapshot? {
        return users.whereEqualTo(field, fxUid).getResultsUnchecked().getOrNull(0)
    }


    //.toObject(UserDoc::class.java)
    private fun logUserActivity(userDocumentId: String, action: String) {
        UserActivityDoc(userDocumentId, clock.millis(), action).let {

            userActivity.document().set(it)

            logger.info("log UserDoc[$userDocumentId] has action [$action] ")
        }
    }
}

sealed class LoginResponse {
    class Success(val message: String) : LoginResponse()
    class SuspiciousWarning(val message: String) : LoginResponse() // a special version of failure
    class UserSuspended(val message: String) : LoginResponse()  // a special version of failure
    class Fail(val message: String) : LoginResponse() // Business logic
    class Error(val message: String) : LoginResponse() // server error
}