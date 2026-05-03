package cherhy.example.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EncoderTest : StringSpec({
    "encode produces a non-empty BCrypt hash" {
        val hash = Encoder.encode("mypassword")
        hash.isNotEmpty() shouldBe true
        hash shouldNotBe "mypassword"
    }

    "encoded hash is at least 60 characters long" {
        val hash = Encoder.encode("test")
        hash.length shouldBe 60
    }

    "ifMatches does not throw when password matches" {
        val raw = "secret123"
        val hash = Encoder.encode(raw)
        // should not throw
        Encoder.ifMatches(raw, hash)
    }

    "ifMatches throws when password does not match" {
        val hash = Encoder.encode("correctpassword")
        shouldThrow<IllegalStateException> {
            Encoder.ifMatches("wrongpassword", hash)
        }
    }

    "ifMatches uses the provided block to create the exception" {
        val hash = Encoder.encode("correctpassword")
        val ex = shouldThrow<IllegalStateException> {
            Encoder.ifMatches("wrong", hash) {
                IllegalStateException("custom error message")
            }
        }
        ex.message shouldBe "custom error message"
    }
})
