package com.cherhy.gateway.security

import com.cherhy.gateway.jwt.TokenDecoder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtReactiveAuthenticationManager(
    private val tokenDecoder: TokenDecoder,
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val token = authentication.credentials as String
        return try {
            val principal = tokenDecoder.decode(token)
            Mono.just(JwtAuthenticationToken(principal, token))
        } catch (e: Exception) {
            Mono.error(BadCredentialsException("Invalid JWT token", e))
        }
    }
}
