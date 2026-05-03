package cherhy.example.domain

import com.cherhy.common.util.model.UserId
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime

object Authorities : LongIdTable("authority", "id") {
    val role = varchar("role", 50)
    val userId = long("user_id")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

fun ResultRow.toAuthorityDomain() = AuthorityDomain(
    id = AuthorityId.of(this[Authorities.id].value),
    userId = UserId.of(this[Authorities.userId]),
    role = Role.valueOf(this[Authorities.role]),
)
