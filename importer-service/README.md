# Importer Service

`importer-service` imports external bookable events into the Booking Platform catalog.

The service is implemented in Java and isolates provider-specific import logic behind external client adapters. Imported records are normalized before they are sent to `event-service`, which keeps the internal catalog contract independent from third-party APIs.

## Key Features

- Java Spring Boot service for external catalog import workflows.
- Source registry for provider-specific clients.
- Ticketmaster and Timepad import adapters.
- Normalization layer for external event data.
- Manual import API and optional scheduled import job.
- Import history persisted in PostgreSQL.
- Liquibase-managed schema.
- RFC 7807 error responses with `ProblemDetail`.
- OpenAPI documentation through Springdoc.

## Supported Sources

- `TICKETMASTER`
- `TIMEPAD`

The source adapters are currently structured for local and demo imports. Real provider integration can be added behind the existing `ExternalEventClient` contract.

## Package Layout

```text
api          # OpenAPI controller contracts
client       # external source clients and event-service client
config       # application, import, source, and HTTP client configuration
controller   # REST API
domain       # JPA entities and enums
dto          # request and response models
exception    # domain exceptions and global error handling
repository   # data access
service      # import orchestration, normalization, mapping, scheduled job
```

## Run

From the Booking Platform project root:

```bash
docker compose up --build importer-service importer-postgres event-service
```

## Build

```bash
./gradlew test
./gradlew bootJar
```
