package com.cherhy.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    val container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("stream_test")
        withUsername("postgres")
        withPassword("postgres")
    }

    fun start(): Database {
        if (!container.isRunning) container.start()
        val db = Database.connect(
            dataSource = HikariDataSource(
                HikariConfig().apply {
                    driverClassName = "org.postgresql.Driver"
                    jdbcUrl = container.jdbcUrl
                    username = container.username
                    password = container.password
                    maximumPoolSize = 5
                    isAutoCommit = true
                }
            ),
            dialect = PostgreSqlDialect(),
        )
        db.useConnection { conn ->
            conn.createStatement().execute(ddl)
        }
        return db
    }

    val ddl = """
        CREATE TABLE IF NOT EXISTS post (
            id BIGSERIAL PRIMARY KEY,
            title VARCHAR(200) NOT NULL,
            content TEXT NOT NULL,
            author BIGINT NOT NULL,
            category VARCHAR(50) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        );
        CREATE TABLE IF NOT EXISTS video (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(200) NOT NULL,
            unique_name VARCHAR(200) NOT NULL,
            size BIGINT NOT NULL,
            extension VARCHAR(20) NOT NULL,
            price DECIMAL(10,2) NOT NULL DEFAULT 0,
            owner BIGINT NOT NULL,
            post BIGINT NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        );
        CREATE TABLE IF NOT EXISTS purchased_video (
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL,
            video_id BIGINT NOT NULL,
            price DECIMAL(10,2) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        );
    """.trimIndent()
}
