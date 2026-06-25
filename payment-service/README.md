# Payment Service

`payment-service` owns the payment lifecycle for the Booking Platform. It stores payment records, publishes payment domain events through the transactional outbox, generates PDF receipts after successful capture, and uploads receipts to S3-compatible storage.

The service participates in the Booking Platform saga choreography:

```text
ticket reserved -> payment created -> payment succeeded/failed -> ticket confirmed/cancelled -> notification
```

## Key Features

- Payment creation with idempotency key support.
- Payment lifecycle statuses: `PENDING`, `SUCCEEDED`, `FAILED`, `CANCELLED`, `REFUNDED`.
- Mock capture and failure endpoints for local saga development.
- PDF receipt generation for successful payments.
- Receipt upload to S3-compatible storage, backed locally by LocalStack.
- Transactional outbox for reliable Kafka publishing.
- Liquibase-managed PostgreSQL schema.
- RFC 7807 error responses with `ProblemDetail`.

## API

Create a payment:

```http
POST /api/v1/payments
Content-Type: application/json

{
  "ticketId": "00000000-0000-0000-0000-000000000001",
  "userId": "00000000-0000-0000-0000-000000000002",
  "eventId": "00000000-0000-0000-0000-000000000003",
  "amount": 49.90,
  "currency": "USD",
  "idempotencyKey": "order-123-payment"
}
```

Capture payment and generate receipt:

```http
POST /api/v1/payments/{paymentId}/capture
```

Fail payment:

```http
POST /api/v1/payments/{paymentId}/fail
Content-Type: application/json

{
  "reason": "insufficient funds"
}
```

## Kafka Events

- `booking.payment.created`
- `booking.payment.succeeded`
- `booking.payment.failed`

## Package Layout

```text
config       # application, Kafka, Liquibase, and S3 configuration
controller   # REST API
domain       # JPA entities and enums
dto          # request, response, and event contracts
exception    # domain exceptions and global error handling
mapper       # entity-to-DTO mapping
messaging    # outbox publisher and event factory
receipt      # PDF generation and S3 storage
repository   # data access
service      # transactional business logic
```

## Run

From the Booking Platform project root:

```bash
docker compose up --build payment-service localstack payment-postgres kafka
```

## Build

```bash
./gradlew test
./gradlew bootJar
```
