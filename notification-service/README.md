# Notification Service

`notification-service` stores and processes transactional notifications for the Booking Platform.

The service consumes booking lifecycle events from Kafka, creates notification records, and marks delivery attempts through a local sender abstraction. It is intentionally structured so real email, SMS, or push providers can be plugged in behind the same delivery boundary.

## Key Features

- Kafka consumers for payment success, payment failure, and event cancellation messages.
- Idempotent processing through a processed-message MongoDB collection.
- Notification records with channel, status, source event, delivery attempts, and failure reason.
- Template service for consistent transactional messages.
- Delivery abstraction for future email, SMS, or push integrations.
- MongoDB document storage for notification records and processed Kafka messages.
- RFC 7807 error responses with `ProblemDetail`.
- OpenAPI documentation through Springdoc.

## Kafka Topics

- `booking.payment.succeeded`
- `booking.payment.failed`
- `booking.event.cancelled`

Invalid or unexpected messages are sent to the configured dead-letter topic suffix.

## Package Layout

```text
api          # OpenAPI controller contracts
config       # application, Kafka, and delivery configuration
controller   # REST API
domain       # MongoDB documents and enums
dto          # response and event contracts
exception    # domain exceptions and global error handling
messaging    # Kafka consumers
repository   # data access
service      # notification orchestration, mapping, templates, sender abstraction
```

## Run

From the Booking Platform project root:

```bash
docker compose up --build notification-service notification-mongodb kafka
```

## Build

```bash
./gradlew test
./gradlew bootJar
```
