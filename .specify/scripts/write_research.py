#!/usr/bin/env python3
"""Write all research.md, data-model.md, quickstart.md, and contracts files for 13 features."""
import os

BASE = "/Users/vinodbalasubramanian/Documents/saas_starter_kit/specs"

# ============================================================
# RESEARCH.md FILES
# ============================================================

research = {}

research["001-authentication-identity-management"] = """# Research: Authentication & Identity Management

## Decision: Identity Provider — Keycloak

**Decision**: Use Keycloak as the local identity provider, mapped to cloud-native providers per environment.
**Rationale**: Keycloak is fully open-source, runs offline, supports OAuth2/OIDC/SAML, provides Admin REST API for programmatic user/client management, and maps cleanly to Cognito (AWS), Firebase Auth (GCP), and Azure AD B2C.
**Alternatives considered**:
- Auth0: SaaS-only, not cloud-agnostic, no offline mode
- Custom JWT: No SSO, significant re-implementation surface

## Decision: Client-per-Organisation Model

**Decision**: Each new organisation registration creates a dedicated Keycloak OAuth2 client (client_id = org slug).
**Rationale**: Provides hard isolation of tokens per organisation; token scope cannot bleed across orgs; enables per-org OAuth2 callback URLs; allows organisation-specific SSO configuration later.
**Alternatives considered**:
- Single client with custom claims: Cross-tenant token reuse risk; harder to revoke per org

## Decision: JWT Access + Refresh Tokens

**Decision**: Keycloak issues short-lived access tokens (15 min default) and longer-lived refresh tokens.
**Rationale**: Standard OAuth2 OIDC pattern; access token validated at Kong; does NOT re-validate downstream (reduces latency); refresh rotates silently via /api/auth/refresh.
**Alternatives considered**:
- Session cookies: Incompatible with microservices and mobile clients

## Decision: MFA via TOTP (Time-based One-Time Passwords)

**Decision**: TOTP-based MFA stored in Keycloak (mfa_secret per user).
**Rationale**: TOTP is widely supported (Google Authenticator, Authy), offline-capable, and built into Keycloak's Required Actions.
**Alternatives considered**:
- SMS OTP: Phishable, carrier-dependent, costs money per message

## Decision: Password Reset via Email Token

**Decision**: Server generates a time-limited token, emails it, and validates on submission.
**Rationale**: Industry standard; Keycloak's built-in Forgot Password flow handles this.
**Alternatives considered**:
- Security questions: Deprecated security practice

## Best Practices Confirmed

- JWT signed with RS256 (asymmetric); public key distributed via JWKS endpoint
- Account lockout: exponential backoff after N failed attempts (configurable per tenant)
- Email verification token: UUID v4, expires in 24 hours
- Password reset token: UUID v4, expires in 1 hour (shorter)
- Refresh tokens must be invalidated on logout (Keycloak session revocation)
"""

research["002-multi-tenancy-tenant-lifecycle"] = """# Research: Multi-Tenancy & Tenant Lifecycle Management

## Decision: Schema-per-Tenant Isolation

**Decision**: Each tenant gets a dedicated PostgreSQL schema (tenant_{slug}).
**Rationale**: Strong data isolation without separate DB instances. Schema-level pg_dump for tenant backup. Natural row filtering without additional WHERE clauses. Connection pooling still works (pgBouncer can route via search_path). Simpler GDPR erasure (drop tenant schema).
**Alternatives considered**:
- DB-per-tenant: Operationally prohibitive at scale (connection limits, $Cost, migration overhead)
- Row-level security: More complex, risk of misconfigured RLS policies leaking data

## Decision: Dynamic Schema Provisioning via DB Function

**Decision**: create_tenant_schema(slug) stored procedure creates all required tables in one transaction.
**Rationale**: Atomic provisioning; easier to version and test; callable from Java via JDBC.
**Alternatives considered**:
- Liquibase per-tenant: Increases migration complexity exponentially with tenant count

## Decision: Tenant Context via X-Tenant-ID Header

**Decision**: Tenant identified by X-Tenant-ID header injected at Kong, propagated via ThreadLocal (TenantContext) and MDC.
**Rationale**: Explicit context propagation; no ambiguity; works for REST, WebSocket, and async jobs.
**Alternatives considered**:
- Subdomain routing: More complex DNS management; doesn't work in all deployment contexts
- JWT claim only: Requires token validation before any tenant query; adds latency

## Decision: master Registry in tenant_management Schema

**Decision**: Shared tenant_management schema holds tenant master registry, schema registry, and cross-tenant user registry.
**Rationale**: SUPER_ADMIN needs cross-tenant visibility; billing needs tenant->plan mapping; health checks need tenant status.

## Best Practices Confirmed

- Tenant slug: lowercase, hyphen-separated, UUID suffix for uniqueness (max 50 chars)
- set_config('app.current_tenant', tenant_id, false) in PostgreSQL for RLS support
- TenantContext.getCurrentTenant() always checked before any DB query; returns 400 if missing
- search_path set per connection to tenant schema; reset on connection return to pool
- OTel span attribute: tenant.id attached to every span
"""

research["003-user-management"] = """# Research: User Management

## Decision: Dual Storage — Keycloak + PostgreSQL

**Decision**: User identity (password hash, OAuth links, MFA) stored in Keycloak; user profile (avatar, bio, preferences, timezone) stored in per-tenant PostgreSQL.
**Rationale**: Clean separation of concerns. Keycloak owns authentication; app DB owns profile. Avoids coupling auth provider to profile schema changes.
**Alternatives considered**:
- Profile in Keycloak attributes: Limited query capabilities; Keycloak not designed for complex profile queries

## Decision: Dynamic Profile Schemas with Immutable Versioning

**Decision**: OrgSchema entity stores field definitions as JSONB; UserProfileValues stores values as JSONB keyed to schema version. Schema lifecycle: draft → published → deprecated. Published versions are immutable.
**Rationale**: Constitution VI is NON-NEGOTIABLE for dynamic user profile schemas. Organisations must define custom attributes without code deployments. Historical data preserved across schema versions.
**Alternatives considered**:
- Hard-coded profile columns: Violates constitution NON-NEGOTIABLE requirement
- EAV (Entity-Attribute-Value): Worse query performance; harder to type-check

## Decision: OTel + MDC Tracing Per User Operation

**Decision**: UserController wraps every operation in TracingService.executeWithSpan(); LoggingService sets MDC context (tenantId, userId, correlationId).
**Rationale**: Constitution VII requires per-operation observability; correlationId enables request tracing across logs and traces.

## Decision: Activate/Deactivate Without Deletion

**Decision**: User status field (active/inactive) controls access without data deletion.
**Rationale**: GDPR right to erasure is separate from deactivation. Account reactivation is possible. Audit trail preserved.

## Best Practices Confirmed

- createUser() must be transactional: Keycloak user created first; PostgreSQL user record second; rollback Keycloak on DB failure
- Profile JSONB indexed with GIN index for fast attribute lookup
- Schema version embedded in UserProfileValues record; frontend fetches active schema to render form
- Bulk backfill procedure required when new required fields added to a schema version
"""

research["004-organization-management"] = """# Research: Organisation Management

## Decision: Split Responsibility — user-service + organisation-service

**Decision**: Basic org CRUD (create/list/get/update/delete) in user-service; enterprise governance (tiers, limits, compliance, onboarding) in organisation-service.
**Rationale**: Keeps user-service focused on user lifecycle. organisation-service handles complex governance without polluting basic CRUD. Clear service boundary.
**Alternatives considered**:
- Single service: Becomes too large; harder to scale governance features independently

## Decision: Soft-Delete for Orgs and Members

**Decision**: deleted_at timestamp; all default queries filter WHERE deleted_at IS NULL. Unique constraint uses partial index (WHERE deleted_at IS NULL).
**Rationale**: Audit trail preserved. Compliance requirement: deleted orgs may need to be referenced in financial records. GDPR erasure is a separate operation.

## Decision: Async Audit Logging

**Decision**: Org and member operations emit audit events asynchronously (Spring @Async or message queue). Audit records written by separate audit writer thread.
**Rationale**: Audit log write failure must NOT fail the business operation. Async ensures < 5 ms added latency per org operation.

## Decision: Risk Level Scoring on Audit Events

**Decision**: Every audit entry includes risk_level (LOW/MEDIUM/HIGH/CRITICAL) and compliance_category.
**Rationale**: Enables security dashboards to aggregate risk; supports compliance reporting per framework.

## Best Practices Confirmed

- Org tiers (STARTER/PROFESSIONAL/ENTERPRISE) stored on org record; limits enforced in service layer
- max_users enforced before member addition (current_users counter)
- Two-phase invitation: invited_at recorded on invite; accepted_at on acceptance
- Onboarding tracked as (onboarding_completed bool + onboarding_step string)
"""

research["005-role-based-access-control"] = """# Research: Role-Based Access Control

## Decision: Keycloak + Local RBAC Mirror

**Decision**: Roles defined in Keycloak realms AND mirrored in local auth.roles/auth.permissions tables. Token carries role claims from Keycloak; local tables used for fine-grained permission checks.
**Rationale**: Keycloak handles SSO role propagation across services; local permission table enables resource:action granularity without custom Keycloak attribute configuration.

## Decision: @PreAuthorize for North-South; OPA for East-West

**Decision**: Spring Security @PreAuthorize enforces role checks at controller layer (North-South). OPA policies enforce service-to-service authorisation (East-West).
**Rationale**: Clean separation of auth concerns per traffic class. @PreAuthorize is declarative and testable. OPA provides deny-all default with explicit allow-list.

## Decision: Spring Cache for Permission Checks

**Decision**: Permission lookups cached with key = userId + resource + action. Cache evicted on role change.
**Rationale**: Permission check is hot path; must add < 5 ms overhead. DB lookup on every request is unacceptable at scale.

## Best Practices Confirmed

- Minimum role set: SUPER_ADMIN, ADMIN, TENANT_ADMIN, USER, VIEWER, GUEST
- Role hierarchy: SUPER_ADMIN > ADMIN > TENANT_ADMIN > USER > VIEWER > GUEST
- Permissions follow resource:action naming: users:read, users:write, billing:read, billing:write, admin:all
- Token role claims updated on next refresh; clients must refresh tokens after role changes
- OPA bundle deployed as sidecar or standalone policy engine; policies loaded from git
"""

research["006-subscription-billing-management"] = """# Research: Subscription & Billing Management

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
"""

research["007-notification-service"] = """# Research: Notification Service

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
"""

research["008-analytics-observability"] = """# Research: Analytics & Observability

## Decision: OpenTelemetry as Single Observability Standard

**Decision**: All services use OTel SDK for traces, metrics, and logs. Backend (Jaeger/Prometheus/Elasticsearch) configurable per environment via config/observability.yaml.
**Rationale**: Constitution VII is NON-NEGOTIABLE. OTel is the CNCF standard; vendor-neutral; backend-agnostic. One instrumentation, swap exporters per environment.

## Decision: Tenant ID as OTel Span Attribute

**Decision**: tenant.id added as baggage + span attribute on every OTel span. Propagated via W3C Trace Context + W3C Baggage headers East-West.
**Rationale**: Enables tenant-scoped trace queries in Jaeger/Grafana Tempo. Supports cross-service debugging within a tenant's trace.

## Decision: Elasticsearch Tenant-Scoped Index Pattern

**Decision**: Log index names follow pattern: logs-{tenantId}-{YYYY-MM}. Kibana RBAC configured per tenant.
**Rationale**: Complete log isolation per tenant. Kibana tenant RBAC prevents cross-tenant log access. ILM (Index Lifecycle Management) per tenant index.

## Decision: Per-Environment Sampling Rates

**Decision**: 100% sampling (local/dev), 50% (sandbox/staging/UAT), 5% (production). Configurable via config/observability.yaml.
**Rationale**: Full sampling in dev provides complete debugging data. Production 5% sampling controls storage cost and collector overhead while maintaining statistical validity.

## Best Practices Confirmed

- RED metrics (Rate, Errors, Duration) per endpoint via Micrometer + Prometheus scrape
- Liveness = /actuator/health/liveness; Readiness = /actuator/health/readiness (Spring Boot Actuator)
- Grafana dashboards for: service overview, per-tenant usage, billing metrics, security events
- Alertmanager routes: warning -> Slack; critical -> PagerDuty (production only)
- UserActivity records: user_id, tenant_id, action, ip_address, user_agent, timestamp
"""

research["009-security-compliance"] = """# Research: Security & Compliance

## Decision: GDPR Consent Management in DB

**Decision**: data_consents table with consent_type, status lifecycle (PENDING->ACTIVE->WITHDRAWN/EXPIRED), legal_basis, retention_period.
**Rationale**: GDPR Art. 7 requires demonstrable consent with timestamp, version, and withdrawal support. DB-based consent provides audit trail and supports right-to-withdraw queries.

## Decision: Kafka UserDataErasureEvent for Right-to-Erasure

**Decision**: ERASURE data subject request triggers a Kafka event (UserDataErasureEvent). Every service consuming user data MUST implement a handler that purges PII for that userId.
**Rationale**: Microservices architecture means user data is distributed. Point-to-point erasure calls are brittle. Kafka event with at-least-once delivery + idempotent handlers ensures all services process erasure.

## Decision: AES-256 Encryption for PHI

**Decision**: protected_health_information.phi_data stored AES-256-CBC encrypted. SHA-256 hash stored alongside for integrity verification. Encryption key from SecretsProvider only.
**Rationale**: HIPAA Security Rule requires encryption of ePHI at rest (addressable but standard practice). AES-256 meets NIST requirements. Key separation prevents encrypted PHI exposure if DB is compromised.

## Decision: Risk-Level Security Alert Scoring

**Decision**: Security events scored 0-100 (risk_score). Severity mapped by score range: LOW (0-33), MEDIUM (34-66), HIGH (67-84), CRITICAL (85-100).
**Rationale**: Enables automated alerting thresholds and compliance dashboard aggregation. Risk score combines: sensitivity of data accessed, number of anomalous events, time-of-day, IP reputation.

## Best Practices Confirmed

- GDPR data retention: 7 years default (2555 days); configurable per tenant
- Account data anonymized on deletion (not deleted): pseudonymize first, then delete PII fields
- HIPAA minimum_necessary_standard flag enforced by PHI access audit
- SAST tools: SpotBugs + OWASP dependency-check in CI pipeline
- DAST: OWASP ZAP against staging environment before release
- Secrets rotation schedule: 90 days for API keys, 30 days for DB passwords
"""

research["010-api-gateway"] = """# Research: API Gateway (Kong)

## Decision: Kong as Single North-South Gateway

**Decision**: Kong handles: JWT validation, rate limiting, CORS, request transformation (header injection), WAF (Kong + ModSecurity plugin), tenant routing.
**Rationale**: Centralised security policy enforcement. No per-service JWT validation. Token re-validation downstream is explicitly prohibited by constitution VIII. Kong is cloud-agnostic (runs on any K8s).

## Decision: Declarative DB-Less Kong Configuration

**Decision**: Kong configured via YAML (kong.yml, kong-multi-tenant.yml, etc.). DB-less mode for faster recovery and GitOps compatibility.
**Rationale**: Config-as-code: YAML lives in version control. No Kong DB to maintain. Config applies on Kong restart or via Admin API deck sync.

## Decision: Plan-Tiered Rate Limiting

**Decision**: Rate limits enforced per Kong Consumer (tenant). Consumer tagged with plan tier. Rate limit plugin applies per-consumer limits from environment config.
**Rationale**: Different plans have different API call entitlements (max_api_calls_per_month in plan). Kong Consumer is the natural unit for rate limit buckets.

## Decision: Request Transformer for Tenant Header Injection

**Decision**: Kong Request Transformer plugin adds X-Tenant-ID, X-Organization-ID, X-User-ID headers derived from JWT claims.
**Rationale**: Backends trust these headers implicitly (injected by trusted gateway only). Simplifies downstream auth — no JWT re-parsing needed.

## Best Practices Confirmed

- JWT validation: RS256 asymmetric signing; public key fetched from Keycloak JWKS endpoint
- CORS: explicit allowed origins list (no wildcard in production)
- TLS: self-signed cert (local), Let's Encrypt (dev-UAT), commercial cert (pre-prod+)
- WAF: kong-plugin-modsecurity or NGINX ModSecurity; basic rules sandbox-UAT; full OWASP CRS production
- Health check route: /api/health (public, no JWT required) -> each service's actuator
"""

research["011-tenant-configuration-management"] = """# Research: Tenant Configuration Management

## Decision: Hierarchical Fallback Config Resolution

**Decision**: Three-tier hierarchy: (1) tenant+key+env specific → (2) key+env global → (3) key+default global.
**Rationale**: Allows platform defaults to be overridden per environment and further per tenant. Reduces config duplication for common settings.

## Decision: AES-256 Encryption for Sensitive Config

**Decision**: Config values with is_encrypted=true stored AES-256-CBC encrypted. Decrypted transparently on read.
**Rationale**: API keys, webhook secrets, SMTP passwords must not be stored in plaintext. Encryption key sourced from SecretsProvider, never from application config files.

## Decision: Spring Cache (Redis) for Config Reads

**Decision**: @Cacheable(value="tenantConfigs", key="#tenantId + '_' + #configKey + '_' + #environment") accelerates reads.
**Rationale**: Config is read-heavy (every request may need 1-3 config values). DB round-trip on every request at 1000 req/s = 3000 DB queries/sec just for config. Cache reduces to near zero.

## Decision: Type-Safe Retrieval

**Decision**: Overloaded getConfiguration(tenantId, key, env, Class<T>) returns typed values.
**Rationale**: Prevents type confusion bugs. Integer config used as Integer, not parsed at call site. Simplifies calling code.

## Best Practices Confirmed

- Cache key includes environment to prevent staging config leaking into production reads
- Encryption key stored in Vault/SecretsManager; injected at service startup via SecretsProvider
- AES key ID stored with encrypted value for key rotation support (decrypt old, re-encrypt with new key)
- Config updates must call CacheManager.evictIfPresent(cacheKey) before returning success
- JSONB type for complex objects: stored as JSON string, deserialized via ObjectMapper
"""

research["012-frontend-application"] = """# Research: Frontend Application

## Decision: React 18 + TypeScript + React Router 6

**Decision**: Industry-standard React SPA stack. TypeScript for type safety. React Router 6 for declarative nested routing (Layout  -> ProtectedRoute -> Page).
**Rationale**: Existing codebase already uses this stack. Consistency; team familiarity; extensive ecosystem.

## Decision: AuthContext with localStorage Persistence

**Decision**: AuthContext stores user object in React state AND localStorage. Restores session on page refresh via useEffect.
**Rationale**: JWT-based SPA pattern. localStorage survives page reload. Cleared on logout. Note: implement httpOnly cookie alternative if XSS risk increases.

## Decision: React Query for Server State

**Decision**: @tanstack/react-query manages all server state: caching, background refetch, loading/error states.
**Rationale**: Eliminates manual loading state management. Stale-while-revalidate for snappy UX. Prefetch on hover for admin navigation.

## Decision: Axios Interceptor for Tenant Header Injection

**Decision**: Axios request interceptor adds Authorization: Bearer {token} and X-Tenant-ID: {tenantId} to every API call.
**Rationale**: Kong requires these headers. Centralising in interceptor eliminates per-call boilerplate. Token refresh logic in response interceptor handles 401 automatically.

## Decision: Code Splitting for Admin Pages

**Decision**: React.lazy() + Suspense for all admin pages. Public routes (Landing, Login, Register) bundled in main chunk.
**Rationale**: Admin pages are each 50-100KB. Loading all 12 admin pages upfront adds 600-1200KB to initial bundle. Code splitting reduces LCP for auth/user flows.

## Best Practices Confirmed

- react-hot-toast for notifications: toast.success(), toast.error() called after all mutations
- Form validation: React Hook Form with zod resolver for schema-based validation
- Error boundaries around each admin page to contain failures
- ARIA labels on all interactive elements for accessibility
- CSS variables for brand colors (green, red, dark-red) defined in index.css
"""

research["013-infrastructure-deployment"] = """# Research: Infrastructure & Deployment

## Decision: Terraform Module-per-Provider Architecture

**Decision**: Terraform modules under infrastructure/terraform/modules/ with per-cloud implementations under infrastructure/terraform/providers/{aws,gcp,azure,local}/. Active provider set in config/providers.yaml.
**Rationale**: Constitution II (Cloud Agnostic) requires zero code changes when switching providers. Module abstraction ensures the same Terraform inputs produce equivalent infrastructure on all clouds.

## Decision: Kustomize for Kubernetes Manifests

**Decision**: Kustomize base/ with overlays/ for all 7 environments. base/ contains shared manifests; overlays/ customise resource limits, replica counts, deployment strategies, SLOs.
**Rationale**: Kustomize is native to kubectl; no Helm chart complexity. Overlay pattern handles environment differences cleanly. ArgoCD integrates natively with Kustomize.

## Decision: Dagger Pipelines (TypeScript)

**Decision**: All pipeline logic in TypeScript Dagger modules under pipeline/. SCM-specific files (.github/workflows, .gitlab-ci.yml, etc.) are thin wrappers invoking dagger run.
**Rationale**: Constitution III (SCM Agnostic) is NON-NEGOTIABLE. Pipeline logic must be SCM-independent. Dagger runs locally (developers test pipeline without pushing); SCM wrappers are < 10 lines.

## Decision: HashiCorp Vault for Local Secrets

**Decision**: Vault runs in local Docker Compose stack. Application reads secrets via SecretsProvider interface. Cloud deployments use AWS Secrets Manager / GCP Secret Manager / Azure Key Vault.
**Rationale**: Same code path for all environments. Vault dev mode for local (no persistence). SecretsProvider maps to appropriate backend per cloud.

## Decision: ArgoCD for GitOps Deployments

**Decision**: ArgoCD monitors git repository; applies Kubernetes manifests on commit to environment branches.
**Rationale**: Cloud-agnostic GitOps (runs on any K8s). Declarative desired state. Automatic drift detection and self-healing. Audit trail of all deployments.

## Best Practices Confirmed

- Local docker-compose.yml already provides: PostgreSQL, Redis, Keycloak, Kong, RabbitMQ, ELK, Prometheus, Alertmanager, OTEL
- Makefile targets: local-up, local-infra, local-service, local-test, local-seed, local-down (as per constitution)
- scale-to-zero: dev/sandbox/staging/UAT use KEDA (Kubernetes Event-Driven Autoscaler) with overnight cron schedule
- Database migrations: Liquibase applied automatically on service startup (non-prod); manual approval gate (prod)
- Container scanning: Trivy in CI pipeline; images rejected if CRITICAL CVEs detected
"""

# Write research files
written = 0
for feature, content in research.items():
    path = os.path.join(BASE, feature, "research.md")
    with open(path, 'w') as f:
        f.write(content.strip() + "\n")
    print(f"OK: {feature}/research.md")
    written += 1

print(f"\nResearch files written: {written}")
