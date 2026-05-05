package cherhy.example.plugins

import cherhy.example.api.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        home()
        login()
        user()
    }
}