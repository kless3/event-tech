# API Gateway

`api-gateway` is the public HTTP entry point for the Event Management System. It routes external API calls to the owning backend service and keeps internal service endpoints away from the public surface.

The gateway is built with Kotlin, Spring Boot 4, and Spring Cloud Gateway Server WebFlux.

## Routes

| Public path | Upstream service |
| --- | --- |
| `/api/v1/users/**` | `user-service` |
| `/api/v1/tickets/**` | `ticket-service` |
| `/api/v1/events/**` | `event-service` |
| `/api/v1/organizers/**` | `event-service` |

## Key Features

- Non-blocking HTTP routing with Spring Cloud Gateway.
- Configurable upstream service URLs.
- Correlation ID propagation through `X-Correlation-Id`.
- Actuator health, info, Prometheus, and Gateway endpoints.
- Dockerfile for standalone image builds.
- Docker Compose integration from the EMS project root.

## Run

From the EMS project root:

```bash
docker compose up --build api-gateway
```

The gateway listens on port `8083` by default.

## Build

```bash
./gradlew test
./gradlew bootJar
```
