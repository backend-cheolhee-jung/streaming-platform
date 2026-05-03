package com.cherhy.consumer.config

import com.cherhy.common.util.KafkaConstant.BOOTSTRAP_SERVERS
import com.cherhy.common.util.KafkaConstant.Consumer.DEFAULT_GROUP_ID
import com.cherhy.common.util.KafkaConstant.Consumer.EARLIEST
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@EnableKafka
@Configuration
class KafkaConsumerConfig {
    // TODO: 이 모듈을 사용하는 다른 모듈에서 Yml 파일을 통해 설정값을 받아오도록 수정, ex) group-id
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
                ConsumerConfig.GROUP_ID_CONFIG to DEFAULT_GROUP_ID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to EARLIEST,
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,
            )
        )

    @Bean
    fun kafkaListenerContainerFactory() =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = consumerFactory()
            setConcurrency(3)
            containerProperties.ackMode = ContainerProperties.AckMode.BATCH
        }
}