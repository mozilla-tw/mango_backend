package org.mozilla.msrp.platform.push

import com.google.common.collect.Lists
import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.common.isDev
import org.mozilla.msrp.platform.common.isStableDev
import org.mozilla.msrp.platform.push.model.AdminPushAgentRequest
import org.mozilla.msrp.platform.push.model.WorkerRequest
import org.mozilla.msrp.platform.push.service.MessageQueueService
import org.mozilla.msrp.platform.push.service.PushLogService
import org.mozilla.msrp.platform.push.service.StmoService
import org.mozilla.msrp.platform.push.service.StmoServiceResponse
import org.mozilla.msrp.platform.push.util.PushAdminMetrics
import org.mozilla.msrp.platform.util.logger
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
class AdminPushAgentController @Inject constructor(
        private val jwtHelper: JwtHelper,
        private val pushLogService: PushLogService,
        private val messageQueueService: MessageQueueService,
        private val stmoService: StmoService) {

    @Inject
    lateinit var environment: Environment

    /**
     * Receive message created from Frontend
     * */
    @PostMapping("/api/v1/admin/push/enqueue")
    fun enqueuedMessageRequest(
            @RequestParam token: String,
            @RequestParam stmoUrl: String,
            @RequestParam title: String,
            @RequestParam body: String,
            @RequestParam destination: String,
            @RequestParam displayType: String,
            @RequestParam displayTimestamp: Long,
            @RequestParam mozMessageId: String,
            @RequestParam mozMsgBatch: String,
            @RequestParam appId: String,
            @RequestParam(required = false) imageUrl: String?,
            @RequestParam(required = false) pushOpenUrl: String?,
            @RequestParam(required = false) pushDeepLink: String?): ResponseEntity<String> {

        val logger = logger()
        // bypass verification in dev/stable environment
        val sender = if (environment.isStableDev || environment.isDev) {
            "testing" // for testing
        } else {
            val verify = jwtHelper.verify(token)
            if (verify?.role != JwtHelper.ROLE_PUSH_ADMIN) {
                return ResponseEntity("Please login First", HttpStatus.UNAUTHORIZED)
            }
            verify.email
        }

        // FIXME: consider  checking the input here.
        val input = AdminPushAgentRequest(stmoUrl = stmoUrl.trim(),
                title = title,
                body = body,
                destination = destination.trim(),
                displayType = displayType.trim(),
                displayTimestamp = displayTimestamp,
                mozMessageId = mozMessageId.trim(),
                mozMsgBatch = mozMsgBatch.trim(),
                appId = appId,
                imageUrl = imageUrl?.trim(),
                sender = sender,
                pushOpenUrl = pushOpenUrl?.trim(),
                pushDeepLink = pushDeepLink?.trim())
        val validate = input.validate()
        if (validate.isNotEmpty()) {
            logger.warn("[push][validate]$validate")
            return ResponseEntity(validate, HttpStatus.BAD_REQUEST)
        }
        val result = addWorkRequest(input)
        when (result) {
            is AddWorkResponse.Success -> {
                val msg = "MessageId [$mozMessageId] with title[$title] created [${result.jobCount}] jobs.<br>[Ids]<br>${result.debugStmo}"
                logger.info("[push][enqueue]$msg")
                return ResponseEntity(msg, HttpStatus.OK)
            }
            is AddWorkResponse.Error -> {
                logger.error("[push][enqueue]${result.message}")
                return ResponseEntity("Error:[${result.message}] ", HttpStatus.BAD_REQUEST)
            }
            else -> {
                logger.error("[push][enqueue]Unexpected")
                return ResponseEntity("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    private fun addWorkRequest(input: AdminPushAgentRequest): AddWorkResponse {

        var debugStmo = StringBuffer()
        val stmoResponse = stmoService.loadClientIds(input.stmoUrl)
        val mozClientIds = when (stmoResponse) {
            is StmoServiceResponse.Success -> {
                stmoResponse.list.forEach { str -> debugStmo.append(str).append("<BR>") }
                stmoResponse.list
            }
            is StmoServiceResponse.Error -> {
                PushAdminMetrics.event(PushAdminMetrics.PUSH_STMO_ERROR, stmoResponse.error)
                return AddWorkResponse.Error(stmoResponse.error)
            }
            else -> listOf()
        }
        // todo: do it more elegantly
        // partition the client id from sql.telemetry.org, delegate them to work in batch (every TOKEN_PER_WORKER)
        Lists.partition(mozClientIds, TOKEN_PER_WORKER).forEach { subList ->

            val data = WorkerRequest(
                    mozClientIds = subList,
                    title = input.title,
                    body = input.body,
                    destination = input.destination,
                    displayType = input.displayType,
                    displayTimestamp = input.displayTimestamp,
                    mozMessageId = input.mozMessageId,
                    mozMsgBatch = input.mozMsgBatch,
                    appId = input.appId,
                    imageUrl = input.imageUrl,
                    sender = input.sender,
                    pushCommand = null, //not used
                    pushOpenUrl = input.pushOpenUrl,
                    pushDeepLink = input.pushDeepLink).toData()
            addWork(data)
        }
        return AddWorkResponse.Success(messageQueueService.close(), debugStmo.toString())
    }

    private fun addWork(data: String) {
        // write data to db
        val addWork = pushLogService.addWork(data)
        logger().info("[push][agent][addWork]$addWork")
        if (addWork != null) {
            // push messages to Pub/Sub topic
            messageQueueService.pushAsync(addWork.toString())
        }
    }

    companion object {
        private const val TOKEN_PER_WORKER = 500
    }
}

sealed class AddWorkResponse {
    class Success(val jobCount: Int, val debugStmo: String) : AddWorkResponse()
    class Error(val message: String) : AddWorkResponse()
}