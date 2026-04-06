# Quickstart: Tenant Configuration Management

## Prerequisites
- Tenant service running (feature 002)
- Redis running
- Java 17, Maven 3.9+

## Start dependencies
```bash
docker compose up -d redis tenant-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
| `REDIS_URL` | `redis://localhost:6379` | Config cache |
| `CONFIG_ENCRYPTION_KEY` | – | AES-256 base64 key |
| `VAULT_ADDR` | `http://localhost:8200` | For secret configs |

## Set a tenant config value
```bash
curl -X PUT http://localhost:8082/api/v1/tenants/$TENANT_ID/config \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"key":"feature.mfa.required","value":"true"}'
```

## Get resolved config (hierarchy applied)
```bash
curl "http://localhost:8082/api/v1/tenants/$TENANT_ID/config/feature.mfa.required" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Invalidate cache
```bash
curl -X DELETE "http://localhost:8082/api/v1/tenants/$TENANT_ID/config/cache" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
