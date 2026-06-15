# Ticket Service

`ticket-service` manages event tickets for the Event Management System. It stores ticket data, protects sensitive ticket payloads with user-scoped encryption keys, and reacts to GDPR-related user lifecycle events.

The service uses synchronous HTTP calls when it needs an immediate answer from `user-service`, such as retrieving a user's DEK for encryption or decryption. Deferred workflows are handled asynchronously through Kafka with an outbox table and idempotent consumers.

## Key Features

- Ticket creation, retrieval, and cancellation.
- Synchronous User Service integration for decrypted user DEK retrieval.
- AES/GCM/NoPadding encryption of sensitive ticket payloads with the user's DEK.
- Kafka-based asynchronous processing for user deletion events.
- GDPR handling: user deletion events erase ticket key references and encrypted personal payloads.
- Transactional outbox for reliable ticket event publishing.
- Idempotent Kafka consumer processing with a processed-message table.
- Liquibase-managed PostgreSQL schema.
- RFC 7807 error responses with `ProblemDetail`.

## Package Layout

```text
client       # synchronous clients for other services
config       # application, Kafka, and HTTP client configuration
controller   # REST API
crypto       # ticket payload encryption and decryption
domain       # JPA entities and enums
dto          # request, response, and event contracts
exception    # domain exceptions and global error handling
mapper       # entity-to-DTO mapping
messaging    # Kafka consumers, outbox publisher, and event factory
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
