package cherhy.example.repository

import cherhy.example.domain.Authorities
import cherhy.example.domain.AuthorityDomain
import cherhy.example.domain.Role
import com.cherhy.common.util.model.UserId
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll

interface AuthorityRepository {
    suspend fun save(id: UserId, role: Role): AuthorityDomain
    suspend fun findOne(userId: UserId): List<AuthorityDomain>
}

class AuthorityRepositoryImpl : AuthorityRepository {
    override suspend fun save(id: UserId, role: Role): AuthorityDomain {
        val rowId = Authorities.insert {
            it[Authorities.role] = role.name
            it[Authorities.userId] = id.value
            it[Authorities.createdAt] = java.time.LocalDateTime.now()
            it[Authorities.updatedAt] = java.time.LocalDateTime.now()
        }[Authorities.id]
        return Authorities.selectAll().where { Authorities.id eq rowId }
            .toList()
            .single()
            .let(AuthorityDomain::of)
    }

    override suspend fun findOne(userId: UserId): List<AuthorityDomain> =
        Authorities.selectAll().where { Authorities.userId eq userId.value }
            .toList()
            .map(AuthorityDomain::of)
}
