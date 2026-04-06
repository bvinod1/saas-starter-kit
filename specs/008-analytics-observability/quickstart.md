# Quickstart: Analytics & Observability

## Prerequisites
- Docker Compose with monitoring stack
- Java 17, Maven 3.9+

## Start observability stack
```bash
docker compose up -d prometheus grafana otel-collector kibana elasticsearch logstash
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_SERVICE_NAME` | `analytics-service` | |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://otel-collector:4317` | |
| `OTEL_RESOURCE_ATTRIBUTES` | `tenant.id=system` | |
| `MANAGEMENT_PROMETHEUS_ENABLED` | `true` | Actuator metrics |

## Access dashboards
| Dashboard | URL |
|-----------|-----|
| Grafana | http://localhost:3000 (admin/admin) |
| Prometheus | http://localhost:9090 |
| Kibana | http://localhost:5601 |

## Verify metrics endpoint
```bash
curl http://localhost:8087/actuator/prometheus | grep saas_
```

## Check health
```bash
curl http://localhost:8087/actuator/health
```

## Run tests
```bash
cd backend/analytics-service && mvn test
```
