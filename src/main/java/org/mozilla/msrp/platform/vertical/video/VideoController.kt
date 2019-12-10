package org.mozilla.msrp.platform.vertical.video

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.VerticalApiInfo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RestController
class VideoController @Inject constructor(
        private val youtubeService: YoutubeService
) {

    @Inject
    lateinit var verticalApiInfo: VerticalApiInfo

    private val log = logger()

    @GetMapping("/api/v1/video")
    internal fun videoListByKeyword(
            @RequestHeader(value = "X-API-Key") apiKey: String?,
            @RequestParam(value = "query") query: String,
            @RequestParam(value = "locale") locale: String,
            @RequestParam(value = "order", required = false, defaultValue = defaultOrder) order: String,
            @RequestParam(value = "limit", required = false, defaultValue = "") limit: String
    ): ResponseEntity<List<VideoItem>> {

        if (apiKey.isNullOrEmpty() || apiKey != verticalApiInfo.clientApiKey) {
            log.error("[VIDEO]====unauthorized request")
            return ResponseEntity(listOf(), HttpStatus.UNAUTHORIZED)
        }

        log.info("[VIDEO]====loading videos [$query][$locale][$order][$limit]")

        val maxResult = limit.toLongOrDefault(0L)
        val key = query + delimiters + locale + delimiters + order + delimiters + maxResult
        val cache = cacheVideos.get(key)
        log.info("[VIDEO]====cache videos key[${key}]")

        if (cache == null || cache.isEmpty()) {
            log.warn("[VIDEO]====no cache found")
            return ResponseEntity(listOf(), HttpStatus.NO_CONTENT)
        }
        log.info("[VIDEO]====cache videos [${cache.size}]")

        return ResponseEntity(cache, HttpStatus.OK)
    }

    private fun String.toLongOrDefault(default: Long): Long {
        return if (isNotEmpty()) {
            try {
                toLong()
            } catch (e: NumberFormatException) {
                default
            }
        } else {
            default
        }
    }

    private val cacheVideos = CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .refreshAfterWrite(cacheTtl, TimeUnit.HOURS)
            .recordStats()
            .build(
                    object : CacheLoader<String, List<VideoItem>>() {
                        override fun load(key: String): List<VideoItem> {
                            val split = key.split(delimiters)
                            if (split.size != 4) {
                                return listOf()
                            }
                            return youtubeService.getVideoList(
                                    split[0],
                                    split[1],
                                    split[2],
                                    split[3].toLong()
                            ) ?: listOf()
                        }
                    }
            )

    companion object {
        private const val delimiters = "=="
        private const val defaultOrder = "relevance"
        private const val cacheSize: Long = 100L
        private const val cacheTtl: Long = 24L
    }
}
