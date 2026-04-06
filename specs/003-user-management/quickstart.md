# Quickstart: User Management

## Prerequisites
- Auth and tenant services running (features 001, 002)
- Java 17, Maven 3.9+

## Start service
```bash
docker compose up -d user-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/userdb` | |
| `KEYCLOAK_URL` | `http://localhost:8080` | |
| `RABBITMQ_URL` | `amqp://localhost:5672` | For async events |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | Tracing |

## Create a user
```bash
curl -X POST http://localhost:8083/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","firstName":"Alice","lastName":"Smith","roles":["user"]}'
```

## List users
```bash
curl http://localhost:8083/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Run tests
```bash
cd backend/user-service && mvn test
```
