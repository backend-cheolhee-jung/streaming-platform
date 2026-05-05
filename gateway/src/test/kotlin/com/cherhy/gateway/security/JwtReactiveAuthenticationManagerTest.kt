package com.cherhy.gateway.security

import com.cherhy.gateway.jwt.TokenDecoder
import com.cherhy.gateway.util.property.JwtProperty
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import reactor.test.StepVerifier
import java.util.*
import javax.crypto.spec.SecretKeySpec

private const val TEST_SECRET = "test-secret-that-is-long-enough-for-hmac"

class JwtReactiveAuthenticationManagerTest : FunSpec({
    val jwtProperty = JwtProperty(
        secret = TEST_SECRET,
        expiration = 3600,
        refreshExpiration = 86400,
        algorithm = "HmacSHA256",
    )
    val tokenDecoder = TokenDecoder(jwtProperty).also { it.afterPropertiesSet() }
    val manager = JwtReactiveAuthenticationManager(tokenDecoder)

    test("authenticates valid token and returns JwtAuthenticationToken") {
        val token = buildToken(userId = 7L, username = "bob", role = "ADMIN")
        val input = UsernamePasswordAuthenticationToken(token, token)

        StepVerifier.create(manager.authenticate(input))
            .assertNext { auth ->
                auth.shouldBeInstanceOf<JwtAuthenticationToken>()
                val principal = auth.principal as GatewayUserPrincipal
                principal.userId shouldBe 7L
                principal.username shouldBe "bob"
                principal.roles shouldBe listOf("ADMIN")
                auth.isAuthenticated shouldBe true
            }
            .verifyComplete()
    }

    test("emits error for invalid token") {
        val badToken = "not.a.valid.jwt"
        val input = UsernamePasswordAuthenticationToken(badToken, badToken)

        StepVerifier.create(manager.authenticate(input))
            .expectError()
            .verify()
    }
})

private fun buildToken(
    userId: Long = 1L,
    username: String = "testuser",
    role: String = "UNPAID_MEMBER",
): String {
    val key = SecretKeySpec(TEST_SECRET.toByteArray(), "HmacSHA256")
    val header = JWSHeader(JWSAlgorithm.HS256)
    val claims = JWTClaimsSet.Builder()
        .claim("user-id", userId)
        .claim("username", username)
        .claim("role", role)
        .expirationTime(Date(System.currentTimeMillis() + 3_600_000))
        .build()
    return SignedJWT(header, claims).also { it.sign(MACSigner(key)) }.serialize()
}
