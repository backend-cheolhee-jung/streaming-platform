package cherhy.example.util

import at.favre.lib.crypto.bcrypt.BCrypt

object Encoder {
    fun encode(
        value: String,
    ) = BCrypt.withDefaults().hashToString(12, value.toCharArray())
        ?: error("BCrypt hashing returned null")

    fun ifMatches(
        value: String,
        encoded: String,
        block: () -> IllegalStateException = { IllegalStateException("Password does not match") },
    ) {
        val isMatched = BCrypt.verifyer().verify(value.toCharArray(), encoded).verified
        if (isMatched.not()) throw block()
    }
}