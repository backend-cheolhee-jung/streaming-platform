package cherhy.example.api

import cherhy.example.usecase.LoginUseCase
import cherhy.example.usecase.RefreshTokenUseCase
import cherhy.example.util.extension.accessToken
import cherhy.example.util.extension.refreshToken
import cherhy.example.util.extension.userId
import com.cherhy.common.util.User.LOGIN
import com.cherhy.common.util.User.LOGOUT
import com.cherhy.common.util.User.REFRESH
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin

fun Route.login() {
    post(LOGIN) {
        val loginUseCase = call.application.getKoin().get<LoginUseCase>()
        val loginRequest = call.receive<LoginRequest>()
        val jwt = loginUseCase.execute(loginRequest.toCommand())

        call.response.headers.accessToken = jwt.accessToken
        call.response.cookies.refreshToken = jwt.refreshToken
        call.respond(HttpStatusCode.Created)
    }

    delete(LOGOUT) {
        call.response.headers.accessToken = null
        call.response.cookies.refreshToken = null
        call.respond(HttpStatusCode.OK)
    }

    post(REFRESH) {
        val refreshTokenUseCase = call.application.getKoin().get<RefreshTokenUseCase>()
        val userId = call.request.headers.userId
        val accessToken = refreshTokenUseCase.execute(userId)

        call.response.headers.accessToken = accessToken
        call.respond(HttpStatusCode.Created)
    }
}
