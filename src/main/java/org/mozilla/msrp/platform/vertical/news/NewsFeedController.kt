package org.mozilla.msrp.platform.vertical.news

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mozilla.msrp.platform.common.property.NewsProperties
import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@RestController
class NewsFeedController @Inject constructor(
        private val googleNewsFeedService: GoogleNewsFeedService,
        private val indonesiaNewsFeedService: IndonesiaNewsFeedService,
        private val newsProperties: NewsProperties) {

    private val log = logger()

    @GetMapping("/api/v1/news/google")
    internal fun googleNews(
            @RequestParam(value = "language") language: String,
            @RequestParam(value = "country") country: String): ResponseEntity<List<FeedItem>> {
        val list = googleNewsFeedService.getNews(language)
                ?: return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        return ResponseEntity(list, HttpStatus.OK)
    }

    @GetMapping("/api/v1/news/google/topic/{topic}")
    internal fun googleNewsByTopic(
            @PathVariable("topic") topic: String,
            @RequestParam hl: String,
            @RequestParam gl: String,
            @RequestParam ceid: String): ResponseEntity<List<FeedItem>> {

        log.info("[NEWS]====loading Google news [$topic][$hl][$gl][$ceid]")

        val containsTopic = googleNewsTopic().contains(topic)
        if (!containsTopic) {
            return ResponseEntity(listOf(), HttpStatus.BAD_REQUEST)
        }
        val key = topic + delimiters + hl + delimiters + gl + delimiters + ceid
        val cache = cacheGoogleNews.get(key)
        log.info("[NEWS]====cache Google news key[${key}]")

        if (cache == null || cache.isEmpty()) {

            return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        }
        log.info("[NEWS]====cache Google news [${cache.size}]")

        return ResponseEntity(cache, HttpStatus.OK)
    }

    @GetMapping("/api/v1/news/google/topics")
    fun googleNewsTopic() = listOf("WORLD", "NATION", "BUSINESS", "TECHNOLOGY", "ENTERTAINMENT", "SPORTS", "SCIENCE", "HEALTH")


    private val cacheGoogleNews = CacheBuilder.newBuilder()
            .maximumSize(newsProperties.cacheSize)
            .refreshAfterWrite(newsProperties.cacheTtl, TimeUnit.MINUTES)
            .recordStats()
            .build(
                    object : CacheLoader<String, List<FeedItem>>() {
                        override fun load(key: String): List<FeedItem> {

                            val split = key.split(delimiters)
                            if (split.size != 4) {
                                return listOf()
                            }
                            val topic = split[0]
                            if (topic == TOPIC_GOOGLE_TOP_NEWS) {
                                return googleNewsFeedService.getTopNews(split[1], split[2], split[3]) ?: listOf()
                            }
                            return googleNewsFeedService.getNews(topic, split[1], split[2], split[3]) ?: listOf()
                        }
                    })


    private val cacheIndonesiaNews = CacheBuilder.newBuilder()
            .maximumSize(newsProperties.cacheSize)
            .refreshAfterWrite(newsProperties.cacheTtl, TimeUnit.MINUTES)
            .recordStats()
            .build(
                    object : CacheLoader<String, List<FeedItem>>() {
                        override fun load(topic: String): List<FeedItem> {
                            return indonesiaNewsFeedService.getNews(topic, detikTopic[topic]) ?: listOf()
                        }
                    })

    @GetMapping("/api/v1/news/indonesia/topic/{topic}")
    internal fun indonesiaNewsByTopic(
            @PathVariable("topic") topic: String): ResponseEntity<in Any> {
        val liputan6Topic = liputan6Topic[topic]
        if (liputan6Topic == null) {
            log.info("[NEWS]====No news for topic $String")
            return ResponseEntity("No such topic", HttpStatus.BAD_REQUEST)
        }
        val newsItems = cacheIndonesiaNews.get(liputan6Topic)
        log.info("[NEWS]====loading indonesia news [${newsItems.size}]")
        if (newsItems.isEmpty()) {
            log.info("[NEWS]====No news for topic $String")
            return ResponseEntity("No news for topic", HttpStatus.NO_CONTENT)
        }
        log.info("[NEWS]====found [${newsItems.size}] news item for topic $topic")
        return ResponseEntity(newsItems, HttpStatus.OK)
    }


    private val liputan6Topic = mapOf(
            "NEWS" to "17",
            "TEKNO" to "8",
            "GLOBAL" to "274",
            "HEALTH INFO" to "9",
            "BOLA" to "11",
            "E-SPORTS" to "1071",
            "RAGAM" to "486",
            "INDONESIA" to "423",
            "SHOWBIZ" to "13",
            "FASHION" to "842",
            "PARENTING" to "1065",
            "LIFESTYLE" to "908")

    private val detikTopic = mapOf(
            "17" to "hot",
            "8" to "inet",
            "9" to "health",
            "486" to "sport")

    @GetMapping("/api/v1/news/indonesia/topics")
    fun idNewsTopic() = liputan6Topic.keys

    companion object {

        private const val delimiters = "=="
        private const val TOPIC_GOOGLE_TOP_NEWS = "Top-news"
    }
}