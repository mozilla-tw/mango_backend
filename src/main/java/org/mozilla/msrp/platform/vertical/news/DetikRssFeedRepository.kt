package org.mozilla.msrp.platform.vertical.news

import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named


@Named
class DetikRssFeedRepository @Inject constructor(private val detikRssFeedClient: DetikRssFeedClient) {

    val log = logger()

    fun news(detikUrl: String): List<FeedItem>? {

        val rss = detikRssFeedClient.fromUrl(detikUrl).execute().body()
        log.info("[NEWS]====loading indonesia news Liputan6 [${rss?.feedItems?.size}]")
        return rss?.feedItems
    }
}