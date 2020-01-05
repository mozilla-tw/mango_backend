package org.mozilla.msrp.platform.vertical.video

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.util.logger
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named

@Named
class VideoCacheRepository @Inject constructor(
        private val firestore: Firestore) {

    var videoCollection: CollectionReference = firestore.collection("video")

    @Inject
    lateinit var clock: Clock

    private var logger = logger()

    fun get(key: String): String? {
        val document = videoCollection.whereEqualTo("key", key).getResultsUnchecked().firstOrNull()?.toObject(VideoListDoc::class.java)
        if (document == null) {
            logger.info("[VideoRepository][get] No such data for key[$key]")
        } else {
            logger.info("[VideoRepository][get] Found data for key[$key]")
        }
        return document?.data
    }

    fun set(key: String, writeValueAsString: String) {
        firestore.runTransaction { transaction ->
            val id = transaction.get(videoCollection.whereEqualTo("key", key)).getUnchecked()?.documents?.firstOrNull()?.id
            val document = if (id == null) {
                logger.info("[VideoRepository][set] No such data for key[$key], creating a new document...")

                videoCollection.document()

            } else {
                logger.info("[VideoRepository][set] Found data for key[$key], document id[$id]")
                videoCollection.document(id)
            }
            document.set(VideoListDoc(key, writeValueAsString), SetOptions.merge())
        }
    }
}

data class VideoListDoc(
        val key: String = "",
        val data: String = ""
)