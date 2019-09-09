package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named

@Named
class LiputanRssFeedRepository @Inject constructor(private val liputanRssFeedClient: LiputanRssFeedClient) {

    fun news(topic: String): List<FeedItem>? {

        val rss = liputanRssFeedClient.rss("{$topic}").execute().body()
        println(rss?.feedItems?.size)
        return rss?.feedItems
    }
}
