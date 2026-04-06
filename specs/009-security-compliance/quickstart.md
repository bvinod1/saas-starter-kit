# Quickstart: Security & Compliance

## Prerequisites
- All core services running
- Java 17, Maven 3.9+
- Vault (for PHI key management)

## Start Vault (dev mode)
```bash
docker compose up -d vault
vault operator init -key-shares=1 -key-threshold=1
vault unseal $UNSEAL_KEY
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `VAULT_ADDR` | `http://localhost:8200` | |
| `VAULT_TOKEN` | `root` (dev mode) | |
| `PHI_ENCRYPTION_KEY_PATH` | `secret/saas/dev/global/phi_key` | |
| `GDPR_ERASURE_TIMEOUT_DAYS` | `30` | Legal deadline |
| `RABBITMQ_URL` | `amqp://localhost:5672` | For erasure events |

## Submit a data erasure request
```bash
curl -X POST http://localhost:8083/api/v1/users/$USER_ID/erasure \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Check erasure status
```bash
curl http://localhost:8083/api/v1/users/$USER_ID/erasure/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Update consent
```bash
curl -X PUT http://localhost:8083/api/v1/users/$USER_ID/consent \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"consentType":"MARKETING","consented":false}'
```
