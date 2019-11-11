package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.data.ContentSubcategory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@Configuration
open class ContentCacheProvider @Inject constructor(private val contentService: ContentService) {

    companion object {
        private const val CACHE_SIZE = 10L
        private const val CACHE_TIME_MINUTES = 15L
    }

    @Bean("ContentCache")
    open fun provideContentCache(): LoadingCache<ContentServiceQueryParam, ContentServiceQueryResult> = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .refreshAfterWrite(CACHE_TIME_MINUTES, TimeUnit.MINUTES)
            .recordStats()
            .build(object : CacheLoader<ContentServiceQueryParam, ContentServiceQueryResult>() {
                override fun load(param: ContentServiceQueryParam): ContentServiceQueryResult {
                    return contentService.getContent(param.category, param.locale)
                }
            })
}

@RestController
class ContentController @Inject constructor(
        @Named("ContentCache") val cacheContent: LoadingCache<ContentServiceQueryParam, ContentServiceQueryResult>) {

    private val log = logger()

    @RequestMapping("/api/v1/content")
    fun getContent(
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String
    ): ResponseEntity<Any> {
        return try {
            when (val result = cacheContent.get(ContentServiceQueryParam(category, locale))) {
                is ContentServiceQueryResult.InvalidParam -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.message)
                is ContentServiceQueryResult.Success -> ResponseEntity.status(HttpStatus.OK).body(ContentResponse(result.version, result.tag, result.data.subcategories))
                is ContentServiceQueryResult.Fail -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.message)
            }
        } catch (e: ExecutionException) {
            log.error("Content: Cache: $category $locale Exception:$e")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Please try again")
        }
    }

}

class ContentServiceQueryParam(
        val category: String,
        val locale: String
)
class ContentResponse(
        val version: Long,
        val tag: String,
        val subcategories: List<ContentSubcategory>
)