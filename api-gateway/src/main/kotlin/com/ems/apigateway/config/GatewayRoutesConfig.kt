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
            .route("user-service-openapi") { route ->
                route.path("/docs/user-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.userServiceBaseUrl)
            }
            .route("ticket-service") { route ->
                route.path("/api/v1/tickets/**")
                    .uri(routesProperties.ticketServiceBaseUrl)
            }
            .route("ticket-service-openapi") { route ->
                route.path("/docs/ticket-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.ticketServiceBaseUrl)
            }
            .route("event-service") { route ->
                route.path("/api/v1/events/**", "/api/v1/organizers/**")
                    .uri(routesProperties.eventServiceBaseUrl)
            }
            .route("event-service-openapi") { route ->
                route.path("/docs/event-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.eventServiceBaseUrl)
            }
            .route("payment-service") { route ->
                route.path("/api/v1/payments/**")
                    .uri(routesProperties.paymentServiceBaseUrl)
            }
            .route("payment-service-openapi") { route ->
                route.path("/docs/payment-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.paymentServiceBaseUrl)
            }
            .route("notification-service") { route ->
                route.path("/api/v1/notifications/**")
                    .uri(routesProperties.notificationServiceBaseUrl)
            }
            .route("notification-service-openapi") { route ->
                route.path("/docs/notification-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.notificationServiceBaseUrl)
            }
            .route("importer-service") { route ->
                route.path("/api/v1/imports/**")
                    .uri(routesProperties.importerServiceBaseUrl)
            }
            .route("importer-service-openapi") { route ->
                route.path("/docs/importer-service/v3/api-docs")
                    .filters { filter -> filter.setPath("/v3/api-docs") }
                    .uri(routesProperties.importerServiceBaseUrl)
            }
            .build()
}
