package com.cherhy.plugins

import com.cherhy.common.util.KafkaConstant.BOOTSTRAP_SERVERS
import com.cherhy.common.util.KafkaConstant.Consumer.DEFAULT_GROUP_ID
import com.cherhy.common.util.KafkaConstant.Consumer.EARLIEST
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer

val consumer = KafkaConsumer<String, String>(
    mapOf(
        BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
        GROUP_ID_CONFIG to DEFAULT_GROUP_ID,
        KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        AUTO_OFFSET_RESET_CONFIG to EARLIEST,
        MAX_POLL_RECORDS_CONFIG to 10,
    )
)
