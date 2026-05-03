package com.cherhy.gateway.config

import com.cherhy.common.util.Payment.PAYMENT_DOMAIN
import com.cherhy.common.util.Stream.STREAM_DOMAIN
import com.cherhy.common.util.User.DELETE_USER
import com.cherhy.common.util.User.UPDATE_USER
import com.cherhy.common.util.User.USER_DOMAIN
import com.cherhy.gateway.security.JwtReactiveAuthenticationManager
import com.cherhy.gateway.security.JwtTokenAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtTokenAuthenticationConverter: JwtTokenAuthenticationConverter,
    private val jwtReactiveAuthenticationManager: JwtReactiveAuthenticationManager,
) {
    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authFilter = AuthenticationWebFilter(jwtReactiveAuthenticationManager).apply {
            setServerAuthenticationConverter(jwtTokenAuthenticationConverter)
        }

        return http
            .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { spec ->
                spec.pathMatchers(HttpMethod.PUT, UPDATE_USER).authenticated()
                spec.pathMatchers(HttpMethod.DELETE, DELETE_USER).authenticated()
                spec.pathMatchers(USER_DOMAIN).permitAll()
                spec.pathMatchers(PAYMENT_DOMAIN).authenticated()
                spec.pathMatchers(STREAM_DOMAIN).authenticated()
                spec.anyExchange().permitAll()
            }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .cors { cors ->
                cors.configurationSource {
                    CorsConfiguration().apply {
                        allowCredentials = true
                        allowedHeaders = listOf("*")
                        allowedMethods = listOf("*")
                        allowedOriginPatterns = listOf("*")
                    }
                }
            }
            .build()
    }
}
