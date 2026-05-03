package com.cherhy.gateway.filter

import com.cherhy.common.util.USER_ID
import com.cherhy.gateway.security.GatewayUserPrincipal
import com.cherhy.gateway.security.JwtAuthenticationToken
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class UserContextGatewayFilter : GlobalFilter, Ordered {
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE - 1

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> =
        ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .filter { it is JwtAuthenticationToken && it.isAuthenticated }
            .cast(JwtAuthenticationToken::class.java)
            .flatMap { auth ->
                val principal = auth.principal as GatewayUserPrincipal
                val mutatedRequest = exchange.request.mutate()
                    .header(USER_ID, principal.userId.toString())
                    .build()
                chain.filter(exchange.mutate().request(mutatedRequest).build())
            }
            .switchIfEmpty(chain.filter(exchange))
}
