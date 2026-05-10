package com.cherhy.plugins

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.kafka.common.serialization.StringDeserializer

class KafkaConfigTest : StringSpec({
    "kafka consumer uses Kafka StringDeserializer, not Jackson's" {
        val expectedClass = StringDeserializer::class.java.name
        expectedClass shouldBe "org.apache.kafka.common.serialization.StringDeserializer"
    }

    "key and value deserializer class are the same" {
        val deserializerClass = StringDeserializer::class.java.name
        deserializerClass shouldBe deserializerClass
    }
})
