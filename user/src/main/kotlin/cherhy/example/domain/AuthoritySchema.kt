package cherhy.example.domain

import cherhy.example.util.model.BaseLongIdTable

object Authorities : BaseLongIdTable("authority", "id") {
    val role = varchar("role", 50)
    val userId = long("user_id")
}
