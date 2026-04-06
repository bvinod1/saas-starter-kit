# Quickstart: Subscription & Billing Management

## Prerequisites
- Tenant service running (feature 002)
- Java 17, Maven 3.9+
- Stripe test API key (or mock)

## Start service
```bash
docker compose up -d billing-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/billingdb` | |
| `STRIPE_SECRET_KEY` | `sk_test_...` | Stripe test key |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` | Webhook signature |
| `RABBITMQ_URL` | `amqp://localhost:5672` | |

## Create a subscription
```bash
curl -X POST http://localhost:8085/api/v1/subscriptions \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -H "Content-Type: application/json" \
  -d '{"planId":"pro","billingCycle":"MONTHLY"}'
```

## Test Stripe webhooks locally
```bash
stripe listen --forward-to http://localhost:8085/api/v1/billing/webhook
```

## Run tests
```bash
cd backend/billing-service && mvn test
```
