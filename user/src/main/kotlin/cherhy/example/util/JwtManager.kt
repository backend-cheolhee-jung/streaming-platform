package cherhy.example.util

import cherhy.example.domain.Role
import cherhy.example.domain.Username
import cherhy.example.util.property.JwtProperty.AUDIENCE
import cherhy.example.util.property.JwtProperty.EXPIRATION
import cherhy.example.util.property.JwtProperty.ISSUER
import cherhy.example.util.property.JwtProperty.SECRET
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cherhy.common.util.ROLE
import com.cherhy.common.util.USERNAME
import com.cherhy.common.util.USER_ID
import com.cherhy.common.util.model.UserId
import mu.KotlinLogging
import java.util.*

class JwtManager {
    private val logger = KotlinLogging.logger {}

    fun createToken(
        userId: UserId,
        userName: Username,
        roles: List<Role>,
        tokenType: TokenType,
    ) =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(USER_ID, userId.value)
            .withClaim(USERNAME, userName.value)
            .withClaim(ROLE, roles.joinToString(",") { it.name })
            .apply {
                when (tokenType) {
                    TokenType.ACCESS -> withExpiresAt(
                        Date(System.currentTimeMillis() + expiration)
                    )
                    TokenType.REFRESH -> withExpiresAt(
                        Date(System.currentTimeMillis() + expiration * 30)
                    )
                }
            }
            .sign(Algorithm.HMAC256(secret))
            ?: error("JWT signing returned null")

    companion object {
        @JvmStatic
        private val secret = ApplicationConfigUtils.getJwt(SECRET)

        @JvmStatic
        private val issuer = ApplicationConfigUtils.getJwt(ISSUER)

        @JvmStatic
        private val audience = ApplicationConfigUtils.getJwt(AUDIENCE)

        @JvmStatic
        private val expiration = ApplicationConfigUtils.getJwt(EXPIRATION).toLong()
    }
}