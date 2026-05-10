package cherhy.example.util.model

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.vendors.ForUpdateOption
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.r2dbc.Query
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
