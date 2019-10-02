package org.mozilla.msrp.platform.vertical.news

import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named

@Named
class Liputan6RssFeedRepository @Inject constructor(private val liputan6RssFeedClient: Liputan6RssFeedClient) {

    val log = logger()

    fun news(topic: String): List<FeedItem>? {

        val rss = liputan6RssFeedClient.rss(topic).execute().body()
        log.info("[NEWS]====loading indonesia news Liputan6 [${rss?.feedItems?.size}]")
        return rss?.feedItems
    }
}
