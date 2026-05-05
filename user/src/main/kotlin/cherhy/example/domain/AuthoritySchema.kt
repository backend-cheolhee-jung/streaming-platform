package cherhy.example.domain

import cherhy.example.util.model.BaseLongIdTable
import com.cherhy.common.util.model.UserId
import org.jetbrains.exposed.v1.core.ResultRow

object Authorities : BaseLongIdTable("authority", "id") {
    val role = varchar("role", 50)
    val userId = long("user_id")
}

fun ResultRow.toAuthorityDomain() = AuthorityDomain(
    id = AuthorityId.of(this[Authorities.id].value),
    userId = UserId.of(this[Authorities.userId]),
    role = Role.valueOf(this[Authorities.role]),
)
