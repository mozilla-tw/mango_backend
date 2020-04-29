package org.mozilla.msrp.platform.push.model

import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

data class AdminPushAgentRequest(
        val stmoUrl: String,
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
        var pushOpenUrl: String?,
        var pushDeepLink: String?) {
    fun validate(): String {
        var error = ""
        if (invalidTimestamp(displayTimestamp)) {
            error += " [displayTimestamp is invalid] "
        }
        if (stmoUrl.isEmpty()){
            error += " [stmoUrl is empty] "
        }
        if (title.isEmpty()){
            error += " [title is empty] "
        }
        if (mozMessageId.isEmpty()){
            error += " [mozMessageId is empty] "
        }
        if (invalidUrl(stmoUrl)) {
            error += " [stmoUrl is invalid] "
        }
        if (invalidUrl(imageUrl)) {
            error += " [imageUrl is invalid] "
        }
        if (invalidUrl(pushOpenUrl)) {
            error += " [pushOpenUrl is invalid] "
        }

        if (invalidLink(pushOpenUrl, pushDeepLink)) {
            error += " [choose either pushDeepLink or pushOpenUrl] "
        }

        return error
    }

    private fun invalidTimestamp(ts: Long): Boolean {
        return ts - System.currentTimeMillis() <= 0
    }

    private fun invalidUrl(url: String?): Boolean {
        if (url?.isNotEmpty() == true) {
            try {
                URL(url)
            } catch (e: MalformedURLException) {
                return true
            }
        }
        return false
    }

    private fun invalidLink(pushDeepLink: String?, pushOpenUrl: String?): Boolean {
        return pushDeepLink?.isNotEmpty() == true && pushOpenUrl?.isNotEmpty() == true
    }
}
