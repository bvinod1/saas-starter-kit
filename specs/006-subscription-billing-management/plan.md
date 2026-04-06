# Implementation Plan: Subscription & Billing Management

**Branch**: `006-subscription-billing-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Provider-agnostic subscription management via ISubscriptionProvider interface. Default
implementation is Zoho Billing. Plans, subscriptions, checkout sessions, invoices, usage
tracking, and webhook events are first-class entities. All service methods are async
(CompletableFuture). Frontend billing page shows plan, usage metrics, and invoices.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x; TypeScript React 18 (billing page)
**Primary Dependencies**: Spring async (CompletableFuture), Spring Data JPA, Zoho Billing HTTP client
**Storage**: PostgreSQL 15 (plans, subscriptions, checkout_sessions, usage_tracking, webhook_events)
**Testing**: JUnit 5, Mockito, WireMock (Zoho API mocking), Testcontainers
**Target Platform**: Linux server
**Project Type**: North-South web service + async webhook consumer
**Performance Goals**: Checkout creation < 1 s p95; webhook processing idempotent
**Constraints**: Provider swap requires only config change; webhook idempotency mandatory
**Scale/Scope**: Thousands of tenants; multiple billing cycles; multi-currency

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | ISubscriptionProvider interface; ZohoBillingProvider is sole concrete impl |
| II. Cloud Agnostic | PASS | Stripe/Paddle/Zoho all swappable via config; MeteringProvider interface |
| V. Traffic-Aware | PASS | North-South REST + webhook receiver; MeteringProvider East-West |
| VI. Tenant-Centric | PASS | Every subscription record carries tenant_id; scoped access enforced |
| VII. Observability | PASS | CompletableFuture spans traced with OTel; sync_status tracked |
| VIII. Security | PASS | Webhook signature verification required; ADMIN or tenant-owner authorization |

## Project Structure

### Documentation (this feature)

```
specs/006-subscription-billing-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/billing-service.yaml
```

### Source Code (repository root)

```
backend/billing-service/
├── src/main/java/com/saasstarterkit/billing/
│   ├── adapters/zoho/    # ZohoBillingProvider implements ISubscriptionProvider
│   ├── controller/       # SubscriptionController, WebhookController
│   ├── domain/
│   │   ├── interfaces/   # ISubscriptionProvider
│   │   └── model/        # Plan, Subscription, CheckoutSession, SubscriptionStatus, BillingCycle
│   ├── repository/
│   └── service/          # SubscriptionService

frontend/src/pages/Billing.tsx
database/migrations/04-subscription-management.sql
contracts/openapi/billing-service.yaml
```

**Structure Decision**: Standalone billing-service. Provider adapter pattern enforced.

## Complexity Tracking

> No constitution violations.
