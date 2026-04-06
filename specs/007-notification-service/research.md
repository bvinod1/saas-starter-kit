# Research: Notification Service

## Decision: RabbitMQ Event-Driven Architecture

**Decision**: Notifications triggered via RabbitMQ events (notification.queue). Services publish NotificationEvent; notification-service consumes.
**Rationale**: Decouples notification delivery from business operations. At-least-once delivery guaranteed by RabbitMQ. Dead letter queue for failed events. Multiple services can trigger notifications without knowing the delivery mechanism.
**Alternatives considered**:
- Synchronous HTTP calls: Coupling; notification failure could fail business operation; latency added

## Decision: Channel Provider Registry Pattern

**Decision**: ChannelProviderRegistry maps channel type -> active NotificationChannelProvider. Provider selected at delivery time.
**Rationale**: Multiple providers per channel (email: SendGrid OR Mailgun OR SES). Provider swap via config. New channels added without changing orchestration logic.

## Decision: Mustache Templating

**Decision**: Notification templates use Mustache/Handlebars-style variable substitution ({{variable}}).
**Rationale**: Logic-less templates; safe for admin-authored content (no code injection risk). Widely supported; easy to preview.
**Alternatives considered**:
- Freemarker: More powerful but code injection risk from admin templates

## Decision: User Preference Gating

**Decision**: All non-CRITICAL notifications checked against UserNotificationPreferences before delivery. CRITICAL priority bypasses preference checks.
**Rationale**: User opt-out compliance (CAN-SPAM, GDPR). CRITICAL alerts (security breach, account compromise) must always deliver.

## Best Practices Confirmed

- Email subject deduplication: hash of template + variables prevents duplicate email sends within TTL window
- FCM push requires device token per user; invalid tokens must be purged from preferences
- Webhook delivery: exponential backoff (1s, 2s, 4s, 8s, 16s) with max 5 retries; then FAILED
- In-app notifications stored in DB; polled by frontend on WebSocket ping or HTTP long-poll
- RabbitMQ dead letter queue (notification.dlq) receives events after max redelivery attempts
