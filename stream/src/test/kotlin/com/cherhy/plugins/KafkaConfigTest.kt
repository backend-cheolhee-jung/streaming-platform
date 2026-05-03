package com.cherhy.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer

class KafkaConfigTest : FunSpec({
    test("kafka consumer config uses Kafka StringDeserializer, not Jackson's") {
        val expectedClass = StringDeserializer::class.java.name
        expectedClass shouldBe "org.apache.kafka.common.serialization.StringDeserializer"
    }

    test("kafka consumer key and value deserializer are consistent") {
        val config = buildKafkaConsumerConfig()
        config[KEY_DESERIALIZER_CLASS_CONFIG] shouldBe config[VALUE_DESERIALIZER_CLASS_CONFIG]
    }

    test("kafka consumer deserializer is Kafka StringDeserializer") {
        val config = buildKafkaConsumerConfig()
        val keyDeserializer = config[KEY_DESERIALIZER_CLASS_CONFIG]
        keyDeserializer shouldBe StringDeserializer::class.java.name
    }
})
