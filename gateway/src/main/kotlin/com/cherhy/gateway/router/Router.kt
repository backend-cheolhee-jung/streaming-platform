package com.cherhy.gateway.router

import com.cherhy.common.util.Payment.PAYMENT_DOMAIN
import com.cherhy.common.util.Payment.PAYMENT_SERVICE
import com.cherhy.common.util.Stream.STREAM_DOMAIN
import com.cherhy.common.util.Stream.STREAM_SERVICE
import com.cherhy.common.util.User.USER_DOMAIN
import com.cherhy.common.util.User.USER_SERVICE
import com.cherhy.gateway.util.property.DomainProperty
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Router(
    private val domainProperty: DomainProperty,
) {
    @Bean
    fun routeLocator(builder: RouteLocatorBuilder) = builder.routes {
        route(PAYMENT_SERVICE) {
            path(PAYMENT_DOMAIN)
            uri(domainProperty.paymentUrl)
        }
        route(STREAM_SERVICE) {
            path(STREAM_DOMAIN)
            uri(domainProperty.streamUrl)
        }
        route(USER_SERVICE) {
            path(USER_DOMAIN)
            uri(domainProperty.userUrl)
        }
    }
}
