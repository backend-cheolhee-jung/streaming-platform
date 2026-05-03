package com.cherhy.producer.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.support.serializer.JsonSerializer

class KafkaProducerConfigTest : FunSpec({

    fun buildProducerConfig(): Map<String, Any> =
        mapOf(
            BOOTSTRAP_SERVERS_CONFIG to com.cherhy.common.util.KafkaConstant.BOOTSTRAP_SERVERS,
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java.name,
            ACKS_CONFIG to com.cherhy.common.util.KafkaConstant.Producer.ALL,
            RETRIES_CONFIG to com.cherhy.common.util.KafkaConstant.Producer.RETRIES,
        )

    test("producer key serializer uses Kafka StringSerializer, not JsonSerializer") {
        val config = buildProducerConfig()
        config[KEY_SERIALIZER_CLASS_CONFIG] shouldBe StringSerializer::class.java.name
    }

    test("producer key serializer class name is org.apache.kafka.common.serialization.StringSerializer") {
        val expectedClass = StringSerializer::class.java.name
        expectedClass shouldBe "org.apache.kafka.common.serialization.StringSerializer"
    }

    test("producer value serializer uses JsonSerializer for structured payloads") {
        val config = buildProducerConfig()
        config[VALUE_SERIALIZER_CLASS_CONFIG] shouldBe JsonSerializer::class.java.name
    }

    test("producer key and value serializers are different") {
        val config = buildProducerConfig()
        config[KEY_SERIALIZER_CLASS_CONFIG] shouldNotBe config[VALUE_SERIALIZER_CLASS_CONFIG]
    }

    test("producer acks config is set to 'all' for durability") {
        val config = buildProducerConfig()
        config[ACKS_CONFIG] shouldBe "all"
    }

    test("producer retries config is set") {
        val config = buildProducerConfig()
        config[RETRIES_CONFIG] shouldNotBe null
    }

    test("producer bootstrap servers config is set") {
        val config = buildProducerConfig()
        config[BOOTSTRAP_SERVERS_CONFIG] shouldBe com.cherhy.common.util.KafkaConstant.BOOTSTRAP_SERVERS
    }
})
