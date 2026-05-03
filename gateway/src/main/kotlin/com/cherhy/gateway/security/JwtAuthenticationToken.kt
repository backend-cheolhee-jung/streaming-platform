package com.cherhy.gateway.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtAuthenticationToken(
    private val principal: GatewayUserPrincipal,
    private val credentials: String,
) : AbstractAuthenticationToken(
    principal.roles.map { SimpleGrantedAuthority("ROLE_$it") }
) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): String = credentials
    override fun getPrincipal(): GatewayUserPrincipal = principal
}
