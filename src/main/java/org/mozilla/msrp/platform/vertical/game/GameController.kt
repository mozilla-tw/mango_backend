package org.mozilla.msrp.platform.vertical.game

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.common.UiModel
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
class GameController @Inject constructor(private val gamesRepository: GamesRepository) {

    private val log = logger()

    private val cacheGames = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .refreshAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build(
                    object : CacheLoader<GamesRepoQuery, GamesRepoResult>() {
                        override fun load(key: GamesRepoQuery): GamesRepoResult {
                            return gamesRepository.getGames(key)
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
            @RequestParam(value = "country") country: String): ResponseEntity<String> {

        val safeCategory = categoryMapping[category]
        if (safeCategory == null || !supportLanguageLocale.contains(language)) {
            val message = "Not supported parameters for games: $category/$language/$country"
            log.warn("[Games]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        try {
            return when (val result = cacheGames.get(GamesRepoQuery(safeCategory, language))) {
                is GamesRepoResult.Success -> {
                    ResponseEntity.ok(result.result)
                }
                is GamesRepoResult.Fail -> {
                    val message = "Can't find games for $category/$language/$country"
                    log.warn("[Games]====$message")
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.message)
                }
            }
        } catch (e: ExecutionException) {
            val message = "error loading news"
            log.error("[Games]====$message:${e.localizedMessage}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message)
        }
    }
}
