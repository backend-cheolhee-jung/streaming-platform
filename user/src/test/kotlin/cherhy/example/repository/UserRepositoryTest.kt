package cherhy.example.repository

import cherhy.example.domain.*
import cherhy.example.util.DatabaseFactory
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class UserRepositoryTest : StringSpec({
    lateinit var repo: UserRepositoryImpl

    beforeSpec {
        TestDatabase.start()
    }

    beforeEach {
        suspendTransaction(db = DatabaseFactory.masterDatabase) {
            exec("""TRUNCATE TABLE authority, "user" RESTART IDENTITY CASCADE""")
        }
        repo = UserRepositoryImpl()
    }

    "save creates a user and returns it" {
        val user = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                email = UserEmail.of("alice@example.com"),
                name = Username.of("alice"),
                password = UserPassword.of("encoded-password-placeholder"),
                salt = UserSalt.of("some-salt"),
            )
        }
        user.email.value shouldBe "alice@example.com"
        user.name.value shouldBe "alice"
    }

    "isExists returns true when user with given email exists" {
        suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                email = UserEmail.of("bob@example.com"),
                name = Username.of("bob"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val exists = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.isExists(UserEmail.of("bob@example.com"))
        }
        exists shouldBe true
    }

    "isExists returns false when user does not exist" {
        val exists = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.isExists(UserEmail.of("ghost@example.com"))
        }
        exists shouldBe false
    }

    "findOne by email returns the user when found" {
        suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                email = UserEmail.of("charlie@example.com"),
                name = Username.of("charlie"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val found = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.findOne(UserEmail.of("charlie@example.com"))
        }
        found shouldNotBe null
        found!!.email.value shouldBe "charlie@example.com"
    }

    "findOne by email returns null when user does not exist" {
        val found = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.findOne(UserEmail.of("nobody@example.com"))
        }
        found shouldBe null
    }

    "findOne by userId returns the user when found" {
        val saved = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                email = UserEmail.of("dana@example.com"),
                name = Username.of("dana"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val userId = UserId.of(saved.id.value)
        val found = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.findOne(userId)
        }
        found shouldNotBe null
        found!!.email.value shouldBe "dana@example.com"
    }

    "save with userId updates email and re-encodes password" {
        val saved = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                email = UserEmail.of("eve@example.com"),
                name = Username.of("eve"),
                password = UserPassword.of("oldpass"),
                salt = UserSalt.of("salt"),
            )
        }
        val userId = UserId.of(saved.id.value)
        val updated = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                userId = userId,
                email = UserEmail.of("eve-new@example.com"),
                password = UserPassword.of("newpass"),
            )
        }
        updated shouldNotBe null
        updated!!.email.value shouldBe "eve-new@example.com"
    }

    "save with non-existent userId returns null" {
        val result = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            repo.save(
                userId = UserId.of(99999L),
                email = UserEmail.of("x@example.com"),
                password = UserPassword.of("pass"),
            )
        }
        result shouldBe null
    }
})
