package com.ems.eventservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.outbox")
data class OutboxProperties(
    val publishLimit: Int,
    val maxAttempts: Int,
)
