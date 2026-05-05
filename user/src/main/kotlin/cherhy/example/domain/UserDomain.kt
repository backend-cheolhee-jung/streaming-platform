package cherhy.example.domain

import com.cherhy.common.util.model.UserId
import org.jetbrains.exposed.v1.core.ResultRow

data class UserDomain(
    val id: UserId,
    val name: Username,
    val email: UserEmail,
    val password: UserPassword,
    val salt: UserSalt,
    var isDeleted: UserIsDeleted = UserIsDeleted.of(false),
) {
    companion object {
        @JvmStatic
        fun of(
            row: ResultRow,
        ) = UserDomain(
            id = UserId.of(row[Users.id].value),
            name = Username.of(row[Users.name]),
            email = UserEmail.of(row[Users.email]),
            password = UserPassword.of(row[Users.password]),
            salt = UserSalt.of(row[Users.salt]),
            isDeleted = UserIsDeleted.of(row[Users.isDeleted]),
        )
    }
}

@JvmInline
value class Username private constructor(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = Username(value)
    }
}

@JvmInline
value class UserEmail private constructor(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = UserEmail(value)
    }
}

@JvmInline
value class UserPassword private constructor(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = UserPassword(value)
    }
}

@JvmInline
value class UserSalt private constructor(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = UserSalt(value)
    }
}

@JvmInline
value class UserIsDeleted private constructor(
    val value: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(
            value: Boolean,
        ) = UserIsDeleted(value)
    }
}