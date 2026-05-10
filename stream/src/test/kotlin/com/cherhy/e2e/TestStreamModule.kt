package com.cherhy.e2e

import com.cherhy.plugins.configureDependencyInjection
import com.cherhy.plugins.configureRouting
import io.ktor.server.application.*

fun Application.streamE2eModule() {
    configureRouting()
    configureDependencyInjection()
}
