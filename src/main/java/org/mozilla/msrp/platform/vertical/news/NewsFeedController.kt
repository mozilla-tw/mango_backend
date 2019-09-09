package org.mozilla.msrp.platform.vertical.news

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
class NewsFeedController @Inject constructor(
        private val googleNewsFeedService: GoogleNewsFeedService,
        private val indonesiaNewsFeedService: IndonesiaNewsFeedService) {


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
        val list = googleNewsFeedService.getNews(language, topic)
                ?: return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        return ResponseEntity(list, HttpStatus.OK)
    }

    @GetMapping("/api/v1/news/google/topics")
    fun googleNewsTopic() = listOf("WORLD", "NATION", "BUSINESS", "TECHNOLOGY", "ENTERTAINMENT", "SPORTS", "SCIENCE", "HEALTH")


    @GetMapping("/api/v1/news/indonesia/topic/{topic}")
    internal fun idonesiaNewsByTopic(
            @PathVariable("topic") topic: String): ResponseEntity<in Any> {
        val liputanTopic = liputanTopic[topic]
                ?: return ResponseEntity("No such topic", HttpStatus.NO_CONTENT)
        return ResponseEntity(indonesiaNewsFeedService.getNews(liputanTopic, detikTopic[topic]), HttpStatus.OK)
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
            "NEWS" to "hot",
            "TEKNO" to "inet",
            "HEALTH INFO" to "health",
            "RAGAM" to "sport")

    @GetMapping("/api/v1/news/indonesia/topics")
    fun idNewsTopic() = liputanTopic.keys


}