package org.mozilla.msrp.platform.admin

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
class AdminMsrpController {

    @GetMapping("/api/v1/admin/msrp")
    fun adminPublish(@RequestParam token: String, model: Model): String {
        val role = JwtHelper.verify(token)
        if (role == JwtHelper.ROLE_MSRP_ADMIN) {
            model.addAttribute("token", token)
            return "msrp"
        }
        return "401"
    }
}
