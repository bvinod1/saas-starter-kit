# Quickstart: Notification Service

## Prerequisites
- RabbitMQ running
- Java 17, Maven 3.9+
- SendGrid / Twilio test credentials (or mock)

## Start service
```bash
docker compose up -d rabbitmq notification-service
```

## Key environment variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/notifdb` | |
| `RABBITMQ_URL` | `amqp://localhost:5672` | |
| `SENDGRID_API_KEY` | `SG...` | Email provider |
| `TWILIO_ACCOUNT_SID` | – | SMS provider |
| `TWILIO_AUTH_TOKEN` | – | |
| `MOCK_PROVIDERS` | `true` | Skip real sends in dev |

## Send a test notification
```bash
# Publish directly to RabbitMQ exchange
rabbitmqadmin publish exchange=notifications \
  routing_key="$TENANT_ID.user.registered.email" \
  payload='{"userId":"$USER_ID","email":"test@example.com","firstName":"Test"}'
```

## View notification log
```bash
curl http://localhost:8086/api/v1/notifications \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: $TENANT_ID"
```

## Run tests
```bash
cd backend/notification-service && mvn test
```
