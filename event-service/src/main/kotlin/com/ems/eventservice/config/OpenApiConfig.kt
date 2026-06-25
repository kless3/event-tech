package com.ems.eventservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Booking Platform Event Service API",
        version = "v1",
        description = "Bookable event catalog, availability, cancellation, and organizer creation API.",
    ),
)
class OpenApiConfig
