package org.mozilla.msrp.platform.vertical.admin

import org.mozilla.msrp.platform.user.FirefoxAccountServiceInfo
import org.mozilla.msrp.platform.user.UserRepository
import org.mozilla.msrp.platform.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestParam
import javax.inject.Inject

@Controller
class AdminController @Inject constructor(
        private val userRepository: UserRepository,
        private val firefoxAccountServiceInfo: FirefoxAccountServiceInfo) {

    private val log = logger()
    @GetMapping("api/v1/admin/shopping")
    fun adminShopping(@RequestAttribute admin: Boolean): String {
        if (admin) {
            return "ADMIN"
        } else {
            return "NON"
        }
    }

    // internal only
    @GetMapping("api/v1/admin/login")
    fun adminLogin(@RequestParam email: String): ResponseEntity<String> {
        val uid = userRepository.findFirebaseUidByEmail(email)
        if (uid == null) {
            val message = "No such user: $email"
            log.warn("[admin]====$message")
            return ResponseEntity.badRequest().body(message)
        }
        // TODO. get authentication api from config
        // firefoxAccountServiceInfo?.
        val url = "https://stable.dev.lcip.org/oauth/signin?client_id=2c1c63a0827677b2&state=$uid&scope=profile"
        return ResponseEntity(url, HttpStatus.PERMANENT_REDIRECT)
    }

}