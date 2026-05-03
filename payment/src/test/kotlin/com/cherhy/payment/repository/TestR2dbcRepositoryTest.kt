package com.cherhy.payment.repository

import com.cherhy.payment.adapter.out.persistence.TestR2dbcEntity
import com.cherhy.payment.adapter.out.persistence.TestRepositoryCustomImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.insert
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.containers.PostgreSQLContainer

class TestR2dbcRepositoryTest : FunSpec({

    val container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("payment_test")
        withUsername("postgres")
        withPassword("postgres")
    }

    lateinit var connectionFactory: ConnectionFactory
    lateinit var template: R2dbcEntityTemplate
    lateinit var repo: TestRepositoryCustomImpl
    lateinit var dbClient: DatabaseClient

    beforeSpec {
        container.start()
        connectionFactory = ConnectionFactoryBuilder.withUrl(
            "r2dbc:postgresql://${container.host}:${container.getMappedPort(5432)}/${container.databaseName}"
        )
            .username(container.username)
            .password(container.password)
            .build()

        dbClient = DatabaseClient.create(connectionFactory)
        template = R2dbcEntityTemplate(connectionFactory)

        // Create schema
        dbClient.sql(
            """
            CREATE TABLE IF NOT EXISTS test (
                id     BIGSERIAL PRIMARY KEY,
                name   VARCHAR(255) NOT NULL,
                status VARCHAR(30)  NOT NULL
            )
            """.trimIndent()
        ).then().awaitFirstOrNull()

        repo = TestRepositoryCustomImpl(template)
    }

    afterSpec {
        if (container.isRunning) container.stop()
    }

    beforeEach {
        dbClient.sql("TRUNCATE TABLE test RESTART IDENTITY CASCADE")
            .then().awaitFirstOrNull()
    }

    suspend fun insertEntity(name: String, status: String = "ACTIVE"): TestR2dbcEntity {
        return template.insert<TestR2dbcEntity>()
            .using(TestR2dbcEntity(name = name, status = status))
            .awaitFirstOrNull()!!
    }

    test("findAll returns all records when both name and status are null (no NPE)") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "INACTIVE")

        val result = repo.findAll(name = null, status = null, pageable = PageRequest.of(0, 10))
            .toList()

        result shouldHaveSize 2
    }

    test("findAll filters by name when name is provided") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "ACTIVE")

        val result = repo.findAll(name = "Alice", status = null, pageable = PageRequest.of(0, 10))
            .toList()

        result shouldHaveSize 1
        result[0].name shouldBe "Alice"
    }

    test("findAll filters by status when status is provided") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "INACTIVE")

        val result = repo.findAll(name = null, status = "INACTIVE", pageable = PageRequest.of(0, 10))
            .toList()

        result shouldHaveSize 1
        result[0].name shouldBe "Bob"
    }

    test("findAll filters by both name and status") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Alice", "INACTIVE")
        insertEntity("Bob", "ACTIVE")

        val result = repo.findAll(name = "Alice", status = "ACTIVE", pageable = PageRequest.of(0, 10))
            .toList()

        result shouldHaveSize 1
        result[0].name shouldBe "Alice"
        result[0].status shouldBe "ACTIVE"
    }

    test("findAll returns empty list when no records match") {
        insertEntity("Alice", "ACTIVE")

        val result = repo.findAll(name = "Charlie", status = null, pageable = PageRequest.of(0, 10))
            .toList()

        result shouldHaveSize 0
    }

    test("countAll returns correct count when both params are null") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "INACTIVE")

        val count = repo.countAll(name = null, status = null)

        count shouldBe 2L
    }

    test("countAll filters by name") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "ACTIVE")

        val count = repo.countAll(name = "Alice", status = null)

        count shouldBe 1L
    }

    test("countAll filters by status") {
        insertEntity("Alice", "ACTIVE")
        insertEntity("Bob", "INACTIVE")
        insertEntity("Charlie", "INACTIVE")

        val count = repo.countAll(name = null, status = "INACTIVE")

        count shouldBe 2L
    }

    test("countAll returns zero when no records match") {
        insertEntity("Alice", "ACTIVE")

        val count = repo.countAll(name = "Nobody", status = null)

        count shouldBe 0L
    }

    test("inserted entity has a generated id") {
        val entity = insertEntity("TestName", "ACTIVE")

        entity.id shouldNotBe 0L
        entity.name shouldBe "TestName"
        entity.status shouldBe "ACTIVE"
    }
})
