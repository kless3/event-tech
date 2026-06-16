package com.ems.eventservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.clients.user-service")
data class UserServiceClientProperties(
    val baseUrl: String,
)
