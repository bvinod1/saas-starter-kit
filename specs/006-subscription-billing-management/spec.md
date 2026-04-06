# Feature Specification: Subscription & Billing Management

**Feature Branch**: `006-subscription-billing-management`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — View available plans (Priority: P1)

Any user can browse available subscription plans with pricing, features, and limits.

**Acceptance Scenarios**:
1. **Given** unauthenticated user, **When** GET /api/v1/subscriptions/plans, **Then** active plans listed.
2. **Given** ENTERPRISE plan, **When** listed, **Then** custom limits shown (max_users, storage_gb, api_calls).

---

### User Story 2 — Purchase subscription via checkout (Priority: P1)

A tenant owner initiates a checkout session that redirects to the payment provider (Zoho Billing).

**Acceptance Scenarios**:
1. **Given** tenant owner, **When** POST /api/v1/subscriptions/checkout with plan_id + billing_cycle, **Then** checkout_url returned.
2. **Given** checkout completed, **When** webhook received, **Then** subscription status updated to ACTIVE.
3. **Given** checkout cancelled, **When** cancel_url loaded, **Then** session status = CANCELLED.

---

### User Story 3 — View current subscription and usage (Priority: P1)

A tenant user views their current plan, billing cycle, usage metrics (API calls, storage, users), and invoices.

**Acceptance Scenarios**:
1. **Given** authenticated user, **When** GET /api/v1/subscriptions/tenant/{tenantId}, **Then** subscription + usage returned.
2. **Given** approaching usage limit, **When** usage > 80%, **Then** warning flag included in response.

---

### User Story 4 — Change or cancel subscription (Priority: P2)

A tenant owner upgrades, downgrades, or cancels their subscription.

**Acceptance Scenarios**:
1. **Given** ACTIVE subscription, **When** PUT with new plan_id, **Then** status = CHANGING_PLAN, new plan applied at period end.
2. **Given** ACTIVE subscription, **When** DELETE, **Then** cancel_at_period_end = true, status = CANCELLING.
3. **Given** GRACE_PERIOD subscription, **When** payment received, **Then** status reverts to ACTIVE.

---

### User Story 5 — Webhook event processing (Priority: P1)

The billing service receives webhooks from Zoho Billing and updates subscription state automatically.

**Acceptance Scenarios**:
1. **Given** subscription.created event, **When** webhook received, **Then** subscription record created.
2. **Given** invoice.paid event, **When** webhook received, **Then** invoice status = PAID, subscription renewed.
3. **Given** subscription.cancelled event, **When** webhook received, **Then** status = CANCELLED.

---

### Edge Cases
- What happens if the webhook is received twice (idempotency)?
- What happens if Zoho is unreachable during checkout?
- How is pro-rata calculated on plan upgrade?

## Requirements

### Functional Requirements
- **FR-001**: System MUST expose a provider-agnostic `ISubscriptionProvider` interface.
- **FR-002**: System MUST implement Zoho Billing as the default provider adapter.
- **FR-003**: System MUST support plan tiers: FREE, STARTER, PROFESSIONAL, ENTERPRISE, CUSTOM.
- **FR-004**: System MUST support billing cycles: MONTHLY, QUARTERLY, ANNUALLY, ONE_TIME.
- **FR-005**: System MUST track subscription statuses: ACTIVE, TRIAL, PAST_DUE, CANCELLED, SUSPENDED, PENDING, EXPIRED, CANCELLING, GRACE_PERIOD, CHANGING_PLAN.
- **FR-006**: System MUST record checkout sessions with status (PENDING, COMPLETED, EXPIRED, CANCELLED, FAILED).
- **FR-007**: System MUST track usage per tenant per subscription.
- **FR-008**: System MUST record all webhook events for audit and replay.
- **FR-009**: System MUST support idempotent webhook processing.
- **FR-010**: Subscription sync status MUST be tracked: PENDING, SYNCED, FAILED, RETRY_SCHEDULED.

### Key Entities
- **Plan**: plan_id, tier, base_price, currency, billing_cycle, max_users, max_storage_gb, max_api_calls
- **Subscription**: tenant_id, plan_id, status, billing_cycle, current_period, trial_start/end, provider, sync_status
- **CheckoutSession**: session_id, tenant_id, plan_id, status, checkout_url, expires_at
- **UsageTracking**: tenant_id, subscription_id, action, quantity, recorded_at
- **WebhookEvent**: provider, event_type, payload, processed_at, status

### Non-Functional Requirements
- Webhook processing MUST be idempotent
- Checkout session creation MUST complete in < 1 s
- Provider adapter swap MUST require only config change, no code change
