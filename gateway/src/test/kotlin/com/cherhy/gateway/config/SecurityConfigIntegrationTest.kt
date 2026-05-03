package com.cherhy.gateway.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Date
import javax.crypto.spec.SecretKeySpec

// Must match jwt.secret in src/test/resources/application.yml
private const val TEST_JWT_SECRET = "test-secret-that-is-long-enough-for-hmac-256"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityConfigIntegrationTest : StringSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var webTestClient: WebTestClient

    init {
        "GET /payments/1 without Authorization header returns 401 Unauthorized" {
            webTestClient.get()
                .uri("/payments/1")
                .exchange()
                .expectStatus().isUnauthorized
        }

        "POST /users/login without token is not 401 (permit all for /users/**)" {
            webTestClient.post()
                .uri("/users/login")
                .exchange()
                .expectStatus().value { status ->
                    assert(status != HttpStatus.UNAUTHORIZED.value()) {
                        "Expected non-401 status but got $status"
                    }
                }
        }

        "GET /payments/1 with valid Bearer JWT is not 401" {
            val token = buildValidToken()

            webTestClient.get()
                .uri("/payments/1")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().value { status ->
                    assert(status != HttpStatus.UNAUTHORIZED.value()) {
                        "Expected non-401 status but got $status"
                    }
                }
        }
    }
}

private fun buildValidToken(): String {
    val key = SecretKeySpec(TEST_JWT_SECRET.toByteArray(), "HmacSHA256")
    return SignedJWT(
        JWSHeader(JWSAlgorithm.HS256),
        JWTClaimsSet.Builder()
            .claim("user-id", 1L)
            .claim("username", "test")
            .claim("role", "UNPAID_MEMBER")
            .expirationTime(Date(System.currentTimeMillis() + 3_600_000))
            .build()
    ).also { it.sign(MACSigner(key)) }.serialize()
}
