# Quickstart: Multi-Tenancy & Tenant Lifecycle

## Prerequisites
- Docker 24+ and Docker Compose v2
- Java 17, Maven 3.9+
- Valid auth token (see feature 001)

## Start services
```bash
docker compose up -d postgres keycloak tenant-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
| `KEYCLOAK_URL` | `http://localhost:8080` | Admin API base |
| `KEYCLOAK_ADMIN_USER` | `admin` | |
| `KEYCLOAK_ADMIN_PASSWORD` | – | Required |
| `RABBITMQ_URL` | `amqp://localhost:5672` | |

## Create a tenant
```bash
curl -X POST http://localhost:8082/api/v1/tenants \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp","subdomain":"acme","planId":"starter"}'
```

## Monitor provisioning
```bash
curl http://localhost:8082/api/v1/tenants/{tenantId}/status \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Run tests
```bash
cd backend/tenant-service && mvn test
```
