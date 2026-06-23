package com.ems.notificationservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "EMS Notification Service API",
        version = "v1",
        description = "Notification lookup API for asynchronous delivery records.",
    ),
)
class OpenApiConfig
