package cherhy.example.repository

import cherhy.example.domain.Authorities
import cherhy.example.domain.Authority
import cherhy.example.domain.Role
import com.cherhy.common.util.model.UserId

interface AuthorityRepository {
    suspend fun save(
        id: UserId,
        role: Role,
    ): Authority

    suspend fun findOne(
        userId: UserId,
    ): List<Authority>
}

class AuthorityRepositoryImpl : AuthorityRepository {
    override suspend fun save(
        id: UserId,
        role: Role,
    ) =
        Authority.new {
            this.userId = id.value
            this.role = role.name
        }

    override suspend fun findOne(
        userId: UserId,
    ) =
        Authority.find { Authorities.userId eq userId.value }.toList()
}
