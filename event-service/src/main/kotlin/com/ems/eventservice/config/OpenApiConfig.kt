package com.ems.eventservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "EMS Event Service API",
        version = "v1",
        description = "Event catalog, availability, cancellation, and organizer event creation API.",
    ),
)
class OpenApiConfig
