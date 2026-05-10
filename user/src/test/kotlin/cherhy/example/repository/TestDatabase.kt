package cherhy.example.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    val container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("user_test")
        withUsername("postgres")
        withPassword("postgres")
    }

    private var _db: Database? = null

    fun start(): Database {
        if (!container.isRunning) container.start()
        val db = _db ?: Database.connect(
            HikariDataSource(
                HikariConfig().apply {
                    driverClassName = "org.postgresql.Driver"
                    jdbcUrl = container.jdbcUrl
                    username = container.username
                    password = container.password
                    maximumPoolSize = 5
                    isAutoCommit = true
                }
            )
        ).also { _db = it }
        transaction(db) {
            SchemaUtils.create(
                cherhy.example.domain.Users,
                cherhy.example.domain.Authorities,
            )
        }
        return db
    }
}
