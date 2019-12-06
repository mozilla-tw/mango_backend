package org.mozilla.msrp.platform.vertical.news

import org.mozilla.msrp.platform.util.logger
import javax.inject.Inject
import javax.inject.Named


@Named
class GoogleRssFeedRepository @Inject constructor(private val googleRssFeedClient: GoogleRssFeedClient) {

    val log = logger()

    fun news(language: String): List<FeedItem>? {
        val rss = googleRssFeedClient.rss(language).execute().body()
        log.info("[NEWS]====loading Google news [${rss?.feedItems?.size}]")
        return rss?.feedItems
    }

    fun news(topic: String, hl: String, gl: String, ceid: String): List<FeedItem>? {


        val rss = googleRssFeedClient.rss(topic, hl, gl, ceid).execute().body()
        log.info("[NEWS]====loading Google news [${rss?.feedItems?.size}]")

        return rss?.feedItems
    }

    fun topNews(hl: String, gl: String, ceid: String): List<FeedItem>? {
        val rss = googleRssFeedClient.topNewsRss(hl, gl, ceid).execute().body()
        log.info("[NEWS]====loading Google news [${rss?.feedItems?.size}]")
        return rss?.feedItems
    }

}