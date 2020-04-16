package org.mozilla.msrp.platform.push

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.common.isDev
import org.mozilla.msrp.platform.common.isStableDev
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@Controller
class AdminPushUiController @Inject constructor(
        private val jwtHelper: JwtHelper) {

    val atomicInteger = AtomicInteger()

    @Inject
    lateinit var environment: Environment

    /**
     * Show Push ADMIN UI to verified users
     * */
    @GetMapping("/api/v1/admin/push/ui")
    fun adminPush(
            timezone: TimeZone,
            @RequestParam token: String, model: Model): String {

        if (!environment.isStableDev && !environment.isDev) {
            val verify = jwtHelper.verify(token)
            if (verify?.role != JwtHelper.ROLE_PUSH_ADMIN) {
                return "401"
            }
        }
        val now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Taipei"))
        model.addAttribute("one_min_later", now.toEpochSecond() * 1000 + 60000)
        model.addAttribute("now", now.toEpochSecond() * 1000)
        model.addAttribute("date", now.toString())
        model.addAttribute("mozMsgBatch", UUID.randomUUID().toString())
        model.addAttribute("token", token)

        return "push"
    }

}