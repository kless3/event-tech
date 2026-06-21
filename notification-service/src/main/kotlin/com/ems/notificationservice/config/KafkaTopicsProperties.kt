package com.ems.notificationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    val paymentSucceeded: String,
    val paymentFailed: String,
    val eventCancelled: String,
    val deadLetterSuffix: String,
) {
    fun deadLetterTopic(topic: String): String = "$topic$deadLetterSuffix"
}
