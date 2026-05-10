package cherhy.example.domain

import cherhy.example.util.model.BaseEntity
import cherhy.example.util.model.BaseEntityClass
import cherhy.example.util.model.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Authorities : BaseLongIdTable("authority", "id") {
    val role = varchar("role", 50)
    val userId = long("user_id")
}

class Authority(id: EntityID<Long>) : BaseEntity(id = id, table = Authorities) {
    var role by Authorities.role
    var userId by Authorities.userId

    companion object : BaseEntityClass<Authority>(Authorities)
}
