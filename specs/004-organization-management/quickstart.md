# Quickstart: Organization Management

## Prerequisites
- User service running (feature 003)
- Java 17, Maven 3.9+

## Start service
```bash
docker compose up -d organisation-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/orgdb` | |
| `RABBITMQ_URL` | `amqp://localhost:5672` | |
| `USER_SERVICE_URL` | `http://localhost:8083` | |

## Create an organization
```bash
curl -X POST http://localhost:8084/api/v1/organizations \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"Engineering","type":"DEPARTMENT"}'
```

## Add a member
```bash
curl -X POST http://localhost:8084/api/v1/organizations/{orgId}/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"userId":"$USER_ID","roleInOrg":"MEMBER"}'
```

## Run tests
```bash
cd backend/organisation-service && mvn test
```
