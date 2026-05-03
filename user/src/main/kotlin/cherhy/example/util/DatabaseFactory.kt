package cherhy.example.util

import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

object DatabaseFactory {
    lateinit var masterDb: R2dbcDatabase
    lateinit var slaveDb: R2dbcDatabase
}
