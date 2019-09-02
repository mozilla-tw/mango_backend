package org.mozilla.msrp.platform.profile

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class ProfileRepository {
    private var users: CollectionReference
    private var accountActivity: CollectionReference

    init {
        println("------ProfileRepository------")
    }

    companion object {
        private const val COLLECTION_USER = "users"
        private const val COLLECTION_ACCOUNT_ACTIVITY = "account_activity"

        private const val DOC_FIELD_USER_FIREBASE_UID = "firebase_uid"
        private const val DOC_FIELD_USER_FIREFOX_UID = "firefox_uid"
        private const val DOC_FIELD_USER_EMAIL = "email"
        private const val DOC_FIELD_USER_UPDATED_TIMESTAMP = "updated_timestamp"
        private const val DOC_FIELD_USER_STATUS = "status"

        private const val ACCOUNT_ACTIVITY_ACTION_SIGN_IN = "sign-in"
        private const val ACCOUNT_ACTIVITY_ACTION_DEPRECATED = "deprecated"

    }

    constructor() {
        val db = FirestoreClient.getFirestore()
        users = db.collection(COLLECTION_USER)
        accountActivity = db.collection(COLLECTION_ACCOUNT_ACTIVITY)
    }

    fun createCustomToken(fxUid: String?, additionalClaims: Map<String, String>): String? {
        return FirebaseAuth.getInstance().createCustomToken(fxUid, additionalClaims)
    }

    fun signInAndUpdateUserDocument(oldFbUid: String, fxUid: String, email: String) {


        // if there's an user document with firefox_uid == fxuid , mark current anonymous one deprecated
        val currentUserDocId = findUserDocumentIdByFbUid(oldFbUid) ?: return
        val existingUserDocId = findUserDocumentIdByFxUid(fxUid)
        if (existingUserDocId != null) {
            // TODO: prevent the user login twice, or we'll invalid the correct user document
            // TODO: add account activity
            users.document(currentUserDocId).set(mapOf(DOC_FIELD_USER_STATUS to ACCOUNT_ACTIVITY_ACTION_DEPRECATED), SetOptions.merge())
            logAccountActivity(currentUserDocId, ACCOUNT_ACTIVITY_ACTION_DEPRECATED)
            return
        }

        println("oldFbUid======$oldFbUid")
        println("fxUid======$fxUid")
        println("documentId======$currentUserDocId")

        // TODO: add account activity
        val updateData = mapOf(
                DOC_FIELD_USER_FIREFOX_UID to fxUid,
                DOC_FIELD_USER_EMAIL to email,
                DOC_FIELD_USER_UPDATED_TIMESTAMP to System.currentTimeMillis(),
                DOC_FIELD_USER_STATUS to ACCOUNT_ACTIVITY_ACTION_SIGN_IN
        )
        users.document(currentUserDocId).set(updateData, SetOptions.merge())
        logAccountActivity(currentUserDocId, ACCOUNT_ACTIVITY_ACTION_SIGN_IN)
    }

    private fun findUserDocumentIdByFbUid(fbUid: String): String? {
        return findUserDocId(DOC_FIELD_USER_FIREBASE_UID, fbUid)
    }

    private fun findUserDocumentIdByFxUid(fxUid: String): String? {
        return findUserDocId(DOC_FIELD_USER_FIREFOX_UID, fxUid)
    }

    private fun findUserDocId(field: String, fxUid: String): String? {
        val documents = users.whereEqualTo(field, fxUid).get().get().documents
        return if (documents.size >= 1) {
            documents[0].id
        } else {
            null
        }
    }

    private fun logAccountActivity(userDocumentId: String, action: String) {
        AccountActivityDoc(userDocumentId, System.currentTimeMillis(), action, 1).let {
            accountActivity.document().set(it)
        }
    }
}