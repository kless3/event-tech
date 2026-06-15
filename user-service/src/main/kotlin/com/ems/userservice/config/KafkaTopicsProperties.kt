package com.ems.userservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    val userDeleted: String,
)
