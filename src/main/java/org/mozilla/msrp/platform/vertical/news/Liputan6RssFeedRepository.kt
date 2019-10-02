package org.mozilla.msrp.platform.vertical.news

import javax.inject.Inject
import javax.inject.Named

@Named
class Liputan6RssFeedRepository @Inject constructor(private val liputan6RssFeedClient: Liputan6RssFeedClient) {

    fun news(topic: String): List<FeedItem>? {

        val rss = liputan6RssFeedClient.rss(topic).execute().body()
        println(rss?.feedItems?.size)
        return rss?.feedItems
    }
}
