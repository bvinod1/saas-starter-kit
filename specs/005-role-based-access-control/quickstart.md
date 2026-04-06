# Quickstart: Role-Based Access Control

## Prerequisites
- Auth, user, and org services running
- Java 17, Maven 3.9+
- Redis (for permission cache)

## Start dependencies
```bash
docker compose up -d redis
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
| `REDIS_URL` | `redis://localhost:6379` | Permission cache |
| `KEYCLOAK_URL` | `http://localhost:8080` | Role sync |
| `CACHE_TTL_SECONDS` | `300` | |

## Assign a role
```bash
curl -X POST http://localhost:8083/api/v1/users/$USER_ID/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"roleId":"billing-admin"}'
```

## Check permissions
```bash
curl http://localhost:8083/api/v1/users/$USER_ID/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Run tests
```bash
cd backend/user-service && mvn test -Dtest="*RbacTest"
```
