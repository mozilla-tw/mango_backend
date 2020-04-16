package org.mozilla.msrp.platform.push

import org.mozilla.msrp.platform.common.auth.JwtHelper
import org.mozilla.msrp.platform.common.isDev
import org.mozilla.msrp.platform.common.isStableDev
import org.mozilla.msrp.platform.user.UserRepository
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.stream.IntStream
import javax.inject.Inject

/**
 * A API only for testing. It'll mimic stmo and return all the clients ids in the database
 *
 * I really don't like this Test API. But there's no way for us to do the testing before BI team got the data
 * */
@RestController
class DummyStmoController @Inject constructor(
        private val userRepository: UserRepository,
        private val jwtHelper: JwtHelper) {

    @Inject
    lateinit var environment: Environment


    @GetMapping("/test/uids")
    fun uids(
            @RequestParam(required = false) token: String,
            @RequestParam(required = false) times: Int?,
            @RequestParam(required = false) all: Boolean?,
            @RequestParam(required = false) user: Array<String>?
    ): String {

        // will verify the token if it's not stable and not dev
        if (!environment.isStableDev && !environment.isDev) {
            val verify = jwtHelper.verify(token)
            if (verify?.role != JwtHelper.ROLE_PUSH_ADMIN) {
                return "401"
            }
        }

        val ids = StringBuffer()

        times?.let {
            IntStream.range(0, it).parallel().forEach {
                ids.append("{ \"client_id\": \"${UUID.randomUUID()}\" },")
            }
        }

        user?.forEach {
            ids.append("{ \"client_id\": \"$it\" },")
        }

        if (all == true) {
            userRepository.getTelemetryClientId().forEach {
                ids.append("{ \"client_id\": \"$it\" },")
            }
        }

        var idString = ids.toString()
        if (idString.length != 0) {
            idString = idString.substring(0, idString.length - 1);
        }
        return "{\n" +
                "  \"query_result\": {\n" +
                "    \"id\": 123456, \"query_hash\": \"1234aaaaaaaaaaaaaaaaaaaaaaaa\",\n" +
                "    \"query\": \"WITH SOME SQL TO SELECT THE DATA\",\n" +
                "    \"data\": {\n" +
                "      \"rows\": [\n" +
                "        $idString\n" +
                "      ], \"columns\": [ { \"type\": \"string\", \"friendly_name\": \"client_id\", \"name\": \"client_id\" } ],\n" +
                "      \"metadata\": { \"data_scanned\": 1234567 }\n" +
                "    }, \"data_source_id\": 2, \"runtime\": 5.0813, \"retrieved_at\": \"2010-10-10T10:10:10.000Z\"\n" +
                "  }\n" +
                "}"
    }
}
