package org.mozilla.msrp.platform.push.model

import com.google.gson.GsonBuilder
import java.util.UUID

data class WorkerRequest(
        val mozClientIds: MutableList<String>,
        val title: String,
        val body: String,
        val destination: String,
        val displayType: String,
        val displayTimestamp: Long,
        val mozMessageId: String,
        val mozMsgBatch: String,
        val appId: String,
        var imageUrl: String?,
        val sender: String,
        val pushId: String = UUID.randomUUID().toString(),
        val createdTimestamp: Long = System.currentTimeMillis(),
        var pushCommand: String?,
        var pushOpenUrl: String?,
        var pushDeepLink: String?) {
    fun toData(): String {
        if(imageUrl.isNullOrEmpty()){
            imageUrl = null
        }
        if(pushOpenUrl.isNullOrEmpty()){
            pushOpenUrl = null
        }
        if(pushCommand.isNullOrEmpty()){
            pushCommand = null
        }
        if(pushDeepLink.isNullOrEmpty()){
            pushDeepLink = null
        }
        return mapper.toJson(this)
    }

    companion object {
        private val mapper = GsonBuilder().disableHtmlEscaping().create()
    }
}
