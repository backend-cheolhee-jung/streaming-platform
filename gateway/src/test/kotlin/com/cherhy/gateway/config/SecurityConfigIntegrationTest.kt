package com.cherhy.gateway.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.kotest.core.spec.style.BehaviorSpec
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
class SecurityConfigIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var webTestClient: WebTestClient

    init {
        Given("Authorization 헤더가 없는 경우") {
            When("GET /payments/1 을 요청하면") {
                Then("401 Unauthorized 를 반환한다") {
                    webTestClient.get()
                        .uri("/payments/1")
                        .exchange()
                        .expectStatus().isUnauthorized
                }
            }
        }

        Given("/users/** 경로는 인증 없이 허용되는 경우") {
            When("POST /users/login 을 요청하면") {
                Then("401 이 아닌 응답을 반환한다") {
                    webTestClient.post()
                        .uri("/users/login")
                        .exchange()
                        .expectStatus().value { status ->
                            assert(status != HttpStatus.UNAUTHORIZED.value()) {
                                "Expected non-401 status but got $status"
                            }
                        }
                }
            }
        }

        Given("유효한 Bearer JWT 토큰이 있는 경우") {
            When("GET /payments/1 을 요청하면") {
                Then("401 이 아닌 응답을 반환한다") {
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
