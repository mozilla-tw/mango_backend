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
        newsProperties: NewsProperties) {

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
    fun googleNewsTopic() = listOf(TOPIC_GOOGLE_TOP_NEWS, "WORLD", "NATION", "BUSINESS", "TECHNOLOGY", "ENTERTAINMENT", "SPORTS", "SCIENCE", "HEALTH")

    @GetMapping("/api/v1/news/google/topics/tw")
    fun googleNewsTopicTw() = listOf("WORLD", "NATION", "BUSINESS", "TECHNOLOGY", "ENTERTAINMENT", "SPORTS", "SCIENCE", "HEALTH")


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
                            val liputan6Url = toLiputan6Url(topic)
                            val detik6Url = toDetik6Url(topic)
                            if (liputan6Url == null && detik6Url == null) {
                                return listOf()
                            }
                            return indonesiaNewsFeedService.getNews(liputan6Url, detik6Url) ?: listOf()
                        }
                    })

    private fun toDetik6Url(topic: String): String? {
        return detikUrl[topic]

    }

    private fun toLiputan6Url(topic: String): String? {
        return liputan6Url[topic]
    }

    @GetMapping("/api/v1/news/indonesia/topic/{topic}")
    internal fun indonesiaNewsByTopic(
            @PathVariable("topic") topic: String): ResponseEntity<in Any> {
        if (liputan6Url[topic] == null) {
            log.info("[NEWS]====No news for topic $String")
            return ResponseEntity("No such topic", HttpStatus.BAD_REQUEST)
        }
        val newsItems = cacheIndonesiaNews.get(topic)
        log.info("[NEWS]====loading indonesia news [${newsItems.size}]")
        if (newsItems.isEmpty()) {
            log.info("[NEWS]====No news for topic $String")
            return ResponseEntity("No news for topic", HttpStatus.NO_CONTENT)
        }
        log.info("[NEWS]====found [${newsItems.size}] news item for topic $topic")
        return ResponseEntity(newsItems, HttpStatus.OK)
    }



    @GetMapping("/api/v1/news/indonesia/topics")
    fun idNewsTopic() = liputan6Url.keys

    companion object {

        private const val delimiters = "=="
        private const val TOPIC_GOOGLE_TOP_NEWS = "Top-news"

        private val liputan6Url = mapOf(
                "NEWS" to "https://feed.liputan6.com/mozilla?categories[]=17&source=Digital%20Marketing&medium=Partnership",
                "TEKNO" to "https://feed.liputan6.com/mozilla?categories[]=8&source=Digital%20Marketing&medium=Partnership",
                "GLOBAL" to "https://feed.liputan6.com/mozilla?categories[]=274&source=Digital%20Marketing&medium=Partnership",
                "HEALTH INFO" to "https://feed.liputan6.com/mozilla?categories[]=9&source=Digital%20Marketing&medium=Partnership",
                "BOLA" to "https://feed.liputan6.com/mozilla?categories[]=11&source=Digital%20Marketing&medium=Partnership",
                "E-SPORTS" to "https://feed.bola.com/mozilla?categories[]=1071&source=Digital%20Marketing&medium=Partnership",
                "RAGAM" to "https://feed.bola.com/mozilla?categories[]=486&source=Digital%20Marketing&medium=Partnership",
                "INDONESIA" to "https://feed.bola.com/mozilla?categories[]=423&source=Digital%20Marketing&medium=Partnership",
                "SHOWBIZ" to "https://feed.liputan6.com/mozilla?categories[]=13&source=Digital%20Marketing&medium=Partnership",
                "FASHION" to "https://feed.fimela.com/mozilla?categories[]=842&source=Digital%20Marketing&medium=Partnership",
                "PARENTING" to "https://feed.fimela.com/mozilla?categories[]=1065&source=Digital%20Marketing&medium=Partnership",
                "LIFESTYLE" to "https://feed.fimela.com/mozilla?categories[]=908&source=Digital%20Marketing&medium=Partnership",
                "ZODIAK" to "https://feed.fimela.com/mozilla?categories[]=601&source=Digital%20Marketing&medium=Partnership")

        private val detikUrl = mapOf(
                "NEWS" to "http://rss.detik.com/index.php/hot",
                "TEKNO" to "http://rss.detik.com/index.php/inet",
                "HEALTH INFO" to "http://rss.detik.com/index.php/health",
                "RAGAM" to "http://rss.detik.com/index.php/sport")
    }
}