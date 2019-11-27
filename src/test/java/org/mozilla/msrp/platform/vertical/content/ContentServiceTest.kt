package org.mozilla.msrp.platform.vertical.content

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mozilla.msrp.platform.common.mock

class ContentServiceTest {

    lateinit var contentService: ContentService

    @Before
    fun warmUp() {
        val mock = mock<ContentRepository>()
        contentService = ContentService(mock)
    }

    @Test
    fun testFirstTryContentLocale() {
        assertEquals("en-US is not in EXACT and will     fallback to english", contentService.getSafeLocale("en-US"), "eng")
        assertEquals("en-IN is     in EXACT and will not fallback here", contentService.getSafeLocale("en-IN"), "en-IN")
        assertEquals("id-ID is     in EXACT and will not fallback here", contentService.getSafeLocale("id-ID"), "id-ID")
        assertEquals("all   is     in EXACT and will not fallback here", contentService.getSafeLocale("all"), "all")
        assertEquals("id-US is not in EXACT and will fallback to indonesia", contentService.getSafeLocale("id-US"), "in")
        assertEquals("es-419is not in EXACT and will fallback to english", contentService.getSafeLocale("es-419"), "eng")
        assertEquals("zh-TW is not in EXACT and will fallback to chinese", contentService.getSafeLocale("zh-TW"), "zh")
        assertEquals("zh-SG is not in EXACT and will fallback to chinese", contentService.getSafeLocale("zh-SG"), "zh")
        assertEquals("abcdef is not a valid locale so we return default", contentService.getSafeLocale("abcdef") , "eng")
    }

    @Test
    fun testSecondTryContentLocale() {
        assertEquals("en-IN will be 'eng' in second try", contentService.fallbackLocale("en-IN"), "eng")
        assertEquals("id-ID will be 'in' in second try", contentService.fallbackLocale("id-ID"), "in")
        assertEquals("all   will be 'eng' in second try", contentService.fallbackLocale("all"), "eng")
    }

}