package cherhy.example.repository

import cherhy.example.domain.*
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction

class AuthorityRepositoryTest : FunSpec({
    lateinit var userRepo: UserRepositoryImpl
    lateinit var authorityRepo: AuthorityRepositoryImpl

    beforeSpec {
        TestDatabase.start()
    }

    beforeEach {
        transaction {
            exec("TRUNCATE TABLE authority, \"user\" RESTART IDENTITY CASCADE")
        }
        userRepo = UserRepositoryImpl()
        authorityRepo = AuthorityRepositoryImpl()
    }

    test("save creates an authority for a user") {
        val user = transaction {
            runBlocking {
                userRepo.save(
                    email = UserEmail.of("alice@example.com"),
                    name = Username.of("alice"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val userId = UserId.of(user.id.value)
        val authority = transaction {
            runBlocking { authorityRepo.save(userId, Role.UNPAID_MEMBER) }
        }
        authority shouldNotBe null
        authority.role shouldBe Role.UNPAID_MEMBER.name
    }

    test("findOne returns authorities for a user") {
        val user = transaction {
            runBlocking {
                userRepo.save(
                    email = UserEmail.of("bob@example.com"),
                    name = Username.of("bob"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val userId = UserId.of(user.id.value)
        transaction {
            runBlocking {
                authorityRepo.save(userId, Role.UNPAID_MEMBER)
                authorityRepo.save(userId, Role.PAID_MEMBER)
            }
        }
        val authorities = transaction { runBlocking { authorityRepo.findOne(userId) } }
        authorities.size shouldBe 2
        authorities.map { Role.valueOf(it.role) }.toSet() shouldBe setOf(Role.UNPAID_MEMBER, Role.PAID_MEMBER)
    }

    test("findOne returns empty list when user has no authorities") {
        val user = transaction {
            runBlocking {
                userRepo.save(
                    email = UserEmail.of("charlie@example.com"),
                    name = Username.of("charlie"),
                    password = UserPassword.of("password"),
                    salt = UserSalt.of("salt"),
                )
            }
        }
        val userId = UserId.of(user.id.value)
        val authorities = transaction { runBlocking { authorityRepo.findOne(userId) } }
        authorities shouldBe emptyList()
    }
})
