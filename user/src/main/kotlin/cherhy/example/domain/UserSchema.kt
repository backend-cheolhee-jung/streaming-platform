package cherhy.example.domain

import cherhy.example.util.model.BaseLongIdTable

object Users : BaseLongIdTable("user", "id") {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val salt = varchar("salt", 100)
    val isDeleted = bool("is_deleted").default(false)
}
