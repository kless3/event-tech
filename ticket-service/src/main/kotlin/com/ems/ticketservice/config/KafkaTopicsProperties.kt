package com.ems.ticketservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    val userDeleted: String,
    val eventCancelled: String,
    val paymentSucceeded: String,
    val paymentFailed: String,
    val ticketCreated: String,
    val ticketGdprErased: String,
    val deadLetterSuffix: String,
) {
    fun deadLetterTopic(topic: String): String = "$topic$deadLetterSuffix"
}
