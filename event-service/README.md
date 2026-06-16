# Event Service

`event-service` manages event catalog data, capacity, and lifecycle state for the Event Management System.

The service uses synchronous calls when it needs an immediate answer from another service, such as validating an organizer through `user-service` or reading ticket counts from `ticket-service`. Deferred workflows are processed asynchronously through Kafka with transactional outbox publishing and idempotent consumers.

## Key Features

- Event creation, lookup, availability checks, and cancellation.
- Synchronous User Service integration for organizer validation and sensitive organizer-note encryption.
- Synchronous Ticket Service integration for live ticket summary reads.
- Kafka consumers for `ticket.created` and `user.deleted`.
- Transactional outbox for `event.created` and `event.cancelled`.
- GDPR handling: organizer deletion clears organizer linkage and encrypted organizer notes.
- Liquibase-managed PostgreSQL schema.
- RFC 7807 error responses with `ProblemDetail`.

## Package Layout

```text
client       # synchronous service clients
config       # application, Kafka, and HTTP client configuration
controller   # REST API
crypto       # organizer note encryption
domain       # JPA entities and enums
dto          # request, response, and event contracts
exception    # domain exceptions and global error handling
mapper       # entity-to-DTO mapping
messaging    # Kafka consumers and outbox publisher
repository   # data access
service      # transactional business logic
```

## Run

From the EMS project root:

```bash
docker compose up --build
```

## Build

```bash
./gradlew test
./gradlew bootJar
```
