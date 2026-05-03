package com.cherhy.gateway.jwt

import com.cherhy.gateway.security.GatewayUserPrincipal
import com.cherhy.gateway.util.property.JwtProperty
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class TokenDecoder(
    private val jwtProperty: JwtProperty,
) : InitializingBean {
    private val log = KotlinLogging.logger {}
    private lateinit var key: SecretKey

    override fun afterPropertiesSet() {
        this.key = SecretKeySpec(jwtProperty.secret.toByteArray(), jwtProperty.algorithm)
    }

    fun decode(token: String): GatewayUserPrincipal {
        val claims = extractClaims(token)
        val userId = claims.getLongClaim("user-id")
        val username = claims.getStringClaim("username")
        val role = claims.getStringClaim("role")
        val roles = role.split(",")
        return GatewayUserPrincipal(userId, username, roles)
    }

    private fun extractClaims(token: String) =
        try {
            val signedJWT = SignedJWT.parse(token)
            val verifier = MACVerifier(key)
            check(signedJWT.verify(verifier)) { "JWT 서명 검증 실패" }
            signedJWT.jwtClaimsSet.also {
                checkNotNull(it.expirationTime) { "만료 시간 없음" }
                check(it.expirationTime.after(java.util.Date())) { "JWT 토큰이 만료 되었습니다." }
            }
        } catch (e: JOSEException) {
            log.info { "JWT 파싱 실패: $e" }
            error("JWT 토큰이 잘못 되었습니다.")
        } catch (e: IllegalStateException) {
            log.info { "JWT 검증 실패: $e" }
            error(e.message ?: "JWT 토큰이 잘못 되었습니다.")
        } catch (e: RuntimeException) {
            log.info { "JWT 처리 오류: $e" }
            error("JWT 토큰이 잘못 되었습니다.")
        }
}
