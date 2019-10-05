package org.mozilla.msrp.platform.admin

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam


@Controller
class AdminPublishController {

    @GetMapping("/api/v1/admin/publish")
    fun adminPublish(@RequestParam token: String, model: Model): String {
        val role = JwtHelper.verify(token)
        model.addAttribute("token", token)
        if (role == JwtHelper.ROLE_PUBLISH_ADMIN) {
            return "publish"
        }
        return "401"
    }

    @GetMapping("/api/v1/admin/publish/preview")
    internal fun previewContent(
            @RequestParam token: String,
            @RequestParam(value = "category") category: String,
            @RequestParam(value = "locale") locale: String
    ): String {
        return "401"

    }
}
