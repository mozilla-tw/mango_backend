package org.mozilla.msrp.platform.vertical.content

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class ContentAdminController() {

    @GetMapping("/api/v1/admin/content/upload")
    fun greeting(): String {
        return "upload"
    }
}
