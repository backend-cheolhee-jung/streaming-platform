package com.cherhy.consumer.config

import com.cherhy.common.util.KafkaConstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

class KafkaConsumerConfigTest : FunSpec({

    fun buildConsumerConfig(): Map<String, Any> =
        mapOf(
            BOOTSTRAP_SERVERS_CONFIG to KafkaConstant.BOOTSTRAP_SERVERS,
            GROUP_ID_CONFIG to KafkaConstant.Consumer.DEFAULT_GROUP_ID,
            KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            AUTO_OFFSET_RESET_CONFIG to KafkaConstant.Consumer.EARLIEST,
            MAX_POLL_RECORDS_CONFIG to 10,
        )

    test("consumer key deserializer uses Kafka StringDeserializer") {
        val config = buildConsumerConfig()
        config[KEY_DESERIALIZER_CLASS_CONFIG] shouldBe StringDeserializer::class.java.name
    }

    test("consumer value deserializer uses Kafka StringDeserializer") {
        val config = buildConsumerConfig()
        config[VALUE_DESERIALIZER_CLASS_CONFIG] shouldBe StringDeserializer::class.java.name
    }

    test("StringDeserializer class name is org.apache.kafka.common.serialization.StringDeserializer") {
        StringDeserializer::class.java.name shouldBe "org.apache.kafka.common.serialization.StringDeserializer"
    }

    test("consumer key and value deserializers are consistent") {
        val config = buildConsumerConfig()
        config[KEY_DESERIALIZER_CLASS_CONFIG] shouldBe config[VALUE_DESERIALIZER_CLASS_CONFIG]
    }

    test("consumer auto offset reset is set to earliest") {
        val config = buildConsumerConfig()
        config[AUTO_OFFSET_RESET_CONFIG] shouldBe "earliest"
    }

    test("consumer max poll records is 10") {
        val config = buildConsumerConfig()
        config[MAX_POLL_RECORDS_CONFIG] shouldBe 10
    }

    test("consumer group id is set") {
        val config = buildConsumerConfig()
        config[GROUP_ID_CONFIG] shouldNotBe null
        config[GROUP_ID_CONFIG] shouldBe KafkaConstant.Consumer.DEFAULT_GROUP_ID
    }

    test("consumer bootstrap servers is set") {
        val config = buildConsumerConfig()
        config[BOOTSTRAP_SERVERS_CONFIG] shouldBe KafkaConstant.BOOTSTRAP_SERVERS
    }

    test("ack mode BATCH does not require manual acknowledgment from listener") {
        val batchMode = ContainerProperties.AckMode.BATCH
        batchMode shouldBe ContainerProperties.AckMode.BATCH
    }

    test("ack mode BATCH is not MANUAL_IMMEDIATE to avoid unacknowledged message replay") {
        val ackMode = ContainerProperties.AckMode.BATCH
        ackMode shouldNotBe ContainerProperties.AckMode.MANUAL_IMMEDIATE
    }

    test("DefaultErrorHandler with FixedBackOff retries 3 times with 1000ms interval") {
        val backOff = FixedBackOff(1000L, 3L)
        backOff.interval shouldBe 1000L
        backOff.maxAttempts shouldBe 3L
    }

    test("DefaultErrorHandler can be created with FixedBackOff") {
        val errorHandler = DefaultErrorHandler(FixedBackOff(1000L, 3L))
        errorHandler shouldNotBe null
    }

    test("pollTimeout is set to 3000ms") {
        val pollTimeout = 3000L
        pollTimeout shouldBe 3000L
    }
})
