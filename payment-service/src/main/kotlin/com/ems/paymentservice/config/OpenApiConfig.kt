package com.ems.paymentservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "EMS Payment Service API",
        version = "v1",
        description = "Payment lifecycle, payment receipt, and receipt storage API.",
    ),
)
class OpenApiConfig
