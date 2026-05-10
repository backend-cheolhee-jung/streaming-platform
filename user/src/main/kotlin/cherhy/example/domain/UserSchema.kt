package cherhy.example.domain

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime

object Users : LongIdTable("user", "id") {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val salt = varchar("salt", 100)
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
