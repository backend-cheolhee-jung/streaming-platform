package cherhy.example.domain

import cherhy.example.util.model.BaseLongIdTable
import com.cherhy.common.util.model.UserId
import org.jetbrains.exposed.v1.core.ResultRow

object Users : BaseLongIdTable("user", "id") {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val salt = varchar("salt", 100)
    val isDeleted = bool("is_deleted").default(false)
}

fun ResultRow.toUserDomain() = UserDomain(
    id = UserId.of(this[Users.id].value),
    name = Username.of(this[Users.name]),
    email = UserEmail.of(this[Users.email]),
    password = UserPassword.of(this[Users.password]),
    salt = UserSalt.of(this[Users.salt]),
    isDeleted = UserIsDeleted.of(this[Users.isDeleted]),
)
