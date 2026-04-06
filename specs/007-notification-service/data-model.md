# Data Model: Notification Service

## Entity: NotificationTemplate
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | NULL = global template |
| event_type | VARCHAR(100) | e.g. `user.registered` |
| channel | VARCHAR(20) | EMAIL, SMS, IN_APP, PUSH |
| subject | VARCHAR(300) | Email subject / push title |
| body | TEXT | Mustache template |
| active | BOOLEAN | |
| created_at | TIMESTAMPTZ | |

## Entity: Notification
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| recipient_user_id | UUID | |
| template_id | UUID (FK) | |
| channel | VARCHAR(20) | Resolved channel |
| status | notif_status ENUM | QUEUED, SENT, DELIVERED, FAILED, SKIPPED |
| payload | JSONB | Rendered variables |
| sent_at | TIMESTAMPTZ | |
| failed_reason | TEXT | |
| retry_count | SMALLINT | |
| created_at | TIMESTAMPTZ | |

## Entity: NotificationPreference
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | |
| tenant_id | UUID | |
| event_type | VARCHAR(100) | |
| channel | VARCHAR(20) | |
| enabled | BOOLEAN | DEFAULT true |
| updated_at | TIMESTAMPTZ | |

## Message Queue Schema (RabbitMQ)
- Exchange: `notifications` (topic)
- Routing key: `{tenant_id}.{event_type}.{channel}`
- DLQ: `notifications.dlq`
- Retry policy: 3 attempts / 30 s backoff

## Relationships
```
Tenant 1 ──< NotificationTemplate
Tenant 1 ──< Notification
User 1 ──< NotificationPreference
NotificationTemplate 1 ──< Notification
```

## Indexes
- `notifications(tenant_id, recipient_user_id, status)`
- `notifications(status, retry_count)` – retry sweep
- `notification_preferences(user_id, tenant_id, event_type, channel)` UNIQUE

## State Transitions (Notification)
```
QUEUED → SENT → DELIVERED
SENT → FAILED (delivery error)
FAILED → QUEUED (retry ≤ 3)
FAILED → SKIPPED (max retries exceeded)
QUEUED → SKIPPED (user opted out)
```

## Migration References
- `database/migrations/03-add-notification-preferences.sql`
