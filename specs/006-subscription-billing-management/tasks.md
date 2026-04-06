# Tasks: Subscription & Billing Management

**Feature**: `006-subscription-billing-management`
**Input**: `specs/006-subscription-billing-management/`
**Source**: `backend/billing-service/`

## Phase 1: Setup

- [ ] T001 Add dependencies to `backend/billing-service/pom.xml` (spring-data-jpa, spring-web, spring-amqp, zoho-billing-sdk or http-client, opentelemetry-spring-starter)
- [ ] T002 [P] Configure `backend/billing-service/src/main/resources/application.yml` (datasource, RabbitMQ, provider settings, webhook secret)
- [ ] T003 [P] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/provider/ISubscriptionProvider.java` interface (`createCheckoutSession`, `cancelSubscription`, `getSubscription`, `handleWebhook`)

---

## Phase 2: Foundational

- [ ] T004 Create `database/migrations/04-subscription-management.sql` — `plans`, `subscriptions`, `checkout_sessions`, `invoices`, `usage_tracking`, `webhook_events` tables
- [ ] T005 [P] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/entity/Plan.java`, `Subscription.java`, `Invoice.java`, `CheckoutSession.java`, `WebhookEvent.java` JPA entities
- [ ] T006 [P] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/repository/SubscriptionRepository.java`, `InvoiceRepository.java`, `WebhookEventRepository.java`
- [ ] T007 Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/provider/ZohoBillingProvider.java` implementing `ISubscriptionProvider` (Zoho Billing API adapter)
- [ ] T008 Seed `plans` table with FREE, STARTER, PROFESSIONAL, ENTERPRISE, CUSTOM tiers in migration

**Checkpoint**: Billing foundation ready

---

## Phase 3: User Story 1 + 3 — View Plans & Subscription Status (Priority: P1) 🎯 MVP

**Goal**: Any user views plans; authenticated user sees their current subscription and usage.

**Independent Test**: GET /api/v1/subscriptions/plans returns all active plans (no auth). GET /api/v1/subscriptions/tenant/{tenantId} returns subscription + usage.

- [ ] T009 [P] [US1] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/dto/PlanResponse.java`, `SubscriptionResponse.java`
- [ ] T010 [US1] Implement `backend/billing-service/src/main/java/com/saasstarterkit/billing/service/BillingService.java` — `listPlans()`, `getCurrentSubscription(tenantId)`, `getUsage(tenantId)`
- [ ] T011 [US1] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/controller/BillingController.java` with `GET /api/v1/subscriptions/plans` (public) and `GET /api/v1/subscriptions/tenant/{tenantId}`
- [ ] T012 [US3] Implement `BillingService.getUsage(tenantId)` — query `usage_tracking` for current billing period; add `warningFlag` if > 80% of any limit

**Checkpoint**: Plan listing and subscription status independently testable

---

## Phase 4: User Story 2 + 5 — Checkout & Webhooks (Priority: P1)

**Goal**: Tenant owner creates checkout session; webhook updates subscription state idempotently.

**Independent Test**: POST /api/v1/subscriptions/checkout returns checkout_url. POST /api/v1/billing/webhook with `invoice.paid` transitions subscription to ACTIVE. Duplicate webhook ignored.

- [ ] T013 [P] [US2] Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/dto/CreateCheckoutRequest.java`
- [ ] T014 [US2] Implement `BillingService.createCheckoutSession(tenantId, request)` — call `ISubscriptionProvider.createCheckoutSession()`, persist `CheckoutSession`, return checkout_url
- [ ] T015 [US2] Add `POST /api/v1/subscriptions/checkout` to `BillingController`
- [ ] T016 [US5] Implement `BillingService.handleWebhook(provider, payload, signature)` — validate signature, idempotency check via `WebhookEvent.event_id`, dispatch to event-specific handler
- [ ] T017 [US5] Implement webhook handlers: `onSubscriptionCreated`, `onInvoicePaid`, `onSubscriptionCancelled` inside `BillingService`
- [ ] T018 [US5] Add `POST /api/v1/billing/webhook` (public, signature-verified) to `BillingController`

**Checkpoint**: Checkout and webhook processing independently testable

---

## Phase 5: User Story 4 — Change/Cancel Subscription (Priority: P2)

**Goal**: Tenant owner can upgrade, downgrade, or cancel subscription at period end.

**Independent Test**: PUT /api/v1/subscriptions/{id} with new plan sets status=CHANGING_PLAN. DELETE cancels at period end.

- [ ] T019 [P] [US4] Implement `BillingService.changeSubscriptionPlan(subscriptionId, newPlanId)` — call provider, update status=CHANGING_PLAN
- [ ] T020 [US4] Implement `BillingService.cancelSubscription(subscriptionId, cancelAtPeriodEnd)` — call provider, set cancel_at_period_end + status=CANCELLING
- [ ] T021 [US4] Add `PUT /api/v1/subscriptions/{id}` and `DELETE /api/v1/subscriptions/{id}` to `BillingController`

**Checkpoint**: Subscription lifecycle management independently testable

---

## Phase 6a: User Data Erasure Consumer (Cross-Cutting)

**Goal**: billing-service consumes `UserDataErasureEvent` and anonymises payment/subscription records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` → billing user PII fields (name, email, payment token) anonymised in DB; subscription records retained for financial compliance with anonymised user reference.

- [ ] TErasure-006a Add `spring-kafka` to `backend/billing-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-006b Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/kafka/ErasureEventConsumer.java` — anonymise user PII in billing records; retain for financial audit (7-year retention policy)

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T022 [P] Add Prometheus gauge for MRR and subscription counts per plan in `BillingService`
- [ ] T023 [P] Add `UsageTrackingService.java` for recording API call events via RabbitMQ listener
- [ ] T024 Run `quickstart.md` smoke tests (list plans, checkout, webhook, cancel)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T008): Depends on Phase 1
- **Phase 3** (T009–T012): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T013–T018): Depends on Phase 2
- **Phase 5** (T019–T021): Depends on Phase 4
- **Phase 6** (T022–T024): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working plan listing and subscription status.
