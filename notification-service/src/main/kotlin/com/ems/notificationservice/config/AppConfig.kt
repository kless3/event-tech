package com.ems.notificationservice.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties::class, NotificationDeliveryProperties::class)
class AppConfig
