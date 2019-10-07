package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
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
import org.threeten.bp.LocalDateTime
import java.lang.Exception
import java.nio.charset.StandardCharsets.UTF_8
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.util.TimeZone
import javax.inject.Inject

@Repository
class ContentRepository @Inject constructor(private var storage: Storage,
                                            private var firestore: Firestore) {

    private var publish: CollectionReference
    private var publishHistory: CollectionReference

    private val log = logger()

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var mapper: ObjectMapper

    companion object {
        private const val COLLECTION_PUBLISH = "publish"
        private const val COLLECTION_PUBLISH_HISTORY = "publish_history"
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd").apply {
            this.timeZone = TimeZone.getTimeZone("UTC");
        }
    }

    init {
        publish = firestore.collection(COLLECTION_PUBLISH)
        publishHistory = firestore.collection(COLLECTION_PUBLISH_HISTORY)
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

            val publishDocId: QueryDocumentSnapshot? = pickPublishControl(contentRepoQuery.category, contentRepoQuery.locale)
            if (publishDocId == null) {
                val message = "[Content][getContentFromDB]====No result for :$contentRepoQuery"
                log.warn(message)
                return ContentRepoResult.Fail(message)
            }
            val publishDoc = publishDocId.toObject(PublishDoc::class.java)
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
//            val publishControlDoc: PublishControlDoc? = getPublishControlDoc().getUnchecked().toObject(PublishControlDoc::class.java)
//            if (publishControlDoc == null) {
//                log.error("[ContentRepository][addContent]====Can't get PublishControlDoc document")
//                return null
//
//            }
//            // this is slow. But we don't have too much write for now so we don't use distributed counter
//            val newVersion = publishControlDoc.version++
//            // write the new version first in case the next operation fails
//            getPublishControlDoc().set(publishControlDoc, SetOptions.merge())

            val publishDoc = PublishDoc(
                    null,
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

    fun publish(publishDocId: String, editorUid: String, schedule: String) {
        try {

            val timestamp = if (schedule.isEmpty()) {
                Instant.now().toEpochMilli()
            } else {
                simpleDateFormat.parse(schedule).time
            }

            val docRef = publish.document(publishDocId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                transaction.update(docRef, "publish_timestamp", timestamp)
            }

            updatePublishHistory(publishDocId, editorUid)

        } catch (e: ParseException) {
            log.error("[Content][error]====publish====$e")

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

    private fun pickPublishControl(category: String, locale: String): QueryDocumentSnapshot? {
        return publish.whereEqualTo("category", category)
                .whereEqualTo("locale", locale)
                .whereGreaterThan("publish_timestamp", Instant.now().toEpochMilli())
                .orderBy("publish_timestamp", Query.Direction.DESCENDING)
                .limit(1).getResultsUnchecked().firstOrNull()
    }

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
    publish_date{Long, null if not ready for publish})
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
        val data: String,
        var publishDate: Long? = null
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