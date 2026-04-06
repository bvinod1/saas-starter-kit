# Quickstart: Authentication & Identity Management

## Prerequisites
- Docker 24+ and Docker Compose v2
- Java 17 (Temurin recommended)
- Maven 3.9+

## Start the auth stack locally
```bash
docker compose up -d postgres keycloak
# Wait for Keycloak to be healthy (~30 s)
docker compose up -d auth-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `KEYCLOAK_URL` | `http://localhost:8080` | Keycloak base URL |
| `KEYCLOAK_REALM` | `saas-starter` | Default realm |
| `KEYCLOAK_CLIENT_ID` | `saas-backend` | Service client |
| `KEYCLOAK_CLIENT_SECRET` | – | Required |
| `JWT_SECRET` | – | Signing secret (HS256) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/authdb` | |

## Obtain a token
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin","tenantSubdomain":"demo"}'
```

## Test endpoints
```bash
cd backend/auth-service
mvn test
```

## Useful logs
```bash
docker compose logs -f auth-service
```
