package org.mozilla.msrp.platform.vertical.news

import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named


@Named
class DetikRssFeedRepository @Inject constructor(private val detikRssFeedClient: DetikRssFeedClient) {

    val log = logger()

    fun news(topic: String): List<FeedItem>? {

        val rss = detikRssFeedClient.rss(topic).execute().body()
        log.info("[NEWS]====loading indonesia news Liputan6 [${rss?.feedItems?.size}]")
        return rss?.feedItems
    }
}