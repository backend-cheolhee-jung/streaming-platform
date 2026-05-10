package com.cherhy.payment.repository

import com.cherhy.payment.adapter.out.persistence.TestR2dbcEntity
import com.cherhy.payment.adapter.out.persistence.TestRepositoryCustomImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.shouldNotBeNull
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

class TestR2dbcRepositoryTest : BehaviorSpec({

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
        template = R2dbcEntityTemplate(connectionFactory, PostgresDialect.INSTANCE)

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
        val inserted = template.insert<TestR2dbcEntity>()
            .using(TestR2dbcEntity(name = name, status = status))
            .awaitFirstOrNull()
        inserted.shouldNotBeNull()
        return inserted
    }

    Given("name과 status가 모두 null인 경우") {
        When("findAll을 호출하면") {
            Then("전체 레코드를 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "INACTIVE")

                val result = repo.findAll(name = null, status = null, pageable = PageRequest.of(0, 10))
                    .toList()

                result shouldHaveSize 2
            }
        }
    }

    Given("name만 제공된 경우") {
        When("findAll을 호출하면") {
            Then("해당 name의 레코드만 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "ACTIVE")

                val result = repo.findAll(name = "Alice", status = null, pageable = PageRequest.of(0, 10))
                    .toList()

                result shouldHaveSize 1
                result[0].name shouldBe "Alice"
            }
        }
    }

    Given("status만 제공된 경우") {
        When("findAll을 호출하면") {
            Then("해당 status의 레코드만 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "INACTIVE")

                val result = repo.findAll(name = null, status = "INACTIVE", pageable = PageRequest.of(0, 10))
                    .toList()

                result shouldHaveSize 1
                result[0].name shouldBe "Bob"
            }
        }
    }

    Given("name과 status가 모두 제공된 경우") {
        When("findAll을 호출하면") {
            Then("name과 status가 모두 일치하는 레코드만 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Alice", "INACTIVE")
                insertEntity("Bob", "ACTIVE")

                val result = repo.findAll(name = "Alice", status = "ACTIVE", pageable = PageRequest.of(0, 10))
                    .toList()

                result shouldHaveSize 1
                result[0].name shouldBe "Alice"
                result[0].status shouldBe "ACTIVE"
            }
        }
    }

    Given("일치하는 레코드가 없는 경우") {
        When("findAll을 호출하면") {
            Then("빈 리스트를 반환한다") {
                insertEntity("Alice", "ACTIVE")

                val result = repo.findAll(name = "Charlie", status = null, pageable = PageRequest.of(0, 10))
                    .toList()

                result shouldHaveSize 0
            }
        }
    }

    Given("name과 status가 모두 null인 경우") {
        When("countAll을 호출하면") {
            Then("전체 레코드 수를 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "INACTIVE")

                val count = repo.countAll(name = null, status = null)

                count shouldBe 2L
            }
        }
    }

    Given("name 필터가 있는 경우") {
        When("countAll을 호출하면") {
            Then("해당 name의 레코드 수를 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "ACTIVE")

                val count = repo.countAll(name = "Alice", status = null)

                count shouldBe 1L
            }
        }
    }

    Given("status 필터가 있는 경우") {
        When("countAll을 호출하면") {
            Then("해당 status의 레코드 수를 반환한다") {
                insertEntity("Alice", "ACTIVE")
                insertEntity("Bob", "INACTIVE")
                insertEntity("Charlie", "INACTIVE")

                val count = repo.countAll(name = null, status = "INACTIVE")

                count shouldBe 2L
            }
        }
    }

    Given("일치하는 레코드가 없는 경우") {
        When("countAll을 호출하면") {
            Then("0을 반환한다") {
                insertEntity("Alice", "ACTIVE")

                val count = repo.countAll(name = "Nobody", status = null)

                count shouldBe 0L
            }
        }
    }

    Given("엔티티를 삽입하는 경우") {
        When("insert를 호출하면") {
            Then("생성된 id가 존재하고 저장한 값과 일치한다") {
                val entity = insertEntity("TestName", "ACTIVE")

                entity.id shouldNotBe 0L
                entity.name shouldBe "TestName"
                entity.status shouldBe "ACTIVE"
            }
        }
    }
})
