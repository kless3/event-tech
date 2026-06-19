package com.ems.apigateway.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.routes")
data class GatewayRoutesProperties(
    val userServiceBaseUrl: String,
    val ticketServiceBaseUrl: String,
    val eventServiceBaseUrl: String,
    val paymentServiceBaseUrl: String,
)
