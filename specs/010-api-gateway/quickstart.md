# Quickstart: API Gateway

## Prerequisites
- Docker 24+ and Docker Compose v2
- All backend services running

## Start Kong
```bash
docker compose up -d kong
# Verify admin API
curl http://localhost:8001/status
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `KONG_DATABASE` | `off` | DB-less mode |
| `KONG_DECLARATIVE_CONFIG` | `/kong/kong.yml` | Config file path |
| `KONG_PROXY_LISTEN` | `0.0.0.0:8000` | Proxy port |
| `KONG_ADMIN_LISTEN` | `127.0.0.1:8001` | Admin API |

## Reload config
```bash
docker compose exec kong kong reload
```

## Test routing
```bash
# Auth route
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test"}'
```

## Test rate limiting (expect 429 after limit)
```bash
for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8000/api/v1/users \
    -H "X-Tenant-ID: $TENANT_ID" -H "Authorization: Bearer $TOKEN"
done
```

## Validate config
```bash
docker compose exec kong kong check /kong/kong.yml
```
