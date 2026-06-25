package com.ems.importerservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Booking Platform Importer Service API",
        version = "v1",
        description = "External event import API for Ticketmaster, Timepad, and future sources."
    )
)
public class OpenApiConfig {
}
