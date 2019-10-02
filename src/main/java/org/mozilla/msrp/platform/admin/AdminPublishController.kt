package org.mozilla.msrp.platform.admin

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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
}
