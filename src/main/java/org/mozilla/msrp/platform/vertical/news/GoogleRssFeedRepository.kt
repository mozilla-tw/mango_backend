package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named


@Named
class GoogleRssFeedRepository @Inject constructor(private val googleRssFeedClient: GoogleRssFeedClient) {

    fun news(language: String): List<FeedItem>? {
        val rss = googleRssFeedClient.rss(language).execute().body()
        return rss?.feedItems
    }

    fun news(language: String, topic: String): List<FeedItem>? {


        val rss = googleRssFeedClient.rss(topic, language).execute().body()
        println(rss?.feedItems?.size)
        return rss?.feedItems
    }

}