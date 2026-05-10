package com.cherhy.consumer.config

import com.cherhy.common.util.KafkaConstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.listener.ContainerProperties

class KafkaConsumerConfigTest : BehaviorSpec({

    val kafkaConsumerConfig = KafkaConsumerConfig()

    afterEach { }

    Given("KafkaConsumerConfig의 consumerFactory가 생성되었을 때") {
        val consumerFactory = kafkaConsumerConfig.consumerFactory()
        val config = consumerFactory.configurationProperties

        When("key deserializer 설정을 조회하면") {
            Then("Kafka StringDeserializer가 사용된다") {
                config[KEY_DESERIALIZER_CLASS_CONFIG] shouldBe StringDeserializer::class.java.name
            }
        }

        When("value deserializer 설정을 조회하면") {
            Then("Kafka StringDeserializer가 사용된다") {
                config[VALUE_DESERIALIZER_CLASS_CONFIG] shouldBe StringDeserializer::class.java.name
            }
        }

        When("key와 value deserializer 설정을 비교하면") {
            Then("동일한 StringDeserializer를 사용한다") {
                config[KEY_DESERIALIZER_CLASS_CONFIG] shouldBe config[VALUE_DESERIALIZER_CLASS_CONFIG]
            }
        }

        When("auto offset reset 설정을 조회하면") {
            Then("earliest로 설정되어 있다") {
                config[AUTO_OFFSET_RESET_CONFIG] shouldBe KafkaConstant.Consumer.EARLIEST
            }
        }

        When("max poll records 설정을 조회하면") {
            Then("10으로 설정되어 있다") {
                config[MAX_POLL_RECORDS_CONFIG] shouldBe 10
            }
        }

        When("group id 설정을 조회하면") {
            Then("null이 아니며 DEFAULT_GROUP_ID와 일치한다") {
                config[GROUP_ID_CONFIG] shouldNotBe null
                config[GROUP_ID_CONFIG] shouldBe KafkaConstant.Consumer.DEFAULT_GROUP_ID
            }
        }

        When("bootstrap servers 설정을 조회하면") {
            Then("KafkaConstant.BOOTSTRAP_SERVERS와 일치한다") {
                config[BOOTSTRAP_SERVERS_CONFIG] shouldBe KafkaConstant.BOOTSTRAP_SERVERS
            }
        }
    }

    Given("KafkaConsumerConfig의 kafkaListenerContainerFactory가 생성되었을 때") {
        val containerFactory = kafkaConsumerConfig.kafkaListenerContainerFactory()

        When("ack mode를 조회하면") {
            Then("BATCH 모드로 설정되어 있다") {
                containerFactory.containerProperties.ackMode shouldBe ContainerProperties.AckMode.BATCH
            }
        }

        When("ack mode가 MANUAL_IMMEDIATE인지 확인하면") {
            Then("MANUAL_IMMEDIATE가 아니어서 unacknowledged message replay가 발생하지 않는다") {
                containerFactory.containerProperties.ackMode shouldNotBe ContainerProperties.AckMode.MANUAL_IMMEDIATE
            }
        }

        When("pollTimeout을 조회하면") {
            Then("3000ms로 설정되어 있다") {
                containerFactory.containerProperties.pollTimeout shouldBe 3000L
            }
        }

        When("errorHandler를 조회하면") {
            Then("null이 아니다") {
                containerFactory.commonErrorHandler shouldNotBe null
            }
        }
    }
})
