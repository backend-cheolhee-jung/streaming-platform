package cherhy.example.repository

import cherhy.example.domain.*
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryTest : FunSpec({
    lateinit var repo: UserRepositoryImpl

    beforeSpec {
        TestDatabase.start()
    }

    beforeEach {
        transaction {
            exec("TRUNCATE TABLE authority, \"user\" RESTART IDENTITY CASCADE")
        }
        repo = UserRepositoryImpl()
    }

    test("save creates a user and returns it") {
        val user = transaction {
            runBlocking {
                repo.save(
                    email = UserEmail.of("alice@example.com"),
                    name = Username.of("alice"),
                    password = UserPassword.of("encoded-password-placeholder"),
                    salt = UserSalt.of("some-salt"),
                )
            }
        }
        user.email shouldBe "alice@example.com"
        user.name shouldBe "alice"
    }

    test("isExists returns true when user with given email exists") {
        transaction {
            runBlocking {
                repo.save(
                    email = UserEmail.of("bob@example.com"),
                    name = Username.of("bob"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val exists = transaction { runBlocking { repo.isExists(UserEmail.of("bob@example.com")) } }
        exists shouldBe true
    }

    test("isExists returns false when user does not exist") {
        val exists = transaction { runBlocking { repo.isExists(UserEmail.of("ghost@example.com")) } }
        exists shouldBe false
    }

    test("findOne by email returns the user when found") {
        transaction {
            runBlocking {
                repo.save(
                    email = UserEmail.of("charlie@example.com"),
                    name = Username.of("charlie"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val found = transaction { runBlocking { repo.findOne(UserEmail.of("charlie@example.com")) } }
        found shouldNotBe null
        found!!.email shouldBe "charlie@example.com"
    }

    test("findOne by email returns null when user does not exist") {
        val found = transaction { runBlocking { repo.findOne(UserEmail.of("nobody@example.com")) } }
        found shouldBe null
    }

    test("findOne by userId returns the user when found") {
        val saved = transaction {
            runBlocking {
                repo.save(
                    email = UserEmail.of("dana@example.com"),
                    name = Username.of("dana"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val userId = UserId.of(saved.id.value)
        val found = transaction { runBlocking { repo.findOne(userId) } }
        found shouldNotBe null
        found!!.email shouldBe "dana@example.com"
    }

    test("save with userId updates email and re-encodes password") {
        val saved = transaction {
            runBlocking {
                repo.save(
                    email = UserEmail.of("eve@example.com"),
                    name = Username.of("eve"),
                    password = UserPassword.of("oldpass"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val userId = UserId.of(saved.id.value)
        val updated = transaction {
            runBlocking {
                repo.save(
                    userId = userId,
                    email = UserEmail.of("eve-new@example.com"),
                    password = UserPassword.of("newpass"),
                )
            }
        }
        updated shouldNotBe null
        updated!!.email shouldBe "eve-new@example.com"
    }

    test("save with non-existent userId returns null") {
        val result = transaction {
            runBlocking {
                repo.save(
                    userId = UserId.of(99999L),
                    email = UserEmail.of("x@example.com"),
                    password = UserPassword.of("pass"),
                )
            }
        }
        result shouldBe null
    }
})
