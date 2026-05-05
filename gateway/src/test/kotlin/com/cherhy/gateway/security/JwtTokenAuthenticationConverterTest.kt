package com.cherhy.gateway.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.test.StepVerifier

class JwtTokenAuthenticationConverterTest : FunSpec({
    val converter = JwtTokenAuthenticationConverter()

    test("returns authentication containing Bearer token") {
        val request = MockServerHttpRequest.get("/test")
            .header("Authorization", "Bearer my.jwt.token")
            .build()
        val exchange = MockServerWebExchange.from(request)

        StepVerifier.create(converter.convert(exchange))
            .assertNext { auth ->
                auth shouldNotBe null
                auth.credentials shouldBe "my.jwt.token"
            }
            .verifyComplete()
    }

    test("returns empty when Authorization header is absent") {
        val request = MockServerHttpRequest.get("/test").build()
        val exchange = MockServerWebExchange.from(request)

        StepVerifier.create(converter.convert(exchange))
            .verifyComplete()
    }

    test("returns empty when Authorization header is not Bearer") {
        val request = MockServerHttpRequest.get("/test")
            .header("Authorization", "Basic dXNlcjpwYXNz")
            .build()
        val exchange = MockServerWebExchange.from(request)

        StepVerifier.create(converter.convert(exchange))
            .verifyComplete()
    }
})
