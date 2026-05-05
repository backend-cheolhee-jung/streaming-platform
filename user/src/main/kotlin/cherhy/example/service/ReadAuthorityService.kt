package cherhy.example.service

import cherhy.example.repository.AuthorityRepository
import com.cherhy.common.util.model.UserId

class ReadAuthorityService(
    private val authorityRepository: AuthorityRepository,
) {
    suspend fun get(
        userId: UserId,
    ) =
        authorityRepository.findOne(userId)
            .map { it.role }
}