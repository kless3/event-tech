package com.ems.userservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Booking Platform User Service API",
        version = "v1",
        description = "User registration, user key access, and GDPR deletion API.",
    ),
)
class OpenApiConfig
