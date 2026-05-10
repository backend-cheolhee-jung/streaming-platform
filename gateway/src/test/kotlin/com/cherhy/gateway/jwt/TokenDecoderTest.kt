package com.cherhy.gateway.jwt

import com.cherhy.gateway.security.GatewayUserPrincipal
import com.cherhy.gateway.util.property.JwtProperty
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*
import javax.crypto.spec.SecretKeySpec

private const val TEST_SECRET = "test-secret-that-is-long-enough-for-hmac"

class TokenDecoderTest : StringSpec({
    val jwtProperty = JwtProperty(
        secret = TEST_SECRET,
        expiration = 3600,
        refreshExpiration = 86400,
        algorithm = "HmacSHA256",
    )
    val tokenDecoder = TokenDecoder(jwtProperty).also { it.afterPropertiesSet() }

    "decodes user-id claim as Long" {
        val token = buildToken(userId = 42L)
        val principal: GatewayUserPrincipal = tokenDecoder.decode(token)
        principal.userId shouldBe 42L
    }

    "decodes username claim" {
        val token = buildToken(username = "alice")
        val principal: GatewayUserPrincipal = tokenDecoder.decode(token)
        principal.username shouldBe "alice"
    }

    "decodes single role" {
        val token = buildToken(role = "UNPAID_MEMBER")
        val principal: GatewayUserPrincipal = tokenDecoder.decode(token)
        principal.roles shouldBe listOf("UNPAID_MEMBER")
    }

    "decodes multiple roles joined by comma" {
        val token = buildToken(role = "ADMIN,PAID_MEMBER")
        val principal: GatewayUserPrincipal = tokenDecoder.decode(token)
        principal.roles shouldBe listOf("ADMIN", "PAID_MEMBER")
    }

    "throws for expired token" {
        val token = buildToken(expiresAt = Date(System.currentTimeMillis() - 1000))
        shouldThrow<RuntimeException> { tokenDecoder.decode(token) }
    }

    "throws for token signed with wrong secret" {
        val token = buildToken(secret = "wrong-secret-that-is-also-long-enough!!")
        shouldThrow<RuntimeException> { tokenDecoder.decode(token) }
    }
})

private fun buildToken(
    userId: Long = 1L,
    username: String = "testuser",
    role: String = "UNPAID_MEMBER",
    expiresAt: Date = Date(System.currentTimeMillis() + 3_600_000),
    secret: String = TEST_SECRET,
): String {
    val key = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
    val header = JWSHeader(JWSAlgorithm.HS256)
    val claims = JWTClaimsSet.Builder()
        .claim("user-id", userId)
        .claim("username", username)
        .claim("role", role)
        .expirationTime(expiresAt)
        .build()
    return SignedJWT(header, claims).also { it.sign(MACSigner(key)) }.serialize()
}
