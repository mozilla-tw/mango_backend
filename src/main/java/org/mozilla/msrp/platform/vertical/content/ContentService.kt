package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.annotations.VisibleForTesting
import org.apache.commons.beanutils.ConversionException
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.data.Category
import org.mozilla.msrp.platform.vertical.content.data.PublishDoc
import org.mozilla.msrp.platform.vertical.content.data.parseContent
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.IllformedLocaleException
import java.util.Locale
import java.util.MissingResourceException
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
            "shoppingCoupon" to "shopping_coupon",
            "travelExplore" to "travel_explore")

    private val supportLocaleList = listOf("id-ID", "en-IN", "all", "zh", "en")
    private val fallbackLanguageList = listOf("eng", "in", "zh")

    // check category and locale. If they are valid, return the safe category. (keep locale as is)
    private fun getSafeCategory(category: String): String? {
        return categoryMapping[category]
    }

    /**
     * @param locale The locale passed from the client.
     *
     * @return the possible "locale" in the DB
     * */
    @VisibleForTesting
    fun getSafeLocale(locale: String): String {
        // try exact match first
        if (supportLocaleList.contains(locale)) {
            return locale
        }
        // if we can't find exact match, find in fallback
        return fallbackLocale(locale)
    }

    // the return value is not really a locale. It's actually a language.
    // we use the world "locale" here cause the key in the document is "locale"
    @VisibleForTesting
    fun fallbackLocale(locale: String): String {
        try {
            for (supportLanguage in fallbackLanguageList) {
                val inputLanguage = Locale.Builder().setLanguageTag(locale).build().language
                if (inputLanguage == supportLanguage) {
                    log.info("$locale fallback to $supportLanguage cause they're all $inputLanguage")
                    return supportLanguage
                }
            }
        } catch (e: IllformedLocaleException) {
            log.error("Error parsing fallbackLocale [$locale] IllformedLocaleException:$e")
        } catch (e: MissingResourceException) {
            log.error("Error parsing fallbackLocale [$locale] MissingResourceException:$e")
        }
        return DEFAULT_LOCALE
    }

    fun getContent(param: ContentServiceQueryParam): ContentServiceQueryResult {
        val safeCategory = getSafeCategory(param.category)
        val safeLocale = getSafeLocale(param.locale)
        if (safeCategory == null) {
            val message = "Not supported parameters for shopping: $param"
            log.warn("[ContentService][getContent]====$message")
            return ContentServiceQueryResult.InvalidParam(message)
        }
        // check if we support the specific locale
        var result = contentRepository.getContentFromDB(ContentRepoQuery(safeCategory, safeLocale, param.tag))
        // then try to fallback that locale
        if (result is ContentRepoResult.Empty){
            val fallbackLocale = fallbackLocale(param.locale)
            log.warn("[ContentService][getContent retry]====$param====with fallbackLocale:$fallbackLocale")
            result = contentRepository.getContentFromDB(ContentRepoQuery(safeCategory, fallbackLocale, param.tag))
        }
        // If all above won't work, fallback to eng
        if (result is ContentRepoResult.Empty){
            val fallbackLocale = DEFAULT_LOCALE
            log.warn("[ContentService][getContent retry]====$param====with fallbackLocale:$fallbackLocale")
            result = contentRepository.getContentFromDB(ContentRepoQuery(safeCategory, fallbackLocale, param.tag))
        }

        return when (result) {
            is ContentRepoResult.Fail -> {
                log.warn("[Content]====getContent===${result.message}")
                ContentServiceQueryResult.Fail(result.message)
            }
            is ContentRepoResult.Success -> {
                log.info("[Content]====getContent===${result.version}")
                ContentServiceQueryResult.Success(result.version, result.tag, result.data)
            }
        }
    }

    fun publish(publishDocId: String, editor: String, schedule: String?): ContentServicePublishResult {
        val publish = contentRepository.publish(publishDocId, editor, schedule)
        return when (publish) {
            is ContentRepositoryPublishResult.Success -> {
                return categoryMapping.filter { it.value == publish.category }.map {
                    return@map ContentServicePublishResult.Success(it.key, publish.locale)
                }.firstOrNull() ?: ContentServicePublishResult.Fail("Data error")
            }
            is ContentRepositoryPublishResult.Fail -> ContentServicePublishResult.Fail(publish.message)
        }
    }


    fun uploadContent(category: String, locale: String, other: MultipartFile, banner: MultipartFile?, tag: String): ContentServiceUploadResult {
        val safeCategory = getSafeCategory(category)
        val safeLocale = getSafeLocale(locale)
        if (safeCategory == null) {
            val message = "No such category: $category/$locale"
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
            val publishDocId = contentRepository.addContent(AddContentRequest(tag, safeCategory, safeLocale, data))
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

        } catch (e: NumberFormatException) {
            val message = "[Shopping][NumberFormatException]==== uploading file: ${other.originalFilename}"
            log.error("$message====$e")
            return ContentServiceUploadResult.Fail("$message<BR>Error====<BR>$e")

        } catch (e: ConversionException) {
            val message = "[Shopping][ConversionException]==== uploading file: ${other.originalFilename}"
            log.error("$message====$e")
            return ContentServiceUploadResult.Fail("$message<BR>Error====<BR>$e")
        }

    }

    fun getPublish(publishDocId: String): PublishDoc? {
        return contentRepository.getContentByPublishDocId(publishDocId)
    }

    companion object {
        const val DEFAULT_LOCALE = "eng"
    }

}

sealed class ContentServiceQueryResult {
    class Success(val version: Long, val tag: String, val data: Category) : ContentServiceQueryResult()
    class InvalidParam(val message: String) : ContentServiceQueryResult()
    class Fail(val message: String) : ContentServiceQueryResult()
}

sealed class ContentServicePublishResult {
    class Success(val category: String, val locale: String) : ContentServicePublishResult()
    class Fail(val message: String) : ContentServicePublishResult()
}

sealed class ContentServiceUploadResult {
    class Success(val publishDocId: String) : ContentServiceUploadResult()
    class InvalidParam(val message: String) : ContentServiceUploadResult()
    class Fail(val message: String) : ContentServiceUploadResult()

}