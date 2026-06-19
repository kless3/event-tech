package com.ems.paymentservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    val paymentCreated: String,
    val paymentSucceeded: String,
    val paymentFailed: String,
    val deadLetterSuffix: String,
) {
    fun deadLetterTopic(topic: String): String = "$topic$deadLetterSuffix"
}
