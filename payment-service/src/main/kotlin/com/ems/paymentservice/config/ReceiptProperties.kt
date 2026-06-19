package com.ems.paymentservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.receipts")
data class ReceiptProperties(
    val bucket: String,
    val publicBaseUrl: String,
)
