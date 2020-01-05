package org.mozilla.msrp.platform.vertical.video

import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.VerticalApiInfo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
class VideoController @Inject constructor(
        private val videoService: VideoService
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
        val cache = videoService.fromCache(query, order, maxResult)
        log.info("[VIDEO]====cache videos params:[$query]")

        if (cache.isEmpty()) {
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

    companion object {
        private const val defaultOrder = "relevance"
    }
}