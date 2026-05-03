package cherhy.example.domain

import com.cherhy.common.util.model.UserId

data class UserDomain(
    val id: UserId,
    val name: Username,
    val email: UserEmail,
    val password: UserPassword,
    val salt: UserSalt,
    var isDeleted: UserIsDeleted = UserIsDeleted.of(false),
)

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