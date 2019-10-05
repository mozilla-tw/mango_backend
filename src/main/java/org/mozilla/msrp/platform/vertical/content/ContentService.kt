package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.data.Category
import org.mozilla.msrp.platform.vertical.content.data.parseContent
import org.mozilla.msrp.platform.vertical.content.db.PublishDoc
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@Named
class ContentService @Inject constructor(private val contentRepository: ContentRepository) {

    private val log = logger()
    @Inject
    lateinit var mapper: ObjectMapper

    private val categoryMapping = hashMapOf(
            "apkGame" to "game_apk",
            "html5Game" to "game_html5",
            "shoppingDeal" to "shopping_deal",
            "shoppingCoupon" to "shopping_coupon")

    private val supportLocale = listOf("id-ID", "en-IN", "all")

    // check category and locale. If they are valid, return the safe category. (keep locale as is)
    fun safeCategory(category: String, locale: String): String? {
        val safeCategory = categoryMapping[category]
        if (safeCategory == null || !supportLocale.contains(locale)) {
            return null
        }
        return safeCategory
    }

    fun getContent(category: String, locale: String): ContentServiceQueryResult {
        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $category/$locale"
            log.warn("[ContentService][getContent]====$message")
            return ContentServiceQueryResult.InvalidParam(message)
        }
        return when (val result = contentRepository.getContentFromDB(ContentRepoQuery(safeCategory, locale))) {
            is ContentRepoResult.Fail -> {
                log.warn("[Content]====getContent===${result.message}")
                ContentServiceQueryResult.Fail(result.message)
            }
            is ContentRepoResult.Success -> {
                log.info("[Content]====getContent===${result.category}")
                ContentServiceQueryResult.Success(result.category)
            }
        }
    }

    fun publish(category: String, locale: String, publishDocId: String, editorUid: String, schedule: String): ContentServicePublishResult {
        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $category/$locale"
            log.warn("[ContentService][publish]====$message")
            return ContentServicePublishResult.InvalidParam(message)
        }
        contentRepository.publish(safeCategory, locale, publishDocId, editorUid, schedule)
        return ContentServicePublishResult.Done
    }


    fun uploadContent(category: String, locale: String, other: MultipartFile, banner: MultipartFile?, tag: String): ContentServiceUploadResult {
        val safeCategory = safeCategory(category, locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $category/$locale"
            log.warn("[ContentService][uploadCoupons]====$message")
            return ContentServiceUploadResult.InvalidParam(message)
        }

        try {
            var bannerBytes: ByteArray? = null
            val listItemBytes = other.bytes
            if (banner != null && !banner.isEmpty) {
                bannerBytes = banner.bytes
            }
            val parseContent = parseContent(bannerBytes, listItemBytes)
            val data = mapper.writeValueAsString(parseContent)
            val publishDocId = contentRepository.addContent(AddContentRequest(tag, safeCategory, locale, data))
            if (publishDocId == null) {
                log.warn("[Content]====uploadContent==fail")
                return ContentServiceUploadResult.Fail("Try publish again!")
            }
            return ContentServiceUploadResult.Success(publishDocId)

        } catch (iOException: IOException) {
            val message = "[Shopping][IOException]==== uploading file: ${other.originalFilename}"
            log.error("$message====$iOException")

            return ContentServiceUploadResult.Fail(message)

        } catch (jsonProcessingException: JsonProcessingException) {
            val message = "[Shopping][jsonProcessingException]==== uploading file: ${other.originalFilename}"
            log.error("$message====$jsonProcessingException")
            return ContentServiceUploadResult.Fail(message)
        }
    }

    fun getPublish(publishDocId: String): PublishDoc? {
        return contentRepository.getContentByPublishDocId(publishDocId)
    }


}

sealed class ContentServiceQueryResult() {
    class Success(val category: Category) : ContentServiceQueryResult()
    class InvalidParam(val message: String) : ContentServiceQueryResult()
    class Fail(val message: String) : ContentServiceQueryResult()
}

sealed class ContentServicePublishResult() {
    object Done : ContentServicePublishResult()
    class InvalidParam(val message: String) : ContentServicePublishResult()
}

sealed class ContentServiceUploadResult() {
    class Success(val publishDocId: String) : ContentServiceUploadResult()
    class InvalidParam(val message: String) : ContentServiceUploadResult()
    class Fail(val message: String) : ContentServiceUploadResult()

}