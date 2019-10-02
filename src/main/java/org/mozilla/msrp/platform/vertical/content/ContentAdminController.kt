package org.mozilla.msrp.platform.vertical.content

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class ContentAdminController() {

    @GetMapping("/admin/content")
    fun greeting(): String {
        return "upload"
    }
}
