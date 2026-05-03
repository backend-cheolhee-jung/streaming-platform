package cherhy.example.util.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import java.time.Instant
import java.time.LocalDateTime

abstract class BaseLongIdTable(
    name: String,
    idName: String = "id",
) : LongIdTable(name, idName) {
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    fun Query.pessimisticLock(
        mode: ForUpdateOption.PostgreSQL.MODE? = null,
    ) = this.forUpdate(
        ForUpdateOption.PostgreSQL.ForUpdate(
            mode,
            this@BaseLongIdTable,
        )
    )
}

abstract class BaseEntity(
    id: EntityID<Long>,
    table: BaseLongIdTable,
) : LongEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class BaseEntityClass<E : BaseEntity>(
    table: BaseLongIdTable,
) : LongEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                action.toEntity(this)?.updatedAt = LocalDateTime.now()
            }
        }
    }
}