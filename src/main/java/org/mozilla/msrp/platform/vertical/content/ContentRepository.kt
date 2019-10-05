package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.firestore.toObject
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.data.Category
import org.mozilla.msrp.platform.vertical.content.db.PublishControlDoc
import org.mozilla.msrp.platform.vertical.content.db.PublishDoc
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Clock
import javax.inject.Inject

@Repository
class ContentRepository @Inject constructor(private var storage: Storage,
                                            private var firestore: Firestore) {

    private var publish: CollectionReference
    private var publishHistory: CollectionReference
    private var publishControl: CollectionReference

    private val log = logger()

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var mapper: ObjectMapper

    companion object {
        private const val COLLECTION_PUBLISH = "publish"
        private const val COLLECTION_PUBLISH_HISTORY = "publish_history"
        private const val COLLECTION_PUBLISH_CONTROL = "publish_control"
    }

    init {
        publish = firestore.collection(COLLECTION_PUBLISH)
        publishHistory = firestore.collection(COLLECTION_PUBLISH_HISTORY)
        publishControl = firestore.collection(COLLECTION_PUBLISH_CONTROL)
    }

    fun getContent(contentRepoQuery: ContentRepoQuery): ContentRepoResult {
        return try {
            val path = "v1/${contentRepoQuery.category}/${contentRepoQuery.locale}/"
            val blobId = BlobId.of("rocket-admin-dev", "${path}data.json")
            val bytes = storage.readAllBytes(blobId)
            val str = String(bytes, UTF_8)

            ContentRepoResult.Success(mapper.readValue(str, Category::class.java))
        } catch (e: StorageException) {
            val message = "error loading games"
            log.error("[Games]====$message====$e")
            ContentRepoResult.Fail(message)
        }
    }

    // This version of getContent gets content from Firestore, not Cloud Storage
    fun getContentFromDB(contentRepoQuery: ContentRepoQuery): ContentRepoResult {
        return try {

            val publishDocId = pickPublishControl(contentRepoQuery.category, contentRepoQuery.locale).getUnchecked()["publishDocId"] as? String
            if (publishDocId == null) {
                val message = "[Content]====No result for :$contentRepoQuery"
                log.warn(message)
                return ContentRepoResult.Fail(message)
            }
            val publishDoc = publish.document(publishDocId).getUnchecked().toObject(PublishDoc::class.java)
            val data = publishDoc?.data
            if (data == null) {
                val message = "[Content]====getContentFromDB====No such Document===="
                log.error("$message$publishDocId")
                ContentRepoResult.Fail(message)
            } else {
                ContentRepoResult.Success(data)
            }
        } catch (e: StorageException) {
            val message = "error loading games"
            log.error("[Content]====$message:${e.localizedMessage}")
            ContentRepoResult.Fail(message)
        }
    }

    fun addContent(request: AddContentRequest): String? {
        // todo: consider make it a transaction
        try {
            val publishControlDoc: PublishControlDoc? = getPublishControlDoc().getUnchecked().toObject(PublishControlDoc::class.java)
            if (publishControlDoc == null) {
                log.error("[ContentRepository][addContent]====Can't get PublishControlDoc document")
                return null

            }
            // this is slow. But we don't have too much write for now so we don't use distributed counter
            val newVersion = publishControlDoc.version++
            // write the new version first in case the next operation fails
            getPublishControlDoc().set(publishControlDoc, SetOptions.merge())

            val publishDoc = PublishDoc(
                    newVersion,
                    request.category,
                    request.locale,
                    clock.millis(),
                    mapper.readValue(request.data, Category::class.java))
            val document = publish.document()
            document.set(publishDoc).getUnchecked()
            return document.id
        } catch (e: Exception) {
            log.error("[ContentRepository][addContent]====$e")
            return null
        }
    }

    fun publish(category: String, locale: String, publishDocId: String, editorUid: String) {
        try {
            // TODO: make it a transaction
            pickPublishControl(category, locale).set(
                    mapOf("publishDocId" to publishDocId), SetOptions.merge()).getUnchecked()

            updatePublishHistory(publishDocId, editorUid)

        } catch (e: Exception) {
            log.error("[Content][error]====publish====$e")
        }
    }

    private fun updatePublishHistory(publishDocId: String, editorUid: String) {
        publishHistory.document().set(
                mapOf("publishDocId" to publishDocId,
                        "created_timestamp" to clock.millis(),
                        "editorUid" to editorUid
                )).getUnchecked()
    }

    private fun pickPublishControl(category: String, locale: String) = publishControl.document("v1").collection(category).document(locale)
    private fun getPublishControlDoc() = publishControl.document("v1")


    fun queryPublish(contentRepoQuery: ContentRepoQuery): List<PublishDoc> {
        return try {
            val search = publish
                    .whereEqualTo("category", contentRepoQuery.category)
                    .whereEqualTo("locale", contentRepoQuery.locale)
            if (contentRepoQuery.tag != null) {
                search.whereEqualTo("tag", contentRepoQuery.tag)
            }
            search.getResultsUnchecked().mapNotNull {
                it.toObject(PublishDoc::class.java, mapper)
            }
        } catch (e: Exception) {
            log.error("[Content][error]====queryPublish====[$contentRepoQuery]====$e")
            listOf()
        }
    }

    fun getContentByPublishDocId(publishDocId: String): PublishDoc? {
        return publish.document(publishDocId).getUnchecked().toObject(PublishDoc::class.java)
    }
    /**
    publish/{publishID}/
    schema_version{v1,v2}
    type{coupon/deals}
    locale{id-ID/en-IN}
    data:
    /*JSON for client*/
    publishHistory/{historyId}
    publishDocId
    publish Date
    editorUid
    publishControl       // only one document.
    v1/content_shopping/
     */
}

class AddContentRequest(
        val tag: String,
        val category: String,
        val locale: String,
        val data: String
)




sealed class ContentRepoResult {
    class Success(val category: Category) : ContentRepoResult()
    class Fail(val message: String) : ContentRepoResult()
}

data class ContentRepoQuery(
        val category: String,
        val locale: String,
        var tag: String? = null
)