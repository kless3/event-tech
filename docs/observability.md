# Observability

Booking Platform runs a local observability stack for metrics, logs, and distributed traces.

## Services

| Component | URL | Purpose |
| --- | --- | --- |
| Prometheus | http://localhost:9090 | Scrapes Spring Boot actuator metrics |
| Grafana | http://localhost:3000 | Dashboards for metrics, logs, and traces |
| Loki | http://localhost:3100 | Stores container logs |
| Tempo | http://localhost:3200 | Stores distributed traces |
| OpenTelemetry Collector | `localhost:4317`, `localhost:4318` | Receives OTLP traces |

Grafana credentials for local development:

```text
Username: admin
Password: admin
```

## Run

Start the full stack:

```bash
docker compose up --build
```

Start only the observability components:

```bash
docker compose up prometheus grafana loki promtail tempo otel-collector
```

## Smoke Check

After the full stack starts, run:

```bash
./scripts/observability-smoke.sh
```

The script checks Prometheus, Grafana, Loki, Tempo, OpenTelemetry Collector, and all service scrape targets.

To check only the observability infrastructure without application targets:

```bash
CHECK_SERVICE_TARGETS=false ./scripts/observability-smoke.sh
```

## Metrics

Prometheus scrapes these actuator endpoints:

- `api-gateway:8083/actuator/prometheus`
- `user-service:8080/actuator/prometheus`
- `ticket-service:8081/actuator/prometheus`
- `event-service:8082/actuator/prometheus`
- `payment-service:8084/actuator/prometheus`
- `notification-service:8085/actuator/prometheus`
- `importer-service:8086/actuator/prometheus`

The `Booking Platform Overview` dashboard is provisioned automatically in Grafana.

## Alerts

Prometheus loads alert rules from:

```text
observability/prometheus/alerts.yml
```

The baseline rules cover service availability, HTTP 5xx rate, JVM heap pressure, and Hikari connection pool saturation. Grafana also shows the current number of firing alerts on the `Booking Platform Overview` dashboard.

## Logs

Promtail reads Docker container logs from the local Docker host and ships them to Loki with the `job="ems-containers"` label.

## Traces

All Spring Boot services are configured with Micrometer Tracing and the OpenTelemetry OTLP exporter. In Docker Compose they send traces to:

```text
http://otel-collector:4318/v1/traces
```

The collector forwards traces to Tempo.
