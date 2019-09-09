package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named


@Named
class DetikRssFeedRepository @Inject constructor(private val detikRssFeedClient: DetikRssFeedClient) {

    fun news(topic: String): List<FeedItem>? {

        val rss = detikRssFeedClient.rss(topic).execute().body()
        println(rss?.feedItems?.size)
        return rss?.feedItems
    }
}