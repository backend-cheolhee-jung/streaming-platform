package cherhy.example.repository

import cherhy.example.domain.*
import cherhy.example.util.Encoder
import com.cherhy.common.util.model.UserId
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

interface UserRepository {
    suspend fun save(email: UserEmail, name: Username, password: UserPassword, salt: UserSalt): UserDomain
    suspend fun isExists(email: UserEmail): Boolean
    suspend fun findOne(email: UserEmail): UserDomain?
    suspend fun findOne(userId: UserId): UserDomain?
    suspend fun save(userId: UserId, email: UserEmail, password: UserPassword): UserDomain?
}

class UserRepositoryImpl : UserRepository {
    override suspend fun save(
        email: UserEmail,
        name: Username,
        password: UserPassword,
        salt: UserSalt,
    ): UserDomain {
        val id = Users.insert {
            it[Users.name] = name.value
            it[Users.email] = email.value
            it[Users.password] = password.value
            it[Users.salt] = salt.value
            it[Users.isDeleted] = false
            it[Users.createdAt] = java.time.LocalDateTime.now()
            it[Users.updatedAt] = java.time.LocalDateTime.now()
        }[Users.id]
        return Users.selectAll().where { Users.id eq id }
            .firstOrNull()
            ?.let(UserDomain::of)
            ?: error("Failed to retrieve inserted user")
    }

    override suspend fun save(
        userId: UserId,
        email: UserEmail,
        password: UserPassword,
    ): UserDomain? {
        val existing = Users.selectAll().where { Users.id eq userId.value }
            .firstOrNull()
            ?.let(UserDomain::of) ?: return null

        val encodedPassword = Encoder.encode(password.value + existing.salt.value)

        Users.update({ Users.id eq userId.value }) {
            it[Users.email] = email.value
            it[Users.password] = encodedPassword
            it[Users.updatedAt] = java.time.LocalDateTime.now()
        }

        return Users.selectAll().where { Users.id eq userId.value }
            .firstOrNull()
            ?.let(UserDomain::of)
    }

    override suspend fun isExists(email: UserEmail): Boolean =
        Users.selectAll().where { Users.email eq email.value }.firstOrNull() != null

    override suspend fun findOne(email: UserEmail): UserDomain? =
        Users.selectAll().where { Users.email eq email.value }
            .firstOrNull()
            ?.let(UserDomain::of)

    override suspend fun findOne(userId: UserId): UserDomain? =
        Users.selectAll().where { Users.id eq userId.value }
            .firstOrNull()
            ?.let(UserDomain::of)
}
