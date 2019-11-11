package org.mozilla.msrp.platform.admin

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.util.logger
import org.mozilla.msrp.platform.vertical.content.ContentService
import org.mozilla.msrp.platform.vertical.content.ContentServicePublishResult
import org.mozilla.msrp.platform.vertical.content.ContentServiceUploadResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import javax.inject.Inject


@Controller
class AdminPublishController @Inject constructor(val contentService: ContentService) {

    @Inject
    private lateinit var jwtHelper: JwtHelper
    private val log = logger()

    @GetMapping("/api/v1/admin/publish")
    fun adminPublish(
            @RequestParam token: String,
            model: Model): String {
        val role = jwtHelper.verify(token)?.role
        if (role == JwtHelper.ROLE_PUBLISH_ADMIN) {
            model.addAttribute("token", token)
            log.info("Success: adminPublish: $role")
            return "publish"
        }
        log.error("No permission: previewContent: $token")
        return "401"
    }

    @GetMapping("/api/v1/admin/publish/preview")
    internal fun previewContent(
            @RequestParam token: String,
            @RequestParam publishDocId: String,
            model: Model
    ): String {
        val role = jwtHelper.verify(token)?.role
        if (role == JwtHelper.ROLE_PUBLISH_ADMIN) {
            model.addAttribute("token", token)
            val publishDoc = contentService.getPublish(publishDocId)
            if (publishDoc != null) {
                model.addAttribute("publishDoc", publishDoc)
                log.info("Success: previewContent: $publishDocId")
                return "preview"
            } else {
                return "401"
            }
        }
        log.error("No permission: previewContent: $token")
        return "401"

    }

    @GetMapping("/api/v1/content/publish")
    fun publishContent(
            @RequestParam token: String,
            @RequestParam publishDocId: String,
            @RequestParam editor: String,
            @RequestParam(required = false) schedule: String? = ""
    ): ResponseEntity<String> {
        if (jwtHelper.verify(token)?.role != JwtHelper.ROLE_PUBLISH_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Permission")
        }
        return when (val result = contentService.publish(publishDocId, editor, schedule)) {
            is ContentServicePublishResult.Success -> ResponseEntity.status(HttpStatus.OK).body("<a href='../content?category=${result.category}&locale=${result.locale}'>View Client JSON</a>")
            is ContentServicePublishResult.Fail -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.message)
        }
    }

    @RequestMapping(value = ["/api/v1/admin/content"], method = [RequestMethod.POST])
    internal fun uploadContent(
            @RequestParam token: String,
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String,
            @RequestParam(value = "tag") tag: String,
            @RequestParam(value = "banner", required = false) banner: MultipartFile?,
            @RequestParam(value = "other") other: MultipartFile
    ): ResponseEntity<String> {
        val verify = jwtHelper.verify(token)
        if (verify?.role != JwtHelper.ROLE_PUBLISH_ADMIN) {
            log.warn("No permission: uploadContent: $category/$locale/$tag/$banner/$other")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Permission")
        }
        log.info("Success: uploadContent: $category/$locale/$tag/$banner/$other")

        return when (val result = contentService.uploadContent(category, locale, other, banner, tag)) {
            is ContentServiceUploadResult.Success -> {
                val preview = "/api/v1/admin/publish/preview?token=$token&publishDocId=${result.publishDocId}"
                val publish = "/api/v1/content/publish?token=$token&category=$category&locale=$locale&publishDocId=${result.publishDocId}&editor=${verify.email}"
                ResponseEntity.status(HttpStatus.OK).body("" +
                        "<a href='$preview'>Preview</a> <BR> " +
                        "<a href='$publish'>Publish Now</a><BR> " +
                        "<form action=$publish>" +
                        "<input type='hidden' name='token' value='$token'>" +
                        "<input type='hidden' name='category' value='$category'>" +
                        "<input type='hidden' name='locale' value='$locale'>" +
                        "<input type='hidden' name='publishDocId' value='${result.publishDocId}'>" +
                        "<input type='hidden' name='editor' value='${verify.email}'>" +

                        "<input type='date' name='schedule'>" +
                        "<input type='submit' value='Schedule Publish'>" +
                        "</form>")
            }
            is ContentServiceUploadResult.InvalidParam -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.message)
            is ContentServiceUploadResult.Fail -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.message)
        }
    }

}
