package cherhy.example.domain

import com.cherhy.common.util.model.UserId
import org.jetbrains.exposed.v1.core.ResultRow

data class AuthorityDomain(
    val id: AuthorityId,
    val userId: UserId,
    val role: Role,
) {
    companion object {
        @JvmStatic
        fun of(
            row: ResultRow,
        ) = AuthorityDomain(
            id = AuthorityId.of(row[Authorities.id].value),
            userId = UserId.of(row[Authorities.userId]),
            role = Role.valueOf(row[Authorities.role]),
        )
    }
}

@JvmInline
value class AuthorityId private constructor(
    val value: Long,
) : Comparable<AuthorityId> {
    override fun compareTo(
        other: AuthorityId,
    ) = value.compareTo(other.value)

    companion object {
        @JvmStatic
        fun of(
            value: Long,
        ) = AuthorityId(value)
    }
}

enum class Role {
    ADMIN,
    PAID_MEMBER,
    UNPAID_MEMBER,
    ;
}
