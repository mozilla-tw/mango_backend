package org.mozilla.msrp.platform.push.service

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutures
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import org.mozilla.msrp.platform.push.util.PushAdminMetrics
import javax.inject.Named

@Named
class MessageQueueService {

    private var futures: MutableList<ApiFuture<String>> = mutableListOf()

    private val projectId: String = ServiceOptions.getDefaultProjectId()
    private val topicName = ProjectTopicName.of(projectId, TOPIC_ID)
    private var publisher: Publisher = Publisher.newBuilder(topicName).build()

    fun pushAsync(workId: String) {

        // convert message to bytes
        val data: ByteString = ByteString.copyFromUtf8(workId)
        val pubsubMessage = PubsubMessage.newBuilder()
                .setData(data)
                .build()
        // Schedule a message to be published. Messages are automatically batched.
        val future: ApiFuture<String> = publisher.publish(pubsubMessage)
        futures.add(future)
    }

    fun close(): Int {
        val messageIds = ApiFutures.allAsList(futures).get()

        var results = ""
        messageIds.parallelStream().forEach {
            results += it
        }
        PushAdminMetrics.event(PushAdminMetrics.ENQUEUE_PUBSUB_RESULT, results)
        val size = messageIds.size
        futures = mutableListOf()
        return size
    }

    companion object {
        private const val TOPIC_ID = "push"

    }
}