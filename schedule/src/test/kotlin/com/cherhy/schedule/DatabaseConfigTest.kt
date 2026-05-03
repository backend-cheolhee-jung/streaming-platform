package com.cherhy.schedule

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

class DatabaseConfigTest : FunSpec({
    val container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("schedule_test")
        withUsername("postgres")
        withPassword("postgres")
    }

    lateinit var db: Database

    beforeSpec {
        container.start()
        db = Database.connect(
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
        )
    }

    afterSpec {
        container.stop()
    }

    test("database connection is established successfully") {
        db shouldNotBe null
    }

    test("can execute a query against the connected database") {
        val result = transaction(db) {
            exec("SELECT 1 AS value") { rs ->
                rs.next()
                rs.getInt("value")
            }
        }
        result shouldBe 1
    }

    test("HikariConfig sets driverClassName before jdbcUrl") {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            maximumPoolSize = 5
            isAutoCommit = true
        }
        config.driverClassName shouldBe "org.postgresql.Driver"
        config.jdbcUrl shouldBe container.jdbcUrl
    }

    test("can create and query a table in the test database") {
        transaction(db) {
            exec(
                """
                CREATE TABLE IF NOT EXISTS test_table (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL
                )
                """.trimIndent()
            )
            exec("INSERT INTO test_table (name) VALUES ('schedule-test')")
            val count = exec("SELECT COUNT(*) AS cnt FROM test_table") { rs ->
                rs.next()
                rs.getInt("cnt")
            }
            count shouldNotBe 0
        }
    }
})
