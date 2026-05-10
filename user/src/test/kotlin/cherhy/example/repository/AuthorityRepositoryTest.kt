package cherhy.example.repository

import cherhy.example.domain.*
import cherhy.example.util.DatabaseFactory
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class AuthorityRepositoryTest : StringSpec({
    lateinit var userRepo: UserRepositoryImpl
    lateinit var authorityRepo: AuthorityRepositoryImpl

    beforeSpec {
        TestDatabase.start()
    }

    beforeEach {
        suspendTransaction(db = DatabaseFactory.masterDatabase) {
            exec("""TRUNCATE TABLE authority, "user" RESTART IDENTITY CASCADE""")
        }
        userRepo = UserRepositoryImpl()
        authorityRepo = AuthorityRepositoryImpl()
    }

    "save creates an authority for a user" {
        val user = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            userRepo.save(
                email = UserEmail.of("alice@example.com"),
                name = Username.of("alice"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val userId = UserId.of(user.id.value)
        val authority = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            authorityRepo.save(userId, Role.UNPAID_MEMBER)
        }
        authority shouldNotBe null
        authority.role shouldBe Role.UNPAID_MEMBER
    }

    "findOne returns authorities for a user" {
        val user = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            userRepo.save(
                email = UserEmail.of("bob@example.com"),
                name = Username.of("bob"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val userId = UserId.of(user.id.value)
        suspendTransaction(db = DatabaseFactory.masterDatabase) {
            authorityRepo.save(userId, Role.UNPAID_MEMBER)
            authorityRepo.save(userId, Role.PAID_MEMBER)
        }
        val authorities = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            authorityRepo.findOne(userId)
        }
        authorities.size shouldBe 2
        authorities.map { it.role }.toSet() shouldBe setOf(Role.UNPAID_MEMBER, Role.PAID_MEMBER)
    }

    "findOne returns empty list when user has no authorities" {
        val user = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            userRepo.save(
                email = UserEmail.of("charlie@example.com"),
                name = Username.of("charlie"),
                password = UserPassword.of("password"),
                salt = UserSalt.of("salt"),
            )
        }
        val userId = UserId.of(user.id.value)
        val authorities = suspendTransaction(db = DatabaseFactory.masterDatabase) {
            authorityRepo.findOne(userId)
        }
        authorities shouldBe emptyList()
    }
})
