package org.mozilla.msrp.platform.push

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutures
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.gson.Gson
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.push.models.FcmOptions
import org.mozilla.msrp.platform.push.models.Message
import org.mozilla.platoform.service.models.PushPayload
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject


@RestController
class AdminPushController @Inject constructor(private val jwtHelper: JwtHelper) {

    private val PROJECT_ID: String = ServiceOptions.getDefaultProjectId()

    /**
     * Show Push ADMIN UI to verified users
     * */
    @GetMapping("/api/v1/admin/push/main")
    fun adminPush(@RequestParam messageCount: Int, model: Model): String {
//        val role = jwtHelper.verify(token)?.role
//        if (role == JwtHelper.ROLE_MSRP_ADMIN) {
//            model.addAttribute("token", token)
        return "push"
//        }
    }

    /**
     * Receive message created from Frontend
     * */
    @GetMapping("/api/v1/admin/push/enqueue")
    fun enqueuedMessageRequest(@RequestParam messageCount: Int, model: Model): String {

        val topicId = "push"
        val topicName = ProjectTopicName.of(PROJECT_ID, topicId)
        var publisher: Publisher? = null
        val futures: MutableList<ApiFuture<String>> = ArrayList()

        try { // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build()
            for (i in 0 until messageCount) {
                val message = PushNotificationHelper.conductPushPayload("token_and_other_para$i")

                // convert message to bytes
                val data: ByteString = ByteString.copyFromUtf8(message)
                val pubsubMessage = PubsubMessage.newBuilder()
                        .setData(data)
                        .build()
                // Schedule a message to be published. Messages are automatically batched.
                val future: ApiFuture<String> = publisher.publish(pubsubMessage)
                futures.add(future)
            }
        } finally { // Wait on any pending requests
            val messageIds = ApiFutures.allAsList(futures).get()
            for (messageId in messageIds) {
                println(messageId)
            }
            if (publisher != null) { // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown()
            }
        }
        return "$messageCount messages sent!"
    }
}


object PushNotificationHelper {
    private val mapper = Gson()

    @Throws(IOException::class)
    fun conductPushPayload(deviceToken: String?): String {
        val fcmOptions = FcmOptions.builder().setAnalyticsLabel("MyLabel").build()
        val push = Message.builder()
                .putData("title", "notification title")
                .putData("body", "notification body")
                .setFcmOptions(fcmOptions)
                .setToken(deviceToken).build()
        return mapper.toJson(PushPayload(push))
    }
}