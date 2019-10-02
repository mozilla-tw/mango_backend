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
class GameController @Inject constructor(private val contentRepository: ContentRepository) {

    private val log = logger()

    private val cacheGames = CacheBuilder.newBuilder()
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
            "html5" to "games_html5",
            "install" to "games_install")
    private val supportLanguageLocale = listOf("id-ID", "en-IN")

    @GetMapping("/api/v1/games/{category}")
    internal fun games(
            @PathVariable(value = "category") category: String,
            @RequestParam(value = "language") language: String,
            @RequestParam(value = "country") country: String): ResponseEntity<Any> {

        val safeCategory = categoryMapping[category]
        if (safeCategory == null || !supportLanguageLocale.contains(language)) {
            val message = "Not supported parameters for games: $category/$language/$country"
            log.warn("[Games]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        try {
            return when (val result = cacheGames.get(ContentRepoQuery(safeCategory, language))) {
                is ContentRepoResult.Success -> {
                    ResponseEntity.ok(result.result)
                }
                is ContentRepoResult.Fail -> {
                    val message = "Can't find games for $category/$language/$country"
                    log.warn("[Games]====$message")
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.message)
                }
            }
        } catch (e: ExecutionException) {
            val message = "error loading games"
            log.error("[Games]====$message, exception=$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message)
        }
    }
}
