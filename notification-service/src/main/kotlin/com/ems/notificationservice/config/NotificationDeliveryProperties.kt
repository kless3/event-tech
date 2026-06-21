package com.ems.notificationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.delivery")
data class NotificationDeliveryProperties(
    val maxAttempts: Int = 3,
)
