package org.mozilla.msrp.platform.vertical.admin

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute

@Controller
class AdminController {

    @GetMapping("api/v1/admin/shopping")
    fun adminShopping(@RequestAttribute admin:Boolean): String{
        if (admin) {
            return "ADMIN"
        } else{
            return "NON"
        }
    }

}