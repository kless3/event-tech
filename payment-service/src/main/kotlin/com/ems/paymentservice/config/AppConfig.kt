package com.ems.paymentservice.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    KafkaTopicsProperties::class,
    OutboxProperties::class,
    ReceiptProperties::class,
    S3Properties::class,
)
class AppConfig
