package cherhy.example.domain

import cherhy.example.util.model.BaseEntity
import cherhy.example.util.model.BaseEntityClass
import cherhy.example.util.model.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Users : BaseLongIdTable("user", "id") {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val salt = varchar("salt", 50)
    val isDeleted = bool("is_deleted").default(false)
}

class User(id: EntityID<Long>) : BaseEntity(id = id, table = Users) {
    var name by Users.name
    var email by Users.email
    var password by Users.password
    var salt by Users.salt
    var isDeleted by Users.isDeleted

    companion object : BaseEntityClass<User>(Users)
}
