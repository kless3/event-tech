package com.ems.ticketservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.clients.event-service")
data class EventServiceClientProperties(
    val baseUrl: String,
)
