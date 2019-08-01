package org.mozilla.msrp.platform.auth

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.util.*

@Repository
class AuthRepository {

    fun createCustomToken(fxUid: String?, additionalClaims: HashMap<String, Any>) =
            FirebaseAuth.getInstance().createCustomToken(fxUid, additionalClaims)

    fun promoteUserDocument(oldFbUid: String, fxUid: String) {
        val db = FirestoreClient.getFirestore()
        val users = db.collection("users")

        // if there's an user document with uid == fxuid , remove current one
        val fxUserDocumentId = findUserDocumentIdByUid(users, fxUid)
        println("fxUserDocumentId======$fxUserDocumentId")

        if (fxUserDocumentId != null) {
            val dying = findUserDocumentIdByUid(users, oldFbUid)
            println("dying======$dying")

            if (dying != null) {
                users.document(dying).delete()
                return
            }
        }

        val documentId = findUserDocumentIdByUid(users, oldFbUid) ?: return

        println("oldFbUid======$oldFbUid")
        println("oldFbUid======$fxUid")
        println("documentId======$documentId")

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val docData = HashMap<String, Any>()
        docData["isfxa"] = true
        docData["uid"] = fxUid
        docData["update_date"] = currentDate
        docData["update_reason"] = "sign_in_fxa"
        users.document(documentId).set(docData, SetOptions.merge())
    }

    private fun findUserDocumentIdByUid(users: CollectionReference, fxUid: String): String? {
        val documents = users.whereEqualTo("uid", fxUid).get().get().documents
        if (documents.size >= 1) {
            return documents[0].id
        } else {
            return null
        }
    }
}