package org.mozilla.msrp.platform.vertical.news

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
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
        private val indonesiaNewsFeedService: IndonesiaNewsFeedService) {

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
            @RequestParam(value = "language") language: String,
            @RequestParam(value = "country") country: String): ResponseEntity<List<FeedItem>> {

        log.info("[NEWS====loading Google news [$topic][$country]")

        val containsTopic = googleNewsTopic().contains(topic)
        if (!containsTopic) {
            return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        }
        val cache = cacheGoogleNews.get(topic + delimiters + language)
        log.info("[NEWS]====cache Google news [${cache}]")

        if (cache == null || cache.isEmpty()) {
            log.info("[NEWS====cache Google news [${cache.size}]")

            return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        }


        return ResponseEntity(cache, HttpStatus.OK)
    }

    @GetMapping("/api/v1/news/google/topics")
    fun googleNewsTopic() = listOf("WORLD", "NATION", "BUSINESS", "TECHNOLOGY", "ENTERTAINMENT", "SPORTS", "SCIENCE", "HEALTH")


    private val cacheGoogleNews = CacheBuilder.newBuilder()
            .maximumSize(1000)  // todo: hard code value
            .refreshAfterWrite(15, TimeUnit.MINUTES)// todo: hard code value
            .recordStats()
            .build(
                    object : CacheLoader<String, List<FeedItem>>() {
                        override fun load(key: String): List<FeedItem> {

                            val split = key.split(delimiters)
                            if (split.size != 2) {
                                return listOf()
                            }
                            return googleNewsFeedService.getNews(split[1], split[0]) ?: listOf()
                        }
                    })


    private val cacheIndonesiaNews = CacheBuilder.newBuilder()
            .maximumSize(1000)// todo: hard code value
            .refreshAfterWrite(15, TimeUnit.MINUTES)// todo: hard code value
            .recordStats()
            .build(
                    object : CacheLoader<String, List<FeedItem>>() {
                        override fun load(topic: String): List<FeedItem> {
                            return indonesiaNewsFeedService.getNews(topic, detikTopic[topic]) ?: listOf()
                        }
                    })

    @GetMapping("/api/v1/news/indonesia/topic/{topic}")
    internal fun idonesiaNewsByTopic(
            @PathVariable("topic") topic: String): ResponseEntity<in Any> {
        val liputanTopic = liputanTopic[topic]
        if (liputanTopic == null) {
            log.info("[NEWS====No news for topic $String")
            return ResponseEntity("No such topic", HttpStatus.NO_CONTENT)
        }
        val newsItems = cacheIndonesiaNews.get(liputanTopic)
        log.info("[NEWS====loading indonesia news [${newsItems.size}]")
        if (newsItems.isEmpty()) {
            log.info("[NEWS====No news for topic $String")
            return ResponseEntity("No news for topic", HttpStatus.NO_CONTENT)
        }
        log.info("[NEWS====found [${newsItems.size}] news item for topic $String")
        return ResponseEntity(newsItems, HttpStatus.OK)
    }


    private val liputanTopic = mapOf(
            "NEWS" to "17",
            "TEKNO" to "8",
            "GLOBAL" to "274",
            "HEALTH INFO" to "9",
            "BOLA" to "11",
            "E-SPORTS" to "107",
            "RAGAM" to "486",
            "INDONESIAv" to "423",
            "SHOWBIZ" to "13",
            "FASHION" to "842",
            "PARENTING" to "1065",
            "LIFESTYLE" to "908",
            "ZODIAK" to "601")

    private val detikTopic = mapOf(
            "17" to "hot",
            "8" to "inet",
            "9" to "health",
            "486" to "sport")

    @GetMapping("/api/v1/news/indonesia/topics")
    fun idNewsTopic() = liputanTopic.keys

    companion object {

        private const val delimiters = "=="
    }
}