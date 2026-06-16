package com.ems.eventservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.clients.ticket-service")
data class TicketServiceClientProperties(
    val baseUrl: String,
)
