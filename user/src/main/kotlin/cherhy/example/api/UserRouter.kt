package cherhy.example.api

import cherhy.example.service.ReadUserService
import cherhy.example.service.WriteUserService
import cherhy.example.usecase.LoginCommand
import cherhy.example.usecase.LoginUseCase
import cherhy.example.usecase.SignUpUseCase
import cherhy.example.util.extension.accessToken
import cherhy.example.util.extension.jwt
import cherhy.example.util.extension.refreshToken
import cherhy.example.util.extension.userId
import com.cherhy.common.util.User.GET_ME
import com.cherhy.common.util.User.SIGN_UP
import com.cherhy.common.util.User.UPDATE_USER
import cherhy.example.plugins.reactiveTransaction
import cherhy.example.util.TransactionType.READ_ONLY
import com.cherhy.common.util.AUTHORITY
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin

fun Route.user() {
    post(SIGN_UP) {
        val koin = call.application.getKoin()
        val signUpUseCase = koin.get<SignUpUseCase>()
        val loginUseCase = koin.get<LoginUseCase>()

        val request = call.receive<SignUpRequest>()
        val signUpCommand = request.toCommand()
        signUpUseCase.execute(signUpCommand)

        val loginRequest = LoginCommand.of(signUpCommand.email, signUpCommand.password)
        val jwt = loginUseCase.execute(loginRequest)

        call.response.headers.accessToken = jwt.accessToken
        call.response.cookies.refreshToken = jwt.refreshToken
        call.respond(HttpStatusCode.Created)
    }

    authenticate(AUTHORITY) {
        get(GET_ME) {
            val readUserService = call.application.getKoin().get<ReadUserService>()
            val userId = call.jwt.userId
            val user = reactiveTransaction(READ_ONLY) { readUserService.get(userId) }
            call.respond(HttpStatusCode.OK, user)
        }

        put(UPDATE_USER) {
            val writeUserService = call.application.getKoin().get<WriteUserService>()
            val userId = call.jwt.userId
            val request = call.receive<UserUpdateRequest>()
            val userUpdateCommand = request.toCommand()
            val updatedUser = reactiveTransaction { writeUserService.update(userId, userUpdateCommand) }
            call.respond(HttpStatusCode.OK, updatedUser.id.value)
        }
    }
}
