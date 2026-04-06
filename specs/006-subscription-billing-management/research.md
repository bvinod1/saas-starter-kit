# Research: Subscription & Billing Management

## Decision: ISubscriptionProvider Interface with Zoho Billing Default

**Decision**: ISubscriptionProvider interface abstracts all billing operations. ZohoBillingProvider is the default concrete implementation. Stripe/Paddle are alternative implementations.
**Rationale**: Constitution I (Interface-First) + Constitution II (Cloud Agnostic). Provider swap requires only config change. No billing provider SDK imported outside the adapter layer.

## Decision: Async CompletableFuture Service Layer

**Decision**: All SubscriptionService methods return CompletableFuture<T> to prevent billing provider HTTP calls from blocking request threads.
**Rationale**: Zoho Billing API calls can take 300-500 ms. Blocking servlet threads at this latency degrades throughput significantly under load.

## Decision: Idempotent Webhook Processing

**Decision**: Webhook events stored in webhook_events table with provider + event_id as unique key. Processing checks for existing event before acting.
**Rationale**: Payment provider webhooks are delivered at-least-once. Duplicate processing can cause double-subscription activation or double-invoice payment.

## Decision: sync_status Tracking

**Decision**: Subscription record tracks sync_status (PENDING/SYNCED/FAILED/RETRY_SCHEDULED/MANUAL_INTERVENTION_REQUIRED) to surface provider sync health.
**Rationale**: Network failures between platform and billing provider can leave local state inconsistent. sync_status makes this visible and actionable.

## Best Practices Confirmed

- Checkout session expires after configurable TTL (default 30 min); expired sessions reject webhook
- Trial periods enforced: trial_start/trial_end on subscription; status = TRIAL during period
- Cancellation: cancel_at_period_end=true sets status=CANCELLING; at period end -> CANCELLED
- Grace period: status=GRACE_PERIOD for 7 days after payment failure before suspension
- Usage tracking written via fire-and-forget to avoid billing API calls blocking user requests
