package cherhy.example.domain

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime

object Authorities : LongIdTable("authority", "id") {
    val role = varchar("role", 50)
    val userId = long("user_id")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
