package cherhy.example.util

import cherhy.example.domain.Role
import cherhy.example.domain.Username
import com.auth0.jwt.JWT
import com.cherhy.common.util.ROLE
import com.cherhy.common.util.USERNAME
import com.cherhy.common.util.USER_ID
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class JwtManagerTest : BehaviorSpec({
    val jwtManager = JwtManager()

    Given("단일 역할을 가진 사용자가") {
        When("액세스 토큰을 생성하면") {
            val token = jwtManager.createToken(
                userId = UserId.of(1L),
                userName = Username.of("alice"),
                roles = listOf(Role.UNPAID_MEMBER),
                tokenType = TokenType.ACCESS,
            )

            Then("역할이 enum 이름으로 단독 인코딩된다") {
                val decoded = JWT.decode(token)
                decoded.getClaim(ROLE).asString() shouldBe "UNPAID_MEMBER"
                decoded.getClaim(USER_ID).asLong() shouldBe 1L
                decoded.getClaim(USERNAME).asString() shouldBe "alice"
            }
        }
    }

    Given("복수 역할을 가진 사용자가") {
        When("액세스 토큰을 생성하면") {
            val token = jwtManager.createToken(
                userId = UserId.of(42L),
                userName = Username.of("bob"),
                roles = listOf(Role.ADMIN, Role.PAID_MEMBER),
                tokenType = TokenType.ACCESS,
            )

            Then("역할들이 콤마로 구분된 enum 이름으로 인코딩된다") {
                val decoded = JWT.decode(token)
                decoded.getClaim(ROLE).asString() shouldBe "ADMIN,PAID_MEMBER"
            }
        }
    }
})
