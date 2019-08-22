package org.mozilla.msrp.platform.profile

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class ProfileRepository {
    private var users: CollectionReference

    constructor() {
        val db = FirestoreClient.getFirestore()
        users = db.collection("users")
    }

    fun createCustomToken(fxUid: String?, additionalClaims: Map<String, String>): String? {
        return FirebaseAuth.getInstance().createCustomToken(fxUid, additionalClaims)
    }

    fun promoteUserDocument(oldFbUid: String, fxUid: String, email: String) {


        // if there's an user document with firefox_uid == fxuid , mark current anonymous one deprecated
        val currentUserDocId = findUserDocumentIdByFbUid(oldFbUid) ?: return
        val existingUserDocId = findUserDocumentIdByFxUid(fxUid)
        if (existingUserDocId != null) {
            // TODO: prevent the user login twice, or we'll invalid the correct user document
            // TODO: add account activity
            users.document(currentUserDocId).set(mapOf("status" to "deprecated"), SetOptions.merge())
            return
        }

        println("oldFbUid======$oldFbUid")
        println("fxUid======$fxUid")
        println("documentId======$currentUserDocId")

        // TODO: add account activity
        val updateData = mapOf(
                "firefox_uid" to fxUid,
                "email" to email,
                "updated_timestamp" to System.currentTimeMillis(),
                "status" to "sign-in"
        )
        users.document(currentUserDocId).set(updateData, SetOptions.merge())
    }

    private fun findUserDocumentIdByFbUid(fbUid: String): String? {
        return findUserDocId("firebase_uid", fbUid)
    }

    private fun findUserDocumentIdByFxUid(fxUid: String): String? {
        return findUserDocId("firefox_uid", fxUid)
    }

    private fun findUserDocId(field: String, fxUid: String): String? {
        val documents = users.whereEqualTo(field, fxUid).get().get().documents
        return if (documents.size >= 1) {
            documents[0].id
        } else {
            null
        }
    }
}