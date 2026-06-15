package com.ems.ticketservice.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(
    UserServiceClientProperties::class,
    KafkaTopicsProperties::class,
    OutboxProperties::class,
)
class AppConfig {
    @Bean
    fun userServiceRestClient(
        builder: RestClient.Builder,
        properties: UserServiceClientProperties,
    ): RestClient = builder
        .baseUrl(properties.baseUrl)
        .build()
}
