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
        val cacheKey = query

        val content: String? = videoCacheRepository.get(query)
        // if not in cache
        if (content == null) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_MISSED, cacheKey)
            val data = loadData(query, order, maxResult)
            videoCacheRepository.set(cacheKey, mapper.writeValueAsString(data))
            return data.videos
        }
        try {
            val cache = mapper.readValue(content, VideoServiceResult::class.java)
            // Use System.currentTimeMillis() means we can only deploy this service in the same timezone.
            // But it's fine right now.
            if (System.currentTimeMillis()- cache.ts > CACHE_THRESHOLD) {
                VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXPIRED, cacheKey)
                val data = loadData(query, order, maxResult)
                if (data.videos.isNotEmpty()) {
                    videoCacheRepository.set(cacheKey, mapper.writeValueAsString(data))
                    return data.videos
                }
            }
            return cache.videos

        } catch (e: JsonMappingException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$cacheKey&JsonMappingException")
            return listOf()
        } catch (e: JsonParseException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$cacheKey&JsonParseException")
            return listOf()
        } catch (e: IOException) {
            VerticalMetrics.event(VerticalMetrics.EVENT_CACHE_EXCEPTION, "$cacheKey&IOException")
            return listOf()
        }
    }


    private fun getVideoList(query: String, order: String, maxResult: Long): VideoServiceResult {
        return VideoServiceResult(youtubeClient.videoList(query, order, maxResult) ?: listOf())
    }

    private fun loadData(query: String, order: String, maxResult: Long): VideoServiceResult {

        return getVideoList(
                query,
                order,
                maxResult
        )
    }

    companion object {
        private const val delimiters = "=="
    }
}

data class VideoServiceResult(
        val videos: List<VideoItem>,
        val ts: Long = System.currentTimeMillis()
)