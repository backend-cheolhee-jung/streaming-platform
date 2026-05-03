package cherhy.example

import cherhy.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureJwt()
    configureDependencyInjection()
    configureDatabase()
    configureJackson()
    configureRouting()
}