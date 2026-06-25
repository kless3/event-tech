# E2E Booking Flow

The repository includes a black-box smoke test for the main booking and ticket purchase journey.

## Run

Start the full local stack:

```bash
docker compose up --build
```

In a second terminal, run:

```bash
export KEYCLOAK_ADMIN_PASSWORD='<local keycloak admin password>'
export E2E_USER_PASSWORD='<temporary e2e user password>'

./scripts/e2e-purchase-flow.sh
```

Optional overrides:

```bash
export GATEWAY_URL=http://localhost:8083
export KEYCLOAK_URL=http://localhost:8088
export KEYCLOAK_REALM=booking
export KEYCLOAK_CLIENT_ID=booking-api-gateway
export KEYCLOAK_ADMIN_USER=admin
```

## Covered Flow

The script verifies the main distributed flow through the public API gateway:

1. Creates organizer and buyer records in `user-service`.
2. Creates a matching temporary organizer identity in Keycloak.
3. Creates a bookable event through `/api/v1/organizers/me/events`.
4. Reserves a ticket through `ticket-service`.
5. Waits for `payment-service` to create a payment from the ticket event.
6. Captures the payment and waits for ticket activation.
7. Verifies receipt generation and notification delivery.

The script intentionally creates throwaway test data and is meant for local and CI smoke environments, not production data.
