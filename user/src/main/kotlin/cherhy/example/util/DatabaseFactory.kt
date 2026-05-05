package cherhy.example.util

import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

object DatabaseFactory {
    lateinit var masterDatabase: R2dbcDatabase
    lateinit var slaveDatabase: R2dbcDatabase
}
