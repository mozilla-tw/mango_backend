package org.mozilla.msrp.platform.vertical.video

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.mozilla.msrp.platform.metrics.VerticalMetrics
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@Named
class VideoService @Inject constructor(
        private val youtubeClient: YoutubeClient,
        private val videoCacheRepository: VideoCacheRepository
) {

    @Inject
    lateinit var mapper: ObjectMapper

    private val CACHE_THRESHOLD = 30 * 24 * 60 * 60 * 1000L // a month

    fun fromCache(query: String, order: String, maxResult: Long): List<VideoItem> {
        try {
            val content: String? = videoCacheRepository.get(query)
            if (content != null) {
                val cache = mapper.readValue(content, VideoServiceResult::class.java)
                if (cache.videos.isEmpty()) {
                    VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_MISSED, query)
                } else if (System.currentTimeMillis() - cache.ts > CACHE_THRESHOLD) {
                    VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXPIRED, query)
                } else {
                    return cache.videos
                }
            } else {
                VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_MISSED, query)
            }

            return loadData(query, order, maxResult)


        } catch (e: JsonMappingException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$query&JsonMappingException")
            return listOf()
        } catch (e: JsonParseException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$query&JsonParseException")
            return listOf()
        } catch (e: IOException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$query&IOException")
            return listOf()
        }
    }


    private fun getVideoList(query: String, order: String, maxResult: Long): VideoServiceResult {
        return VideoServiceResult(youtubeClient.videoList(query, order, maxResult) ?: listOf())
    }

    private fun loadData(query: String, order: String, maxResult: Long): List<VideoItem> {
        val data = getVideoList(
                query,
                order,
                maxResult
        )
        if (data.videos.isNotEmpty()) {
            videoCacheRepository.set(query, mapper.writeValueAsString(data))
            return data.videos
        }
        return listOf()
    }

    companion object {
        private const val delimiters = "=="
    }
}

data class VideoServiceResult(
        val videos: List<VideoItem>,
        val ts: Long = System.currentTimeMillis()
)