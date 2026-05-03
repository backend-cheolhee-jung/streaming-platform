package com.cherhy.producer.config

import com.cherhy.common.util.KafkaConstant.BOOTSTRAP_SERVERS
import com.cherhy.common.util.KafkaConstant.Producer.ALL
import com.cherhy.common.util.KafkaConstant.Producer.RETRIES
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.support.serializer.JsonSerializer

class KafkaProducerConfigTest : BehaviorSpec({

    fun buildProducerConfig(): Map<String, Any> =
        mapOf(
            BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java.name,
            ACKS_CONFIG to ALL,
            RETRIES_CONFIG to RETRIES,
        )

    Given("KafkaProducerConfig 설정이 구성된 경우") {
        val config = buildProducerConfig()

        When("key serializer를 확인하면") {
            Then("Kafka StringSerializer를 사용한다") {
                config[KEY_SERIALIZER_CLASS_CONFIG] shouldBe StringSerializer::class.java.name
            }

            Then("클래스 이름이 org.apache.kafka.common.serialization.StringSerializer이다") {
                val expectedClass = StringSerializer::class.java.name
                expectedClass shouldBe "org.apache.kafka.common.serialization.StringSerializer"
            }
        }

        When("value serializer를 확인하면") {
            Then("구조화된 페이로드를 위해 JsonSerializer를 사용한다") {
                config[VALUE_SERIALIZER_CLASS_CONFIG] shouldBe JsonSerializer::class.java.name
            }
        }

        When("key serializer와 value serializer를 비교하면") {
            Then("서로 다른 serializer를 사용한다") {
                config[KEY_SERIALIZER_CLASS_CONFIG] shouldNotBe config[VALUE_SERIALIZER_CLASS_CONFIG]
            }
        }

        When("acks 설정을 확인하면") {
            Then("내구성을 위해 'all'로 설정된다") {
                config[ACKS_CONFIG] shouldBe "all"
            }
        }

        When("retries 설정을 확인하면") {
            Then("null이 아니며 100으로 설정된다") {
                config[RETRIES_CONFIG] shouldNotBe null
                config[RETRIES_CONFIG] shouldBe 100
            }
        }

        When("bootstrap servers 설정을 확인하면") {
            Then("BOOTSTRAP_SERVERS 상수와 동일한 값으로 설정된다") {
                config[BOOTSTRAP_SERVERS_CONFIG] shouldBe BOOTSTRAP_SERVERS
            }
        }
    }
})
