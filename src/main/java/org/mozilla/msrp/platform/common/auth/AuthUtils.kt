package org.mozilla.msrp.platform.common.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import org.mozilla.msrp.platform.util.logger
import java.util.Date
import java.util.UUID

object JwtHelper {

    const val ROLE_PUBLISH_ADMIN = "publish_admin"
    const val ROLE_MSRP_ADMIN = "msrp_admin"

    private const val ISSUER = "org.mozilla.msrp"
    private const val ROLE = "role"
    private const val EMAIL = "email"
    private const val EXPIRATION = 30 * 60 * 1000  // expired in 50 minutes

    private val SECRET = UUID.randomUUID().toString()   // the secret is different every time the server starts
    private val algorithm = Algorithm.HMAC256(SECRET)
    private val verifier = JWT.require(algorithm).withIssuer(ISSUER).build()
    private val log = logger()

    @JvmStatic
    fun createToken(role: String, email: String): String? {
        try {
            val algorithm = Algorithm.HMAC256(SECRET)
            return JWT.create()
                    .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION))
                    .withIssuer(ISSUER)
                    .withClaim(ROLE, role)
                    .withClaim(EMAIL, email)
                    .sign(algorithm)
        } catch (exception: JWTCreationException) {
            log.error("[JwtHelper][createToken]====$exception")
            return null

        }
    }

    @JvmStatic
    fun verify(token: String): Auth? {
        return try {
            return verifier.verify(token).let {
                Auth(it.getClaim(ROLE).asString(), it.getClaim(EMAIL).asString())
            }
        } catch (exception: JWTVerificationException) {
            log.error("[JwtHelper][verify]====$exception")
            null
        }
    }
}

class Auth(
        val role: String,
        val email: String
)