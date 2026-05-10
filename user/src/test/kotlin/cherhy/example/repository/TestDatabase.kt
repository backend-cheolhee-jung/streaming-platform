package cherhy.example.repository

import cherhy.example.util.DatabaseFactory
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    val container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("user_test")
        withUsername("postgres")
        withPassword("postgres")
    }

    private var started = false

    suspend fun start() {
        if (started) return
        container.start()
        val db = R2dbcDatabase.connect(
            url = "r2dbc:postgresql://${container.host}:${container.firstMappedPort}/user_test",
            user = "postgres",
            password = "postgres",
        )
        DatabaseFactory.masterDatabase = db
        DatabaseFactory.slaveDatabase = db
        started = true
        suspendTransaction(db = db) {
            exec(
                """
                CREATE TABLE IF NOT EXISTS "user" (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    email VARCHAR(50) NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    salt VARCHAR(100) NOT NULL,
                    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """.trimIndent()
            )
            exec(
                """
                CREATE TABLE IF NOT EXISTS authority (
                    id BIGSERIAL PRIMARY KEY,
                    role VARCHAR(50) NOT NULL,
                    user_id BIGINT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
