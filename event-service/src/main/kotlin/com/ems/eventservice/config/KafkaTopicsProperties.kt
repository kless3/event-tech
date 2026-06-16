package com.ems.eventservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    val userDeleted: String,
    val ticketCreated: String,
    val eventCreated: String,
    val eventCancelled: String,
    val deadLetterSuffix: String,
) {
    fun deadLetterTopic(topic: String): String = "$topic$deadLetterSuffix"
}
