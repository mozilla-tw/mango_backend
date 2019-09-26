package org.mozilla.msrp.platform.vertical.content

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.mozilla.msrp.platform.util.logger
import org.springframework.stereotype.Repository
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

@Repository
class ContentRepository @Inject constructor(private var storage: Storage) {

    private val log = logger()

    fun getContent(contentRepoQuery: ContentRepoQuery): ContentRepoResult {
        return try {
            val path = "v1/${contentRepoQuery.category}/${contentRepoQuery.languageLocale}/"
            val blobId = BlobId.of("rocket-admin-dev", "${path}data.json")
            val bytes = storage.readAllBytes(blobId)
            ContentRepoResult.Success(String(bytes, UTF_8))
        } catch (e: StorageException) {
            val message = "error loading games"
            log.error("[Games]====$message====$e")
            ContentRepoResult.Fail(message)
        }
    }
}

sealed class ContentRepoResult {
    class Success(val result: String) : ContentRepoResult()
    class Fail(val message: String) : ContentRepoResult()
}

data class ContentRepoQuery(
        val category: String,
        val languageLocale: String
)