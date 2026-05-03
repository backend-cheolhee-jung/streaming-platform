package com.cherhy.gateway.security

data class GatewayUserPrincipal(
    val userId: Long,
    val username: String,
    val roles: List<String>,
)
