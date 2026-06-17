package com.ems.apigateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRoutesConfig(
    private val routesProperties: GatewayRoutesProperties,
) {
    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator =
        builder.routes()
            .route("user-service") { route ->
                route.path("/api/v1/users/**")
                    .uri(routesProperties.userServiceBaseUrl)
            }
            .route("ticket-service") { route ->
                route.path("/api/v1/tickets/**")
                    .uri(routesProperties.ticketServiceBaseUrl)
            }
            .route("event-service") { route ->
                route.path("/api/v1/events/**", "/api/v1/organizers/**")
                    .uri(routesProperties.eventServiceBaseUrl)
            }
            .build()
}
