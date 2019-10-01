package org.mozilla.msrp.platform.vertical.content

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@RestController
class ShoppingController @Inject constructor(private val contentRepository: ContentRepository) {

    private val log = logger()

    private val cacheShopping = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .refreshAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build(
                    object : CacheLoader<ContentRepoQuery, ContentRepoResult>() {
                        override fun load(key: ContentRepoQuery): ContentRepoResult {
                            return contentRepository.getContent(key)
                        }
                    })

    private val categoryMapping = hashMapOf(
            "deal" to "shopping_deal",
            "coupon" to "shopping_coupon")
    private val supportLanguageLocale = listOf("id-ID", "en-IN")

    @GetMapping("/api/v1/shopping/{category}")
    internal fun shopping(
            @PathVariable(value = "category") category: String,
            @RequestParam(value = "language") language: String,
            @RequestParam(value = "country") country: String): ResponseEntity<String> {

        val safeCategory = categoryMapping[category]
        if (safeCategory == null || !supportLanguageLocale.contains(language)) {
            val message = "Not supported parameters for shopping: $category/$language/$country"
            log.warn("[Shopping]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        try {
            return when (val result = cacheShopping.get(ContentRepoQuery(safeCategory, language))) {
                is ContentRepoResult.Success -> {
                    ResponseEntity.ok(result.result)
                }
                is ContentRepoResult.Fail -> {
                    val message = "Can't find shopping for $category/$language/$country"
                    log.warn("[Shopping]====$message")
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.message)
                }
            }
        } catch (e: ExecutionException) {
            val message = "error loading news"
            log.error("[Shopping]====$message====$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message)
        }
    }
}
