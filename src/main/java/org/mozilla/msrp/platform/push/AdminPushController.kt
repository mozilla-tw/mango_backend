package org.mozilla.msrp.platform.push

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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.inject.Inject


@RestController
class AdminPushController @Inject constructor(private val jwtHelper: JwtHelper) {

    private val PROJECT_ID: String = ServiceOptions.getDefaultProjectId()

    /**
     * Show Push ADMIN UI to verified users
     * */
    @GetMapping("/api/v1/admin/push/main")
    fun adminPush(@RequestParam token: String, model: Model): String {
//        val role = jwtHelper.verify(token)?.role
//        if (role == JwtHelper.ROLE_MSRP_ADMIN) {
//            model.addAttribute("token", token)
        return "push"
//        }
    }

    /**
     * Receive message created from Frontend
     * */
    @PostMapping("/api/v1/admin/push/enqueue")
    fun enqueuedMessageRequest(
            @RequestParam token: String,
            @RequestParam recipients: Array<String>,
            @RequestParam recipientType: String,
            @RequestParam title: String,
            @RequestParam body: String,
            @RequestParam destination: String,
            @RequestParam displayType: String,
            @RequestParam displayTimestamp: Long,
            @RequestParam mozMessageId: String,
            @RequestParam appId: String,
            @RequestParam(required = false, defaultValue = "") imageUrl: String): String {
//        val verify = jwtHelper.verify(token)
//        if (verify?.role != JwtHelper.ROLE_PUSH_ADMIN) {
//            return "401"
//        }
        // FIXME: consider  checking the input here.

        val sender = "nevintest" //verify.email
        val topicId = "push"
        val topicName = ProjectTopicName.of(PROJECT_ID, topicId)
        var publisher: Publisher? = null
        val futures: MutableList<ApiFuture<String>> = ArrayList()

        var error: String? = null

        // TODO: write log to DB
        try { // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build()
            for (recipient in recipients) {
                val recipientToken = recipient  // TODO: add exchange here
                // TODO: Make it a POJO. Log DB will use it too. consider JSON from f2e too
                val fcmRequest = FcmRequest(
                        recipientToken = recipientToken,
                        title = title,
                        body = body,
                        destination = destination,
                        displayType = displayType,
                        displayTimestamp = displayTimestamp,
                        mozMessageId = mozMessageId,
                        appId = appId,
                        imageUrl = imageUrl,
                        sender = sender

                )
                val message = PushNotificationHelper.conductPushPayload(fcmRequest)

                // convert message to bytes
                val data: ByteString = ByteString.copyFromUtf8(message)
                val pubsubMessage = PubsubMessage.newBuilder()
                        .setData(data)
                        .build()
                // Schedule a message to be published. Messages are automatically batched.
                val future: ApiFuture<String> = publisher.publish(pubsubMessage)
                futures.add(future)
                // FIXME: consider the error logging here
            }
            // FIXME: consider the error logging here
        } catch (e: Exception) {
            // FIXME: consider the error logging here
            error = "error ${e.localizedMessage}"
            println(error)
        } finally { // Wait on any pending requests
            // FIXME logging one by one?
            val messageIds = ApiFutures.allAsList(futures).get()
            // TODO: write log to DB
//            for (messageId in messageIds) {
//                println(messageId)
//            }
            if (messageIds.size != recipients.size) {
                // FIXME: DO we want to rollback the queue here?
                publisher?.shutdown()
                return " Expecting ${recipients.size} FCM request enqueued, but got ${messageIds.size}"
            }
            // FIXME: consider the error logging here
            publisher?.shutdown()
            val finalResult = error ?: "${messageIds.size} messages sent!"
            print(finalResult)
            return finalResult
        }

    }
}

data class FcmRequest(
        val recipientToken: String,
        val title: String,
        val body: String,
        val destination: String,
        val displayType: String,
        val displayTimestamp: Long,
        val mozMessageId: String,
        val appId: String,
        val imageUrl: String,
        val sender: String,
        val pushId: String = UUID.randomUUID().toString(),
        val createdTimestamp: Long = System.currentTimeMillis()
)

object PushNotificationHelper {
    private val mapper = Gson()
    fun conductPushPayload(
            fcmRequest: FcmRequest): String {
        val fcmOptions = FcmOptions.builder().setAnalyticsLabel(fcmRequest.mozMessageId).build()
        val push = Message.builder()
                .setToken(fcmRequest.recipientToken)
                .putData("body", fcmRequest.body)
                .putData("title", fcmRequest.title)
                .putData("appId", fcmRequest.appId)
                .putData("destination", fcmRequest.destination)
                .putData("displayType", fcmRequest.displayType)
                .putData("imageUrl", fcmRequest.imageUrl)
                .putData("pushId", fcmRequest.pushId)
                .putData("recipientToken", fcmRequest.recipientToken)
                .putData("sender", fcmRequest.sender)
                .setFcmOptions(fcmOptions).build()
        return mapper.toJson(PushPayload(push))
    }
}