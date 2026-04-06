#!/usr/bin/env python3
"""Write all 13 plan.md files for the SaaS Starter Kit features."""
import os

BASE = "/Users/vinodbalasubramanian/Documents/saas_starter_kit/specs"

plans = {
"001-authentication-identity-management": """# Implementation Plan: Authentication & Identity Management

**Branch**: `001-authentication-identity-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-authentication-identity-management/spec.md`

## Summary

Keycloak-backed authentication with local email/password and OAuth2 social login (Google, Apple,
Microsoft). Each organisation gets a dedicated Keycloak client at registration. The auth-service
orchestrates user creation in Keycloak, per-tenant PostgreSQL schema provisioning, and JWT token
issuance. Supports MFA (TOTP), account lockout, email verification, and password reset.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Spring Web, Keycloak Admin Client, JJWT, Spring Data JPA
**Storage**: PostgreSQL 15 (auth schema), Keycloak internal DB, Redis (session cache)
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: North-South web service (REST API)
**Performance Goals**: Login < 200 ms p95; registration < 2 s p95 (including Keycloak + DB)
**Constraints**: Password min 8 chars; configurable account lockout; token validation < 50 ms
**Scale/Scope**: 500 concurrent logins; per-tenant isolation in Keycloak + DB

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Keycloak via KeycloakService abstraction; OAuth via Spring Security OAuth2 client |
| II. Cloud Agnostic | PASS | Identity: Keycloak (local) / Cognito (AWS) / Firebase Auth (GCP) / Azure AD B2C |
| III. SCM/CI Agnostic | PASS | No SCM-specific logic in service code |
| V. Traffic-Aware | PASS | North-South REST + JWT; East-West propagates W3C Trace Context |
| VI. Tenant-Centric | PASS | Dedicated Keycloak client per org; tenant slug in token claims |
| VII. Observability | PASS | OTel instrumented; structured JSON logs with tenantId + correlationId; RED metrics |
| VIII. Security | PASS | JWT validated at Kong only; secrets via SecretsProvider |

## Project Structure

### Documentation (this feature)

```
specs/001-authentication-identity-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/auth-service.yaml
```

### Source Code (repository root)

```
backend/auth-service/
├── src/main/java/com/saasstarterkit/auth/
│   ├── config/           # SecurityConfig, OAuth2Config, KeycloakConfig
│   ├── controller/       # AuthController
│   ├── dto/              # LoginRequest, RegisterRequest, RegisterResponse, TokenResponse
│   ├── entity/           # User, Role
│   ├── exception/        # UserAlreadyExistsException, ValidationException
│   ├── repository/
│   ├── service/          # AuthService, KeycloakService, UserService
│   └── util/
└── src/test/java/

database/init/01-init-schema.sql
contracts/openapi/auth-service.yaml
```

**Structure Decision**: Single Spring Boot service. North-South REST. OpenAPI contract in `contracts/openapi/`.

## Complexity Tracking

> No constitution violations.
""",

"002-multi-tenancy-tenant-lifecycle": """# Implementation Plan: Multi-Tenancy & Tenant Lifecycle Management

**Branch**: `002-multi-tenancy-tenant-lifecycle` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Schema-per-tenant isolation model. Every tenant gets a dedicated PostgreSQL schema
(tenant_{slug}) provisioned via create_tenant_schema() database function at registration.
The user-service exposes tenant lifecycle CRUD scoped to SUPER_ADMIN. X-Tenant-ID context
is injected at Kong and propagated through HTTP interceptor, MDC, and OTel spans.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Data JPA, Spring Web, PostgreSQL JDBC, Liquibase
**Storage**: PostgreSQL 15 (tenant_management schema + dynamic tenant schemas)
**Testing**: JUnit 5, Testcontainers (PostgreSQL)
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: North-South + East-West service
**Performance Goals**: Schema provisioning < 5 s; tenant context resolution < 5 ms overhead
**Constraints**: Zero cross-tenant data leakage; exclusive schema per tenant
**Scale/Scope**: Thousands of tenants; each with dedicated schema

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Tenant DB access via JPA repositories only |
| II. Cloud Agnostic | PASS | PostgreSQL (local/cloud); schema-per-tenant strategy configurable |
| V. Traffic-Aware | PASS | Tenant context resolved North-South at Kong; propagated East-West via headers |
| VI. Tenant-Centric | PASS | This IS the tenant isolation feature; every query scoped to tenant schema |
| VII. Observability | PASS | tenantId in every OTel span attribute and MDC log field |
| VIII. Security | PASS | SUPER_ADMIN role required for global tenant operations |

## Project Structure

### Documentation (this feature)

```
specs/002-multi-tenancy-tenant-lifecycle/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/tenant-service.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   ├── controller/       # TenantController
│   ├── entity/           # Tenant, TenantConfiguration, TenantUser
│   ├── repository/
│   ├── service/          # TenantService, TenantUserService
│   └── config/           # TenantContext (ThreadLocal), TenantInterceptor

database/init/02-multi-tenant-schema.sql
database/migrations/04-create-tenant-configurations.sql
contracts/openapi/tenant-service.yaml
```

**Structure Decision**: Tenant lifecycle in user-service. DB function provisions schemas dynamically.

## Complexity Tracking

> No constitution violations.
""",

"003-user-management": """# Implementation Plan: User Management

**Branch**: `003-user-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Full user lifecycle management within a tenant. Admin CRUD via /api/admin/users; user-facing
profile management via /api/users. Users stored in both Keycloak (identity) and per-tenant
PostgreSQL schemas (profile/preferences). Dynamic user profile schemas with immutable versioning
(draft -> published -> deprecated) allow organisations to define custom user attributes rendered
dynamically in the frontend.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x; TypeScript React 18 (admin UI)
**Primary Dependencies**: Spring Data JPA, Spring Security, Spring Web, Keycloak Admin Client
**Storage**: PostgreSQL 15 (tenant_{slug}.users, OrgSchema, UserProfileValues as JSONB)
**Testing**: JUnit 5, Testcontainers, React Testing Library
**Target Platform**: Linux server + browser
**Project Type**: North-South REST API + React SPA admin panel
**Performance Goals**: User list < 200 ms for 10 000 users; profile update immediately consistent
**Constraints**: Tenant-scoped queries only; schema version immutable once published
**Scale/Scope**: 10 000+ users per tenant

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Keycloak user ops via KeycloakService; no Admin Client in controllers |
| II. Cloud Agnostic | PASS | Identity via identity.interface; storage via JPA |
| V. Traffic-Aware | PASS | North-South REST for CRUD; OTel tracing per user operation |
| VI. Tenant-Centric | PASS | All queries tenant-scoped; dynamic schemas per org (NON-NEGOTIABLE in constitution) |
| VII. Observability | PASS | LoggingService + TracingService wired into UserController |
| VIII. Security | PASS | @PreAuthorize(hasRole(ADMIN)) on all admin endpoints |

## Project Structure

### Documentation (this feature)

```
specs/003-user-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/user-service.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   ├── controller/       # AdminController, UserController
│   ├── domain/           # OrgSchema, UserProfileValues
│   ├── dto/              # AdminUserDTO, UserProfileDTO, CreateUserRequest, UpdateUserRequest
│   ├── entity/           # User, TenantUser, UserRole, Role
│   ├── repository/
│   ├── service/          # UserService, AdminService, LoggingService, TracingService
│   └── security/

frontend/src/pages/admin/UserManagement.tsx
contracts/openapi/user-service.yaml
```

**Structure Decision**: user-service owns user lifecycle. Dynamic schema system adds OrgSchema entity.

## Complexity Tracking

> No constitution violations.
""",

"004-organization-management": """# Implementation Plan: Organisation Management

**Branch**: `004-organization-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Within each tenant, users create and manage organisations (teams/departments). A separate
organisation-service handles enterprise governance: tiers, usage limits, compliance settings,
onboarding tracking, and comprehensive audit logging (org-level + member-level). All org
operations require valid tenant context. Soft-delete throughout.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Data JPA, Spring Web, Spring Security
**Storage**: PostgreSQL 15 (tenant_{slug}.organizations, organizational_management schema)
**Testing**: JUnit 5, Testcontainers
**Target Platform**: Linux server
**Project Type**: North-South + East-West service
**Performance Goals**: Org list < 150 ms; audit log writes async (< 5 ms added latency)
**Constraints**: Soft-delete enforced; tenant context required for every request
**Scale/Scope**: Hundreds of orgs per tenant; thousands of members

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Audit emit via async service; no direct DB writes in controllers |
| II. Cloud Agnostic | PASS | JPA; no cloud-specific storage |
| V. Traffic-Aware | PASS | North-South REST; audit events async East-West |
| VI. Tenant-Centric | PASS | TenantContext required for every org operation |
| VII. Observability | PASS | Audit logs with risk_level scoring |
| VIII. Security | PASS | Audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/004-organization-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/organization-service.yaml
```

### Source Code (repository root)

```
backend/user-service/src/main/java/com/saasstarterkit/user/
├── controller/       # OrganizationController
├── entity/           # TenantOrganization, OrganizationMember
└── service/          # OrganizationService

backend/organisation-service/
└── src/main/           # Enterprise governance service

database/init/03-organizational-management-schema.sql
contracts/openapi/organization-service.yaml
```

**Structure Decision**: Basic org CRUD in user-service; enterprise governance in organisation-service.

## Complexity Tracking

> No constitution violations.
""",

"005-role-based-access-control": """# Implementation Plan: Role-Based Access Control (RBAC)

**Branch**: `005-role-based-access-control` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Granular RBAC backed by Keycloak roles and local role-permission mappings (resource:action).
Spring Security @PreAuthorize annotations enforce role checks at every controller method.
OPA policies govern East-West service authorisation. Permissions cached via Spring Cache.
Role changes take effect on next token refresh.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Keycloak Spring Boot Adapter, OPA (East-West)
**Storage**: PostgreSQL 15 (auth.roles, auth.permissions, auth.user_roles, auth.role_permissions)
**Testing**: JUnit 5, Spring Security Test, MockMvc
**Target Platform**: Linux server
**Project Type**: Cross-cutting concern (all backend services)
**Performance Goals**: Permission check < 5 ms via Spring Cache
**Constraints**: Role changes reflected on next token refresh only; OPA deny-all by default
**Scale/Scope**: 5+ default roles; 50+ permissions; cached per request

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | @PreAuthorize declarative; OPA as policy engine interface |
| V. Traffic-Aware | PASS | JWT role claims validated North-South at Kong; OPA East-West |
| VI. Tenant-Centric | PASS | Roles are tenant-scoped in Keycloak realms |
| VII. Observability | PASS | Authorization failures logged with tenantId + userId |
| VIII. Security | PASS | OPA deny-all default; explicit allow per service pair required |

## Project Structure

### Documentation (this feature)

```
specs/005-role-based-access-control/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/rbac-events.yaml
```

### Source Code (repository root)

```
backend/auth-service/src/main/java/com/saasstarterkit/auth/
├── entity/           # Role
├── config/           # SecurityConfig
└── repository/       # RoleRepository

backend/user-service/src/main/java/com/saasstarterkit/user/
├── controller/       # AdminController (role endpoints)
└── service/          # AdminService (role management)

database/init/01-init-schema.sql   # auth.roles, auth.permissions, auth.role_permissions
```

**Structure Decision**: Role data in auth-service; admin role assignment ops in user-service.

## Complexity Tracking

> No constitution violations.
""",

"006-subscription-billing-management": """# Implementation Plan: Subscription & Billing Management

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
""",

"007-notification-service": """# Implementation Plan: Notification Service

**Branch**: `007-notification-service` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Multi-channel notification delivery backed by RabbitMQ. The service consumes NotificationEvent
from notification.queue, resolves user preferences, renders Mustache templates, and delivers
via email (SendGrid/Mailgun/SES), SMS (Twilio/Plivo), push (FCM), in-app, and webhook channels.
All outbound email goes exclusively through the EmailProvider interface (no direct SMTP).

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring AMQP (RabbitMQ), Spring Web, Mustache, Spring Mail, Firebase Admin SDK
**Storage**: PostgreSQL 15 (notifications, notification_templates, user_notification_preferences)
**Testing**: JUnit 5, Testcontainers (RabbitMQ, PostgreSQL), MockMvc
**Target Platform**: Linux server
**Project Type**: East-West async consumer + North-South webhook delivery
**Performance Goals**: Email delivery < 5 s p95; 1 000 events/minute throughput
**Constraints**: At-least-once delivery; all providers swappable via config only
**Scale/Scope**: All tenants; 5 channels; template-driven

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | NotificationChannelProvider + EmailProvider interfaces; all channel impls pluggable |
| II. Cloud Agnostic | PASS | Queue: RabbitMQ (local) / SQS (AWS) / Pub/Sub (GCP); email/SMS providers swappable |
| V. Traffic-Aware | PASS | Event consumption East-West (RabbitMQ); webhook delivery North-South outbound |
| VI. Tenant-Centric | PASS | Every notification carries tenantId; preferences per tenant-user |
| VII. Observability | PASS | Delivery result logged per channel; RED metrics on queue consumer |
| VIII. Security | PASS | Webhook retried with backoff; all outbound email via EmailProvider interface only |

## Project Structure

### Documentation (this feature)

```
specs/007-notification-service/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/notification-events.yaml
```

### Source Code (repository root)

```
backend/notification-service/
├── src/main/java/com/saasstarterkit/notification/
│   ├── domain/enums/     # NotificationChannel, NotificationPriority
│   ├── dto/              # NotificationEvent
│   ├── entity/           # Notification, NotificationTemplate, UserNotificationPreferences
│   ├── repository/
│   └── service/
│       ├── NotificationService.java
│       ├── TemplatingService.java
│       ├── NotificationTemplateService.java
│       ├── UserPreferencesService.java
│       ├── ChannelProviderRegistry.java
│       └── channel/      # EmailChannelProvider, SmsChannelProvider, PushChannelProvider,
│                           InAppChannelProvider, WebhookChannelProvider

frontend/src/pages/admin/NotificationManagement.tsx
contracts/asyncapi/notification-events.yaml
```

**Structure Decision**: Standalone async consumer service. AsyncAPI contract for event schema.

## Complexity Tracking

> No constitution violations.
""",

"008-analytics-observability": """# Implementation Plan: Analytics & Observability

**Branch**: `008-analytics-observability` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

OTel is the single observability standard. Every service instruments traces, metrics, and structured
JSON logs. Prometheus scrapes RED metrics; Grafana provides dashboards; Alertmanager routes by
severity and environment. ELK Stack provides centralised log aggregation with tenant-scoped indices.
Analytics service captures UserActivity and TenantMetrics for usage and billing inputs.

## Technical Context

**Language/Version**: Java 17 (analytics-service); YAML/config (infrastructure components)
**Primary Dependencies**: OTel SDK (Java), Micrometer (Prometheus), Logstash, Elasticsearch, Spring Data JPA
**Storage**: PostgreSQL (analytics), Elasticsearch (logs), Prometheus TSDB (metrics)
**Testing**: JUnit 5, OTel testing SDK, Testcontainers
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: Cross-cutting observability concern + analytics microservice
**Performance Goals**: Trace overhead < 2 ms; log ingestion > 10 000 events/sec
**Constraints**: OTel backend switchable via config/observability.yaml without code change
**Scale/Scope**: All services + all tenants; 5% sampling in production

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | OTel SDK abstracts tracing backend; Micrometer abstracts metrics backend |
| II. Cloud Agnostic | PASS | Backend: Jaeger (local) / X-Ray (AWS) / Cloud Trace (GCP) / Azure Monitor |
| V. Traffic-Aware | PASS | Trace root created at Kong (North-South); W3C Trace Context propagated East-West |
| VI. Tenant-Centric | PASS | tenantId in every span attribute, log field, Elasticsearch index |
| VII. Observability | PASS | This IS the observability feature |
| VIII. Security | PASS | PII masked in non-production; audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/008-analytics-observability/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/analytics-events.yaml
```

### Source Code (repository root)

```
backend/analytics-service/
├── src/main/java/com/saasstarterkit/analytics/
│   ├── entity/           # UserActivity, TenantMetrics
│   └── repository/

infrastructure/
├── prometheus/prometheus.yml
├── alertmanager/alertmanager.yml
├── otel-collector/otel-collector-config.yaml
├── kibana/tenant-rbac-config.yml
└── logstash/

frontend/src/pages/admin/
├── MonitoringDashboard.tsx
└── LoggingDashboard.tsx

config/observability.yaml
```

**Structure Decision**: analytics-service for business metrics; infrastructure/ for observability stack.

## Complexity Tracking

> No constitution violations.
""",

"009-security-compliance": """# Implementation Plan: Security & Compliance

**Branch**: `009-security-compliance` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

GDPR consent management (12 consent types), data subject rights (16 request types), HIPAA PHI
protection with AES-256 encryption and SHA-256 hash verification, security event detection with
risk scoring, and compliance framework dashboards (GDPR/HIPAA/SOX/PCI-DSS). UserDataErasureEvent
broadcast via Kafka triggers purge handlers in all services. Secrets via SecretsProvider.
Audit logs immutable in production.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Javax Crypto (AES-256), Spring Data JPA, Kafka (erasure events)
**Storage**: PostgreSQL (data_consents, data_subject_requests, protected_health_information)
**Testing**: JUnit 5, Testcontainers (Kafka, PostgreSQL)
**Target Platform**: Linux server
**Project Type**: North-South + East-West service (compliance + security events)
**Performance Goals**: Erasure requests complete within 30-day GDPR legal window
**Constraints**: PHI decryption requires access purpose logging; audit logs write-only in prod
**Scale/Scope**: All tenants; compliance framework selectable per tenant

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | SecretsProvider for encryption keys; no hardcoded secrets anywhere |
| II. Cloud Agnostic | PASS | Vault (local), Secrets Manager (AWS), Key Vault (Azure) via SecretsProvider |
| V. Traffic-Aware | PASS | Consent API North-South; UserDataErasureEvent East-West Kafka |
| VI. Tenant-Centric | PASS | Every consent/PHI record carries tenant_id; compliance selectable per tenant |
| VII. Observability | PASS | Audit logs are immutable compliance observability; security alerts with risk_score |
| VIII. Security | PASS | AES-256 encryption; SAST/DAST in CI; audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/009-security-compliance/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/compliance-events.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   └── temp-backup/      # ComplianceController.java, ComplianceService.java (to be promoted)

database/migrations/05-gdpr-hipaa-compliance.sql

frontend/src/pages/admin/SecurityCompliance.tsx
contracts/asyncapi/compliance-events.yaml   # UserDataErasureEvent AsyncAPI schema
```

**Structure Decision**: Compliance logic promoted into user-service. Erasure event is East-West Kafka.

## Complexity Tracking

> No constitution violations.
""",

"010-api-gateway": """# Implementation Plan: API Gateway (Kong)

**Branch**: `010-api-gateway` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Kong is the single North-South ingress for all 13 features. It validates JWTs (backends never
re-validate), injects tenant headers (X-Tenant-ID, X-Organization-ID, X-User-ID), enforces
per-tenant/per-plan rate limiting, handles CORS, and applies WAF rules from staging upward.
Config is declarative YAML, version-controlled, and hot-reloadable.

## Technical Context

**Language/Version**: Kong 3.x (declarative YAML); Bash (deploy scripts)
**Primary Dependencies**: Kong JWT plugin, Rate Limiting, Request Transformer, CORS, OTel plugin
**Storage**: Kong DB-less (YAML) or Kong DB (PostgreSQL)
**Testing**: Kong integration tests, Postman/Newman collections, test-multi-tenant.sh
**Target Platform**: Linux server (Docker / Kubernetes ingress)
**Project Type**: North-South API Gateway
**Performance Goals**: < 5 ms latency overhead p99; > 10 000 req/s in production
**Constraints**: Config hot-reload via Admin API; no service restart on config change
**Scale/Scope**: All 13 features' endpoints; multi-tenant; 7 environments

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Kong is the interface-first North-South boundary; all JWT validation happens here |
| II. Cloud Agnostic | PASS | Kong runs on any Kubernetes (EKS/GKE/AKS/kind) |
| V. Traffic-Aware | PASS | This IS the North-South boundary; TLS termination here |
| VI. Tenant-Centric | PASS | X-Tenant-ID injected at Kong; all downstream services receive it |
| VII. Observability | PASS | OTel trace root created at Kong; distributed trace spans initiated here |
| VIII. Security | PASS | TLS termination; JWT validation; WAF from staging up; DDoS from sandbox up |

## Project Structure

### Documentation (this feature)

```
specs/010-api-gateway/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/kong-admin.yaml
```

### Source Code (repository root)

```
api-gateway/
├── kong.yml
├── kong-multi-tenant.yml
├── kong-multi-tenant-enhanced.yml
├── kong-organisation.yml
├── kong-organizational-management.yml
├── deploy-multi-tenant.sh
└── test-multi-tenant.sh

frontend/src/pages/admin/ApiGatewayManagement.tsx
```

**Structure Decision**: All Kong config in api-gateway/. Kong is the only North-South boundary.

## Complexity Tracking

> No constitution violations.
""",

"011-tenant-configuration-management": """# Implementation Plan: Tenant Configuration Management

**Branch**: `011-tenant-configuration-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Hierarchical key-value config store with three tiers: tenant-specific -> environment-specific
global -> global default. Sensitive values encrypted with AES-256 via SecretsProvider.
Spring Cache (Redis) accelerates reads. Type-safe retrieval for String, Integer, Boolean,
Long, Double, and JSONB. Config scoped per environment (dev, staging, production, default).

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Data JPA, Spring Cache (Redis), Javax Crypto (AES-256)
**Storage**: PostgreSQL 15 (tenant_configurations), Redis (config cache)
**Testing**: JUnit 5, Testcontainers (PostgreSQL + Redis)
**Target Platform**: Linux server
**Project Type**: East-West internal service
**Performance Goals**: Cached reads < 5 ms; cache miss < 50 ms; encryption overhead < 10 ms
**Constraints**: Encryption key from SecretsProvider only; cache invalidated on update
**Scale/Scope**: Hundreds of config keys per tenant; thousands of tenants

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Encryption key from SecretsProvider; no hardcoded secrets |
| II. Cloud Agnostic | PASS | JPA + Redis (cloud-agnostic); secrets via Vault/cloud-native SecretsProvider |
| V. Traffic-Aware | PASS | Config service East-West only; not externally exposed |
| VI. Tenant-Centric | PASS | Every config row scoped to tenant_id |
| VII. Observability | PASS | Cache misses and fallback chain logged; warnings on key-not-found |
| VIII. Security | PASS | AES-256 encryption at rest; encryption key via SecretsProvider only |

## Project Structure

### Documentation (this feature)

```
specs/011-tenant-configuration-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
└── quickstart.md
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   ├── controller/       # TenantConfigurationController
│   ├── entity/           # TenantConfiguration
│   ├── repository/       # TenantConfigurationRepository
│   └── service/          # TenantConfigurationService

database/migrations/04-create-tenant-configurations.sql
frontend/src/pages/admin/SystemConfiguration.tsx
```

**Structure Decision**: Config management in user-service alongside tenant lifecycle.

## Complexity Tracking

> No constitution violations.
""",

"012-frontend-application": """# Implementation Plan: Frontend Application

**Branch**: `012-frontend-application` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

React 18 + TypeScript SPA with route-guarded public/auth/protected/admin areas. AuthContext
manages session in localStorage. Admin panel covers all 13 feature management pages (lazy-loaded).
React Query handles caching; Tailwind CSS for styling; React Hook Form for forms; typed Axios
service layer for all API calls. OTel browser SDK and Sentry for frontend observability.

## Technical Context

**Language/Version**: TypeScript 5.x, React 18, Node 20
**Primary Dependencies**: React Router 6, @tanstack/react-query, Axios, React Hook Form, Tailwind CSS, @heroicons/react, react-hot-toast
**Storage**: localStorage (session only)
**Testing**: Jest, React Testing Library, MSW (API mocking)
**Target Platform**: Browser (Chrome/Firefox/Safari/Edge latest 2)
**Project Type**: React SPA (North-South API consumer)
**Performance Goals**: LCP < 3 s on 3G; admin pages lazy-loaded (code-split)
**Constraints**: Auth state persisted on refresh; role-based route guards enforced
**Scale/Scope**: 12 admin pages; 3 auth pages; 4 user pages; 1 landing page

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | All API calls via typed services/api.ts; no direct fetch in components |
| V. Traffic-Aware | PASS | SPA is the North-South consumer of Kong-fronted REST APIs |
| VI. Tenant-Centric | PASS | X-Tenant-ID injected in Axios request interceptor for all authenticated requests |
| VII. Observability | PASS | OTel browser SDK; Sentry for error tracking |
| VIII. Security | PASS | JWT handled per browser security best practice; React XSS defaults |

## Project Structure

### Documentation (this feature)

```
specs/012-frontend-application/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/   # Generated TypeScript types from consumed API contracts
```

### Source Code (repository root)

```
frontend/
├── src/
│   ├── App.tsx
│   ├── contexts/
│   │   ├── AuthContext.tsx
│   │   └── NotificationContext.tsx
│   ├── components/
│   │   ├── auth/
│   │   └── layout/           # Layout, AuthLayout
│   ├── pages/
│   │   ├── LandingPage.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Billing.tsx
│   │   ├── auth/             # Login, Register, ForgotPassword
│   │   └── admin/            # 12 admin management pages (lazy)
│   └── services/
│       └── api.ts
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── jest.config.js
```

**Structure Decision**: Standard React SPA. Admin pages code-split. services/api.ts is the single API boundary.

## Complexity Tracking

> No constitution violations.
""",

"013-infrastructure-deployment": """# Implementation Plan: Infrastructure & Deployment

**Branch**: `013-infrastructure-deployment` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Cloud-agnostic IaC using Terraform modules per provider (AWS/GCP/Azure/local). Kubernetes
manifests use Kustomize base + 7 environment overlays. Pipeline logic in Dagger (TypeScript);
SCM wrappers are thin shells that only invoke Dagger. Secrets via SecretsProvider interface
(Vault locally, cloud-native in cloud). GitOps via ArgoCD. Scale-to-zero for dev/sandbox/staging/UAT.

## Technical Context

**Language/Version**: Terraform 1.x, Kubernetes 1.29, Dagger (TypeScript 5.x), Bash
**Primary Dependencies**: Terraform AWS/GCP/Azure providers, Kustomize, ArgoCD, HashiCorp Vault, Unleash
**Storage**: Terraform state backend (S3/GCS/Azure Blob/local), Vault secrets
**Testing**: Terratest, Dagger pipeline unit tests, k8s conformance
**Target Platform**: AWS / GCP / Azure / local (kind/minikube)
**Project Type**: Infrastructure as Code + CI/CD pipeline
**Performance Goals**: Local cold start < 3 min; CI pipeline < 10 min; cloud deploy < 15 min
**Constraints**: Zero code changes to switch provider; secrets never hardcoded or committed
**Scale/Scope**: 7 environments; 13 microservices; multi-cloud

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | SecretsProvider interface; Terraform provider modules abstract cloud APIs |
| II. Cloud Agnostic | PASS | This IS the cloud-agnostic infrastructure feature |
| III. SCM/CI Agnostic | PASS | Dagger pipeline; SCM wrappers in .scm/ contain only Dagger invocation |
| IV. Seven-Environment Parity | PASS | Kustomize overlays for all 7 environments |
| V. Traffic-Aware | PASS | Public subnets: Kong/LB only; private subnets: all services/DBs |
| VII. Observability | PASS | OTel collector, Prometheus, Grafana, ELK in local and all cloud stacks |
| VIII. Security | PASS | mTLS from dev upward; Vault for secrets; SAST/DAST in CI pipeline |

## Project Structure

### Documentation (this feature)

```
specs/013-infrastructure-deployment/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
└── quickstart.md
```

### Source Code (repository root)

```
docker-compose.yml                            # Local full stack (existing)
infrastructure/
├── iam/realm-export.json
├── prometheus/prometheus.yml
├── alertmanager/alertmanager.yml
├── otel-collector/otel-collector-config.yaml
├── kibana/tenant-rbac-config.yml
├── logstash/
├── terraform/                                  [TO BE CREATED]
│   ├── modules/
│   └── providers/{aws,gcp,azure,local}/
└── kubernetes/                                 [TO BE CREATED]
    ├── base/
    └── overlays/{local,dev,sandbox,staging,uat,pre-prod,prod}/

pipeline/                                       [TO BE CREATED]
└── src/                # Dagger TypeScript pipeline

.scm/                                           [TO BE CREATED]
├── github/workflows/
├── gitlab/.gitlab-ci.yml
└── ...

config/
├── providers.yaml      # Active cloud provider
├── scm.yaml            # Active SCM
└── observability.yaml  # OTel backend per env

Makefile                                        [TO BE CREATED]
```

**Structure Decision**: IaC in infrastructure/; pipeline in pipeline/; provider config in config/.

## Complexity Tracking

> No constitution violations.
"""
}

written = 0
for feature, content in plans.items():
    path = os.path.join(BASE, feature, "plan.md")
    with open(path, 'w') as f:
        f.write(content.strip() + "\n")
    print(f"OK: {feature}/plan.md")
    written += 1

print(f"\nTotal written: {written}")
