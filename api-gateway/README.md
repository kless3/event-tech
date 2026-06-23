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
| `/api/v1/payments/**` | `payment-service` |
| `/api/v1/notifications/**` | `notification-service` |
| `/api/v1/imports/**` | `importer-service` |

## Auth

The gateway validates Keycloak JWT access tokens from the `ems` realm, maps realm/client roles to Spring Security roles, and forwards trusted user context to downstream services with:

- `X-Authenticated-User-Id`
- `X-Authenticated-Username`
- `X-Authenticated-User-Roles`

Local Keycloak users:

| Username | Password | Roles |
| --- | --- | --- |
| `user@ems.local` | `password` | `USER` |
| `organizer@ems.local` | `password` | `USER`, `ORGANIZER` |
| `admin@ems.local` | `password` | `USER`, `ORGANIZER`, `ADMIN` |

Role rules:

| Public path | Access |
| --- | --- |
| `GET /api/v1/events/**` | Public |
| `POST /api/v1/users/**` | Public |
| `/api/v1/tickets/**` | `USER`, `ORGANIZER`, `ADMIN` |
| `/api/v1/payments/**` | `USER`, `ORGANIZER`, `ADMIN` |
| `/api/v1/users/**` | `USER`, `ADMIN` |
| `POST /api/v1/events/**` | Denied at the public gateway; internal service-to-service API only |
| `PUT/PATCH/DELETE /api/v1/events/**` | `ORGANIZER`, `ADMIN` |
| `/api/v1/organizers/**` | `ORGANIZER`, `ADMIN` |
| `/api/v1/imports/**` | `ORGANIZER`, `ADMIN` |
| `/api/v1/notifications/**` | `ADMIN` |

Get a local access token:

```bash
curl -s \
  -d "client_id=ems-api-gateway" \
  -d "username=organizer@ems.local" \
  -d "password=password" \
  -d "grant_type=password" \
  http://localhost:8088/realms/ems/protocol/openid-connect/token
```

Or use the helper from the repository root:

```bash
./scripts/keycloak-token.sh organizer@ems.local password
```

Use the token through the gateway:

```bash
TOKEN="<access_token>"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8083/api/v1/imports/ticketmaster
```

Create an event as the authenticated organizer:

```bash
curl -X POST http://localhost:8083/api/v1/organizers/me/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Event Summit",
    "description": "Tech conference",
    "location": "Berlin",
    "startsAt": "2030-06-17T18:00:00",
    "capacity": 500,
    "organizerNote": "speaker room setup"
  }'
```

## Key Features

- Non-blocking HTTP routing with Spring Cloud Gateway.
- Configurable upstream service URLs.
- Correlation ID propagation through `X-Correlation-Id`.
- Keycloak JWT validation with RBAC enforcement.
- Authenticated user context propagation to downstream services.
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
