package org.mozilla.msrp.platform.push.model

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
        var pushCommand: String?,
        var pushOpenUrl: String?,
        var pushDeepLink: String?)
