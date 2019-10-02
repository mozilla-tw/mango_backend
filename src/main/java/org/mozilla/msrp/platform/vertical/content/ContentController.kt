package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.data.parseContent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@RestController
class ContentController @Inject constructor(private val contentRepository: ContentRepository) {

    private val log = logger()
    @Inject
    lateinit var mapper: ObjectMapper

    private val categoryMapping = hashMapOf(
            "apkGame" to "game_apk",
            "html5Game" to "game_html5",
            "shoppingDeal" to "shopping_deal",
            "shoppingCoupon" to "shopping_coupon")
    private val supportLocale = listOf("id-ID", "en-IN", "all")

    // TODO: Make this another endpoint
    // @RequestMapping("/api/v1/content/publish")
    fun publishContent(@RequestParam(value = "category") category: String,
                       @RequestParam(value = "locale") locale: String,
                       @RequestParam(value = "publishDocId") publishDocId: String,
                       @RequestParam(value = "editorUid", required = false) editorUid: String = "admin"
    ): ResponseEntity<String> {
        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for content: $category/$locale"
            log.warn("[Shopping][uploadCoupons]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }
        contentRepository.publish(safeCategory, locale, publishDocId, editorUid)
        return ResponseEntity.ok("<a href='../content?category=$category&locale=$locale'>preview</a>")
    }

    @RequestMapping("/api/v1/content")
    fun getContent(
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String
    ): ResponseEntity<Any> {
        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $category/$locale"
            log.warn("[Shopping][uploadCoupons]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }
        val result = contentRepository.getContentFromDB(ContentRepoQuery(safeCategory, locale))
        if (result is ContentRepoResult.Fail) {
            log.warn("[Content]====getContent===${result.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("listPublish fail")
        }
        return ResponseEntity.ok(result)
    }

    @RequestMapping(value = ["/api/v1/admin/content"], method = [RequestMethod.POST])
    internal fun uploadContent(
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String,
            @RequestParam(value = "tag") tag: String,
            @RequestParam(value = "banner", required = false) banner: MultipartFile?,
            @RequestParam(value = "other") other: MultipartFile
    ): ResponseEntity<String> {

        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $category/$locale"
            log.warn("[Shopping][uploadCoupons]====$message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        try {
            var bannerFile: File? = null
            val otherFile = convertMultiPartToFile(other)
            if (banner != null && !banner.isEmpty) {
                bannerFile = convertMultiPartToFile(banner)
            }
            val parseContent = parseContent(SHOPPING_SCHEMA_VERSION, bannerFile, otherFile)
            val data = mapper.writeValueAsString(parseContent)
            val publishDocId = contentRepository.addContent(AddContentRequest(SHOPPING_SCHEMA_VERSION, tag, safeCategory, locale, data))
            if (publishDocId == null) {
                log.warn("[Content]====uploadContent==fail")
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Publish failed.")
            }
            // TODO: We now publish immediately. We may want to publish later in the future.
            return publishContent(category, locale, publishDocId)

        } catch (iOException: IOException) {
            val message = "[Shopping][Error]==== uploading file: ${other.originalFilename}"
            log.error("$message====$iOException")
            return ResponseEntity.badRequest().body(message)

        } catch (jsonProcessingException: JsonProcessingException) {
            val message = "[Shopping][Error]==== uploading file: ${other.originalFilename}"
            log.error("$message====$jsonProcessingException")
            return ResponseEntity.badRequest().body(message)
        }
    }

    // check category and locale. If they are valid, return the safe category. (keep locale as is)
    private fun safeCategory(category: String, locale: String): String? {
        val safeCategory = categoryMapping[category]
        if (safeCategory == null || !supportLocale.contains(locale)) {
            return null
        }
        return safeCategory
    }

    // find a way to use input stream instead of File to parse CSV. So we don't have to save the file at local
    @Throws(IOException::class)
    private fun convertMultiPartToFile(file: MultipartFile): File {
        val convFile = File(file.originalFilename)
        val fos = FileOutputStream(convFile)
        fos.write(file.bytes)
        fos.close()
        return convFile
    }

    companion object {
        private const val SHOPPING_SCHEMA_VERSION = 1
    }
}
