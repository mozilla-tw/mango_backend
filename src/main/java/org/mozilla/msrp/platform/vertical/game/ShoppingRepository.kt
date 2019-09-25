package org.mozilla.msrp.platform.vertical.game

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.mozilla.msrp.platform.util.logger
import org.springframework.stereotype.Repository
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

@Repository
class GamesRepository @Inject constructor(private var storage: Storage) {

    private val log = logger()

    fun getGames(gamesRepoQuery: GamesRepoQuery): GamesRepoResult {
        return try {
            val path = "v1/${gamesRepoQuery.category}/${gamesRepoQuery.languageLocale}/"
            val blobId = BlobId.of("rocket-admin-dev", "${path}game_mock_items.json")
            val bytes = storage.readAllBytes(blobId)
            GamesRepoResult.Success(String(bytes, UTF_8))
        } catch (e: StorageException) {
            val message = "error loading games"
            log.error("[Games]====$message:${e.localizedMessage}")
            GamesRepoResult.Fail(message)
        }
    }
}

sealed class GamesRepoResult {
    class Success(val result: String) : GamesRepoResult()
    class Fail(val message: String) : GamesRepoResult()
}

data class GamesRepoQuery(
        val category: String,
        val languageLocale: String
)