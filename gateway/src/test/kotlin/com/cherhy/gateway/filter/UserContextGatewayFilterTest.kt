package com.cherhy.gateway.filter

import com.cherhy.common.util.USER_ID
import com.cherhy.gateway.security.GatewayUserPrincipal
import com.cherhy.gateway.security.JwtAuthenticationToken
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserContextGatewayFilterTest : StringSpec({

    val filter = UserContextGatewayFilter()

    "adds user-id header when security context has JwtAuthenticationToken" {
        val principal = GatewayUserPrincipal(42L, "alice", listOf("UNPAID_MEMBER"))
        val auth = JwtAuthenticationToken(principal, "dummy-token")
        val securityContext = SecurityContextImpl(auth)

        val request = MockServerHttpRequest.get("/test").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedUserId: String? = null
        val chain = GatewayFilterChain { ex ->
            capturedUserId = ex.request.headers.getFirst(USER_ID)
            Mono.empty()
        }

        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(
                    ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))
                )
        )
            .verifyComplete()

        capturedUserId shouldBe "42"
    }

    "passes exchange unchanged when security context is empty" {
        val request = MockServerHttpRequest.get("/test").build()
        val exchange = MockServerWebExchange.from(request)

        var capturedUserId: String? = "sentinel"
        val chain = GatewayFilterChain { ex ->
            capturedUserId = ex.request.headers.getFirst(USER_ID)
            Mono.empty()
        }

        StepVerifier.create(
            filter.filter(exchange, chain)
        )
            .verifyComplete()

        capturedUserId shouldBe null
    }
})
