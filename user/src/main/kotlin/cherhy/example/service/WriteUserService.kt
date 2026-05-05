package cherhy.example.service

import cherhy.example.domain.*
import cherhy.example.repository.UserRepository
import com.cherhy.common.util.model.UserId

class WriteUserService(
    private val userRepository: UserRepository,
) {
    suspend fun create(
        email: UserEmail,
        name: Username,
        password: UserPassword,
        salt: UserSalt,
    ) =
        userRepository.save(email, name, password, salt)

    suspend fun update(
        userId: UserId,
        command: UserUpdateCommand,
    ) =
        userRepository.save(userId, command.email, command.password)
            ?: throw IllegalArgumentException("User not found")
}

data class UserUpdateCommand(
    val email: UserEmail,
    val password: UserPassword,
    val confirmPassword: UserPassword,
) {
    init {
        require(password == confirmPassword) { "password and confirmPassword must be same" }
    }
}