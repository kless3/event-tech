package com.ems.paymentservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val pathStyleAccessEnabled: Boolean,
)
