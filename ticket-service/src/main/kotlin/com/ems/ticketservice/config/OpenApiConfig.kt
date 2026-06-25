package com.ems.ticketservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Booking Platform Ticket Service API",
        version = "v1",
        description = "Ticket reservation, cancellation, and event ticket summary API.",
    ),
)
class OpenApiConfig
