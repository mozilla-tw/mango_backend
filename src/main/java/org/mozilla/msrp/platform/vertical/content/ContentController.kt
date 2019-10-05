package org.mozilla.msrp.platform.vertical.content

import com.fasterxml.jackson.databind.ObjectMapper
import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.inject.Inject
import javax.servlet.http.HttpServletResponse


@RestController
class ContentController @Inject constructor(private val contentService: ContentService) {

    private val log = logger()
    @Inject
    lateinit var mapper: ObjectMapper

    @RequestMapping("/api/v1/content")
    fun getContent(
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String
    ): ResponseEntity<Any> {
        return when (val result = contentService.getContent(category, locale)) {
            is ContentServiceQueryResult.InvalidParam -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.message)
            is ContentServiceQueryResult.Success -> ResponseEntity.status(HttpStatus.OK).body(result.category)
            is ContentServiceQueryResult.Fail -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.message)
        }
    }

    // ======================== ADMIN ======================== START
    @RequestMapping("/api/v1/content/publish")
    fun publishContent(
            @RequestParam token: String,
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String,
            @RequestParam(value = "publishDocId") publishDocId: String,
            @RequestParam(value = "editorUid", required = false) editorUid: String = "admin"
    ): ResponseEntity<String> {
        if (JwtHelper.verify(token)?.role != JwtHelper.ROLE_PUBLISH_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Permission")
        }
        return when (val result = contentService.publish(category, locale, publishDocId, editorUid)) {
            ContentServicePublishResult.Done -> ResponseEntity.status(HttpStatus.OK).body("<a href='../content?category=$category&locale=$locale'>preview</a>")
            is ContentServicePublishResult.InvalidParam -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.message)
        }
    }


    @RequestMapping(value = ["/api/v1/admin/content"], method = [RequestMethod.POST])
    internal fun uploadContent(
            @RequestParam token: String,
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String,
            @RequestParam(value = "tag") tag: String,
            @RequestParam(value = "banner", required = false) banner: MultipartFile?,
            @RequestParam(value = "other") other: MultipartFile,
            response: HttpServletResponse  // need HttpServletResponse to redirect
    ): ResponseEntity<String> {
        val verify = JwtHelper.verify(token)
        if (verify?.role != JwtHelper.ROLE_PUBLISH_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Permission")
        }
        return when (val result = contentService.uploadContent(category, locale, other, banner, tag)) {
            is ContentServiceUploadResult.Success -> {
                val preview = "/api/v1/admin/publish/preview?token=$token&publishDocId=${result.publishDocId}"
                val publish = "/api/v1/content/publish?token=$token&category=$category&locale=$locale&publishDocId=${result.publishDocId}?editorUid=${verify.email}"
                ResponseEntity.status(HttpStatus.OK).body("" +
                        "<a href='$preview'>Preview</a> <BR> " +
                        "<a href='$publish'>Publish Now</a>")
            }
            is ContentServiceUploadResult.InvalidParam -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.message)
            is ContentServiceUploadResult.Fail -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.message)
        }
    }


    // ======================== ADMIN ======================== END

    companion object {
        private const val SHOPPING_SCHEMA_VERSION = 1
    }
}
