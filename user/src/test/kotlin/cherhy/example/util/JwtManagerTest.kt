package cherhy.example.util

import cherhy.example.domain.Role
import cherhy.example.domain.Username
import com.auth0.jwt.JWT
import com.cherhy.common.util.ROLE
import com.cherhy.common.util.USERNAME
import com.cherhy.common.util.USER_ID
import com.cherhy.common.util.model.UserId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JwtManagerTest : StringSpec({
    val jwtManager = JwtManager()

    "single role is encoded as the role name, not a literal comma" {
        val token = jwtManager.createToken(
            userId = UserId.of(1L),
            userName = Username.of("alice"),
            roles = listOf(Role.UNPAID_MEMBER),
            tokenType = TokenType.ACCESS,
        )

        val decoded = JWT.decode(token)
        decoded.getClaim(ROLE).asString() shouldBe "UNPAID_MEMBER"
        decoded.getClaim(USER_ID).asLong() shouldBe 1L
        decoded.getClaim(USERNAME).asString() shouldBe "alice"
    }

    "multiple roles are joined with comma using their enum names" {
        val token = jwtManager.createToken(
            userId = UserId.of(42L),
            userName = Username.of("bob"),
            roles = listOf(Role.ADMIN, Role.PAID_MEMBER),
            tokenType = TokenType.ACCESS,
        )

        val decoded = JWT.decode(token)
        decoded.getClaim(ROLE).asString() shouldBe "ADMIN,PAID_MEMBER"
    }
})
