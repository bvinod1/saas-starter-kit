#!/usr/bin/env python3
import os, textwrap

SPECS_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "specs")

DATA_MODELS = {
    "001-authentication-identity-management": textwrap.dedent("""\
        # Data Model: Authentication & Identity Management

        ## Entity: User (Keycloak)
        Managed by Keycloak; local mirror held in `auth-service`.

        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | Keycloak user ID |
        | username | VARCHAR(150) | UNIQUE per realm |
        | email | VARCHAR(255) | UNIQUE per realm |
        | tenant_id | UUID (FK → tenants.id) | Realm partition |
        | mfa_enabled | BOOLEAN | TOTP/WEBAUTHN toggle |
        | created_at | TIMESTAMPTZ | |
        | updated_at | TIMESTAMPTZ | |

        ## Entity: RefreshToken
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID (FK → users.id) | |
        | tenant_id | UUID (FK → tenants.id) | |
        | token_hash | BYTEA | SHA-256 of opaque token |
        | expires_at | TIMESTAMPTZ | |
        | revoked | BOOLEAN | |
        | created_at | TIMESTAMPTZ | |

        ## Entity: AuditLog
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID | Nullable for anonymous |
        | tenant_id | UUID | |
        | event_type | VARCHAR(60) | LOGIN, LOGOUT, MFA_ENROLL, … |
        | ip_address | INET | |
        | user_agent | TEXT | |
        | occurred_at | TIMESTAMPTZ | |

        ## Relationships
        ```
        Tenant 1 ──< User 1 ──< RefreshToken
        User 1 ──< AuditLog
        ```

        ## Indexes
        - `users(tenant_id, email)` UNIQUE
        - `refresh_tokens(token_hash)` UNIQUE
        - `refresh_tokens(user_id, expires_at)`
        - `audit_log(tenant_id, occurred_at)`

        ## State Transitions (User Account)
        ```
        PENDING_VERIFICATION → ACTIVE → SUSPENDED → DEACTIVATED
        ACTIVE → MFA_PENDING_SETUP (on policy change)
        ```

        ## Migration References
        - `database/init/01-init-schema.sql` – users table
        - `database/migrations/05-gdpr-hipaa-compliance.sql` – audit extension
    """),

    "002-multi-tenancy-tenant-lifecycle": textwrap.dedent("""\
        # Data Model: Multi-Tenancy & Tenant Lifecycle

        ## Entity: Tenant
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | name | VARCHAR(200) | Display name |
        | subdomain | VARCHAR(100) | UNIQUE; URL slug |
        | status | tenant_status ENUM | PENDING, ACTIVE, SUSPENDED, ARCHIVED |
        | plan_id | VARCHAR(50) | Maps to billing plan |
        | schema_name | VARCHAR(63) | PostgreSQL schema: `tenant_{id}` |
        | keycloak_realm | VARCHAR(100) | Dedicated realm name |
        | created_at | TIMESTAMPTZ | |
        | updated_at | TIMESTAMPTZ | |

        ## Entity: TenantConfiguration
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID (FK → tenants.id) UNIQUE | |
        | features | JSONB | Feature flag map |
        | limits | JSONB | Resource quotas |
        | custom_domain | VARCHAR(255) | Optional |
        | updated_at | TIMESTAMPTZ | |

        ## Entity: TenantProvisioningJob
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID (FK) | |
        | status | job_status ENUM | QUEUED, RUNNING, DONE, FAILED |
        | error_message | TEXT | |
        | started_at | TIMESTAMPTZ | |
        | completed_at | TIMESTAMPTZ | |

        ## Relationships
        ```
        Tenant 1 ──1 TenantConfiguration
        Tenant 1 ──< TenantProvisioningJob
        Tenant 1 ──< (schema: tenant_{id}) schema-isolated tables
        ```

        ## Indexes
        - `tenants(subdomain)` UNIQUE
        - `tenants(status, created_at)`
        - `tenant_provisioning_jobs(tenant_id, status)`

        ## State Transitions (Tenant)
        ```
        PENDING → PROVISIONING → ACTIVE → SUSPENDED → ARCHIVED
        PROVISIONING → FAILED (on error; retryable)
        ```

        ## Migration References
        - `database/init/02-multi-tenant-schema.sql`
        - `database/migrations/04-create-tenant-configurations.sql`
    """),

    "003-user-management": textwrap.dedent("""\
        # Data Model: User Management

        ## Entity: UserProfile
        Stored in `user-service` PostgreSQL; Keycloak holds credentials.

        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | Matches Keycloak user ID |
        | tenant_id | UUID (FK → tenants.id) | Row-level isolation |
        | email | VARCHAR(255) | |
        | first_name | VARCHAR(100) | |
        | last_name | VARCHAR(100) | |
        | phone | VARCHAR(30) | |
        | avatar_url | TEXT | |
        | status | user_status ENUM | ACTIVE, SUSPENDED, DEACTIVATED |
        | dynamic_fields | JSONB | Per-tenant custom profile schema |
        | created_at | TIMESTAMPTZ | |
        | updated_at | TIMESTAMPTZ | |

        ## Entity: UserTenantRole
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID (FK → user_profiles.id) | |
        | tenant_id | UUID | |
        | role_id | UUID (FK → roles.id) | |
        | granted_at | TIMESTAMPTZ | |
        | granted_by | UUID | |

        ## Entity: UserConsent (GDPR)
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID | |
        | tenant_id | UUID | |
        | consent_type | VARCHAR(60) | MARKETING, ANALYTICS, ESSENTIAL |
        | consented | BOOLEAN | |
        | consented_at | TIMESTAMPTZ | |
        | ip_address | INET | |

        ## Relationships
        ```
        Tenant 1 ──< UserProfile
        UserProfile 1 ──< UserTenantRole
        UserProfile 1 ──< UserConsent
        ```

        ## Dynamic Profile Schema
        Tenant administrators define additional fields via `TenantProfileSchema` (JSONB):
        ```json
        {
          "fields": [
            { "name": "department", "type": "string", "required": true },
            { "name": "employee_id", "type": "string", "required": false }
          ]
        }
        ```
        Stored in `dynamic_fields` JSONB; validated at service layer against schema.

        ## Indexes
        - `user_profiles(tenant_id, email)` UNIQUE
        - `user_profiles(tenant_id, status)`
        - `user_tenant_roles(user_id, tenant_id)`

        ## Migration References
        - `database/init/01-init-schema.sql`
        - `database/migrations/05-gdpr-hipaa-compliance.sql`
    """),

    "004-organization-management": textwrap.dedent("""\
        # Data Model: Organization Management

        ## Entity: Organization
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID (FK → tenants.id) | Owning tenant |
        | name | VARCHAR(200) | |
        | parent_org_id | UUID (FK → organizations.id) | Nullable; hierarchy |
        | type | VARCHAR(50) | COMPANY, DIVISION, DEPARTMENT, TEAM |
        | status | org_status ENUM | ACTIVE, ARCHIVED |
        | metadata | JSONB | Flexible attributes |
        | created_by | UUID | |
        | created_at | TIMESTAMPTZ | |
        | updated_at | TIMESTAMPTZ | |
        | deleted_at | TIMESTAMPTZ | Soft-delete |

        ## Entity: OrganizationMembership
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | org_id | UUID (FK → organizations.id) | |
        | user_id | UUID (FK → user_profiles.id) | |
        | role_in_org | VARCHAR(60) | OWNER, ADMIN, MEMBER |
        | joined_at | TIMESTAMPTZ | |
        | invited_by | UUID | |

        ## Entity: OrganizationAuditEvent
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | org_id | UUID | |
        | tenant_id | UUID | |
        | actor_id | UUID | |
        | event_type | VARCHAR(80) | ORG_CREATED, MEMBER_ADDED, … |
        | payload | JSONB | Before/after snapshot |
        | occurred_at | TIMESTAMPTZ | |

        ## Relationships
        ```
        Tenant 1 ──< Organization (self-referential tree via parent_org_id)
        Organization 1 ──< OrganizationMembership
        Organization 1 ──< OrganizationAuditEvent
        ```

        ## Indexes
        - `organizations(tenant_id, parent_org_id)`
        - `organizations(tenant_id, deleted_at)` WHERE deleted_at IS NULL
        - `org_memberships(org_id, user_id)` UNIQUE
        - `org_memberships(user_id, tenant_id)`

        ## State Transitions (Organization)
        ```
        ACTIVE → ARCHIVED (soft delete; deleted_at set)
        ```

        ## Migration References
        - `database/init/03-organizational-management-schema.sql`
    """),

    "005-role-based-access-control": textwrap.dedent("""\
        # Data Model: Role-Based Access Control

        ## Entity: Role
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | NULL = system role |
        | name | VARCHAR(100) | |
        | description | TEXT | |
        | is_system | BOOLEAN | Immutable built-in role |
        | created_at | TIMESTAMPTZ | |

        ## Entity: Permission
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | resource | VARCHAR(100) | e.g. `users`, `billing` |
        | action | VARCHAR(50) | CREATE, READ, UPDATE, DELETE, EXECUTE |
        | description | TEXT | |

        ## Entity: RolePermission
        | Field | Type | Notes |
        |-------|------|-------|
        | role_id | UUID (FK → roles.id) | |
        | permission_id | UUID (FK → permissions.id) | |
        | PK | (role_id, permission_id) | |

        ## Entity: UserRole
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID | |
        | role_id | UUID (FK → roles.id) | |
        | tenant_id | UUID | Scope |
        | granted_by | UUID | |
        | granted_at | TIMESTAMPTZ | |
        | expires_at | TIMESTAMPTZ | Nullable |

        ## Entity: PolicyCacheEntry (Redis)
        | Field | Type | Notes |
        |-------|------|-------|
        | key | STRING | `rbac:{tenant_id}:{user_id}` |
        | permissions | JSON ARRAY | Resolved permission list |
        | ttl | 300s | Cache TTL |

        ## Relationships
        ```
        Tenant 1 ──< Role
        Role M ──< RolePermission >── M Permission
        User M ──< UserRole >── M Role
        ```

        ## Indexes
        - `roles(tenant_id, name)` UNIQUE
        - `user_roles(user_id, tenant_id)`
        - `user_roles(role_id)`

        ## Migration References
        - `database/init/01-init-schema.sql` (roles, permissions)
        - `database/init/03-organizational-management-schema.sql` (org-scoped roles)
    """),

    "006-subscription-billing-management": textwrap.dedent("""\
        # Data Model: Subscription & Billing Management

        ## Entity: SubscriptionPlan
        | Field | Type | Notes |
        |-------|------|-------|
        | id | VARCHAR(50) (PK) | e.g. `starter`, `pro`, `enterprise` |
        | name | VARCHAR(100) | Display name |
        | price_monthly_cents | INTEGER | 0 = free |
        | price_annual_cents | INTEGER | |
        | features | JSONB | Feature entitlement map |
        | limits | JSONB | User/API quotas |
        | active | BOOLEAN | |

        ## Entity: Subscription
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID (FK → tenants.id) UNIQUE | |
        | plan_id | VARCHAR(50) (FK → plans.id) | |
        | status | sub_status ENUM | TRIAL, ACTIVE, PAST_DUE, CANCELLED, EXPIRED |
        | billing_cycle | VARCHAR(10) | MONTHLY, ANNUAL |
        | current_period_start | TIMESTAMPTZ | |
        | current_period_end | TIMESTAMPTZ | |
        | trial_ends_at | TIMESTAMPTZ | Nullable |
        | external_subscription_id | VARCHAR(200) | Provider reference |
        | created_at | TIMESTAMPTZ | |
        | updated_at | TIMESTAMPTZ | |

        ## Entity: Invoice
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | subscription_id | UUID (FK) | |
        | amount_cents | INTEGER | |
        | currency | CHAR(3) | ISO 4217 |
        | status | invoice_status ENUM | DRAFT, OPEN, PAID, VOID, UNCOLLECTIBLE |
        | due_date | DATE | |
        | paid_at | TIMESTAMPTZ | |
        | external_invoice_id | VARCHAR(200) | |
        | created_at | TIMESTAMPTZ | |

        ## Entity: PaymentMethod
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | provider_token | VARCHAR(300) | Tokenized; never raw card |
        | type | VARCHAR(30) | CARD, BANK_TRANSFER |
        | last4 | CHAR(4) | |
        | is_default | BOOLEAN | |
        | created_at | TIMESTAMPTZ | |

        ## Relationships
        ```
        SubscriptionPlan 1 ──< Subscription
        Tenant 1 ──1 Subscription
        Tenant 1 ──< Invoice
        Tenant 1 ──< PaymentMethod
        Subscription 1 ──< Invoice
        ```

        ## Indexes
        - `subscriptions(tenant_id)` UNIQUE
        - `subscriptions(status, current_period_end)` – renewal sweep
        - `invoices(tenant_id, status)`
        - `invoices(external_invoice_id)` UNIQUE

        ## State Transitions (Subscription)
        ```
        TRIAL → ACTIVE (payment collected)
        ACTIVE → PAST_DUE (payment fails)
        PAST_DUE → ACTIVE (retry succeeds)
        PAST_DUE → CANCELLED (grace period expires)
        ACTIVE → CANCELLED (user cancel)
        CANCELLED → EXPIRED (period ends)
        ```

        ## Migration References
        - `database/migrations/04-subscription-management.sql`
    """),

    "007-notification-service": textwrap.dedent("""\
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
    """),

    "008-analytics-observability": textwrap.dedent("""\
        # Data Model: Analytics & Observability

        ## OpenTelemetry Resource Attributes (all spans)
        | Attribute | Value |
        |-----------|-------|
        | `service.name` | service identifier |
        | `tenant.id` | UUID of current tenant |
        | `deployment.environment` | dev / staging / prod / … |

        ## Entity: MetricDefinition (Prometheus labels)
        Metrics emitted by each service follow naming: `saas_{service}_{metric}_{unit}`.

        | Metric | Type | Labels |
        |--------|------|--------|
        | `saas_http_requests_total` | Counter | service, method, path, status, tenant_id |
        | `saas_http_request_duration_seconds` | Histogram | service, method, path, tenant_id |
        | `saas_active_users_gauge` | Gauge | tenant_id |
        | `saas_subscription_mrr_cents` | Gauge | plan_id |
        | `saas_notification_sent_total` | Counter | channel, status, tenant_id |

        ## Entity: TenantUsageSummary (PostgreSQL — analytics-service)
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | period_start | DATE | |
        | period_end | DATE | |
        | active_users | INTEGER | |
        | api_calls | BIGINT | |
        | storage_bytes | BIGINT | |
        | computed_at | TIMESTAMPTZ | |

        ## Entity: FeatureUsageEvent (time-series — future: TimescaleDB)
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | user_id | UUID | Nullable |
        | feature | VARCHAR(100) | |
        | action | VARCHAR(50) | |
        | occurred_at | TIMESTAMPTZ | |
        | metadata | JSONB | |

        ## Log Schema (ElasticSearch index: `saas-logs-*`)
        ```json
        {
          "timestamp": "ISO8601",
          "level": "INFO|WARN|ERROR",
          "service": "user-service",
          "tenant_id": "uuid",
          "trace_id": "hex",
          "span_id": "hex",
          "message": "...",
          "context": {}
        }
        ```

        ## Sampling Strategy
        | Environment | Traces sampled |
        |-------------|---------------|
        | dev | 100 % |
        | staging | 100 % |
        | prod | 10 % (errors always 100 %) |

        ## Indexes
        - `tenant_usage_summary(tenant_id, period_start)` UNIQUE
        - `feature_usage_events(tenant_id, occurred_at)`
        - `feature_usage_events(feature, tenant_id, occurred_at)`

        ## Migration References
        - No dedicated migration; analytics-service manages its own schema
    """),

    "009-security-compliance": textwrap.dedent("""\
        # Data Model: Security & Compliance

        ## Entity: ConsentRecord (GDPR)
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID | |
        | tenant_id | UUID | |
        | consent_type | VARCHAR(60) | MARKETING, ANALYTICS, COOKIES |
        | consented | BOOLEAN | |
        | version | VARCHAR(20) | Policy version string |
        | consented_at | TIMESTAMPTZ | |
        | ip_address | INET | |
        | user_agent | TEXT | |

        ## Entity: DataErasureRequest (GDPR Right-to-Erasure)
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | user_id | UUID | |
        | tenant_id | UUID | |
        | requested_at | TIMESTAMPTZ | |
        | completed_at | TIMESTAMPTZ | Nullable |
        | status | erasure_status ENUM | PENDING, IN_PROGRESS, COMPLETED, FAILED |
        | services_notified | TEXT[] | Services that received event |

        ## Entity: SecurityEvent
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | user_id | UUID | Nullable |
        | event_type | VARCHAR(80) | FAILED_LOGIN, BRUTE_FORCE, PRIVILEGE_ESCALATION, … |
        | severity | VARCHAR(10) | LOW, MEDIUM, HIGH, CRITICAL |
        | source_ip | INET | |
        | details | JSONB | |
        | occurred_at | TIMESTAMPTZ | |

        ## Entity: EncryptedPHIField (HIPAA)
        Marker for fields encrypted with AES-256-GCM before persistence.

        | Column pattern | Encryption |
        |----------------|------------|
        | `*_phi` | AES-256-GCM, key in Vault |
        | `ssn`, `dob`, `diagnosis` | AES-256-GCM |

        ## Relationships
        ```
        User 1 ──< ConsentRecord
        User 1 ──< DataErasureRequest
        Tenant 1 ──< SecurityEvent
        ```

        ## Indexes
        - `consent_records(user_id, tenant_id, consent_type)` UNIQUE per version
        - `data_erasure_requests(user_id, status)`
        - `security_events(tenant_id, severity, occurred_at)`

        ## State Transitions (DataErasureRequest)
        ```
        PENDING → IN_PROGRESS (saga coordinator picks up)
        IN_PROGRESS → COMPLETED (all services ack)
        IN_PROGRESS → FAILED (timeout/error)
        FAILED → IN_PROGRESS (retry)
        ```

        ## Migration References
        - `database/migrations/05-gdpr-hipaa-compliance.sql`
    """),

    "010-api-gateway": textwrap.dedent("""\
        # Data Model: API Gateway

        > Kong operates DB-less; all state is in declarative YAML. No relational model.

        ## Kong Entity Hierarchy
        ```
        Service
         └── Route
              └── Plugin (per-route or global)
        Consumer (represents a tenant API key)
        Upstream
         └── Target (backend instance)
        ```

        ## Service Definition Schema
        | Field | Type | Example |
        |-------|------|---------|
        | name | string | `auth-service` |
        | url | string | `http://auth-service:8080` |
        | connect_timeout | integer ms | 5000 |
        | read_timeout | integer ms | 30000 |
        | retries | integer | 2 |

        ## Route Definition Schema
        | Field | Type | Example |
        |-------|------|---------|
        | name | string | `auth-routes` |
        | paths | string[] | `["/api/v1/auth"]` |
        | methods | string[] | `["GET","POST"]` |
        | strip_path | boolean | true |
        | service.name | string | `auth-service` |

        ## Plugin Schema (Rate Limiting)
        | Parameter | Value |
        |-----------|-------|
        | plugin | `rate-limiting` |
        | config.second | plan-tier dependent |
        | config.minute | plan-tier dependent |
        | config.policy | `local` |
        | config.limit_by | `header` (X-Tenant-ID) |

        ## Consumer Schema (Tenant API Key)
        | Field | Type | Notes |
        |-------|------|-------|
        | username | string | `tenant-{tenant_id}` |
        | custom_id | string | Tenant UUID |
        | keyauth_credentials[].key | string | SHA-256 hashed API key |

        ## Upstream Health Check Configuration
        ```yaml
        healthchecks:
          active:
            http_path: /actuator/health
            interval: 10
            healthy.successes: 2
            unhealthy.http_failures: 3
        ```

        ## Per-Plan Rate Limits
        | Plan | Requests/sec | Requests/min |
        |------|-------------|--------------|
        | starter | 10 | 300 |
        | pro | 50 | 2000 |
        | enterprise | 200 | 10000 |

        ## Config Files
        - `api-gateway/kong-multi-tenant-enhanced.yml` – production config
        - `api-gateway/kong-multi-tenant.yml` – base config
        - `api-gateway/kong-organisation.yml` – org-specific routes
    """),

    "011-tenant-configuration-management": textwrap.dedent("""\
        # Data Model: Tenant Configuration Management

        ## Configuration Hierarchy
        ```
        Global Defaults (system-wide, code/YAML)
            ↓ override
        Plan Defaults (per subscription tier)
            ↓ override
        Tenant Overrides (admin-set, DB)
            ↓ override
        Organization Overrides (org-level, DB)
        ```

        ## Entity: GlobalConfigDefault
        | Field | Type | Notes |
        |-------|------|-------|
        | key | VARCHAR(200) (PK) | Dot-notation: `feature.mfa.required` |
        | value | TEXT | Serialized value |
        | data_type | VARCHAR(20) | STRING, INTEGER, BOOLEAN, JSON |
        | description | TEXT | |

        ## Entity: PlanConfigDefault
        | Field | Type | Notes |
        |-------|------|-------|
        | plan_id | VARCHAR(50) | |
        | key | VARCHAR(200) | |
        | value | TEXT | |
        | PK | (plan_id, key) | |

        ## Entity: TenantConfig
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | tenant_id | UUID | |
        | key | VARCHAR(200) | |
        | value | TEXT | AES-256-GCM encrypted if sensitive |
        | is_secret | BOOLEAN | Vault-backed if true |
        | updated_by | UUID | |
        | updated_at | TIMESTAMPTZ | |
        | UNIQUE | (tenant_id, key) | |

        ## Entity: OrgConfig
        | Field | Type | Notes |
        |-------|------|-------|
        | id | UUID (PK) | |
        | org_id | UUID | |
        | tenant_id | UUID | For RLS |
        | key | VARCHAR(200) | |
        | value | TEXT | |
        | UNIQUE | (org_id, key) | |

        ## Entity: ConfigCacheEntry (Redis)
        | Key pattern | TTL |
        |-------------|-----|
        | `config:{tenant_id}:{key}` | 300 s |
        | `config:{tenant_id}:*` | Bulk invalidated on update |

        ## Sensitive Config Storage
        - is_secret = true → value is Vault path reference
        - Vault path: `secret/saas/{env}/tenant/{tenant_id}/{key}`
        - Vault dynamic secrets for DB credentials

        ## Relationships
        ```
        Tenant 1 ──< TenantConfig
        Org 1 ──< OrgConfig
        GlobalConfigDefault <── resolved by ── ConfigResolver service
        ```

        ## Indexes
        - `tenant_config(tenant_id, key)` UNIQUE
        - `org_config(org_id, key)` UNIQUE
        - `global_config_default(key)` PK

        ## Migration References
        - `database/migrations/04-create-tenant-configurations.sql`
    """),

    "012-frontend-application": textwrap.dedent("""\
        # Data Model: Frontend Application

        > The frontend is a React SPA; it has no persistent store. All data
        > is fetched from backend APIs. This file documents client-side
        > state shapes and local storage contracts.

        ## Client-Side Auth State (AuthContext)
        ```typescript
        interface AuthState {
          user: {
            id: string;          // Keycloak user UUID
            email: string;
            firstName: string;
            lastName: string;
            roles: string[];
            tenantId: string;
          } | null;
          accessToken: string | null;   // JWT; stored in memory only
          refreshToken: string | null;  // Stored in httpOnly cookie
          isAuthenticated: boolean;
          isLoading: boolean;
        }
        ```

        ## LocalStorage Keys
        | Key | Value | Notes |
        |-----|-------|-------|
        | `saas.tenantId` | UUID string | Current active tenant |
        | `saas.theme` | `light` \| `dark` | UI preference |
        | `saas.locale` | BCP-47 string | e.g. `en-US` |

        > **Security note**: Access tokens MUST NOT be stored in localStorage.
        > Use in-memory state only; refresh token lives in httpOnly cookie.

        ## React Query Cache Keys (naming convention)
        ```
        ['users', tenantId]
        ['users', tenantId, userId]
        ['billing', 'subscription', tenantId]
        ['billing', 'invoices', tenantId]
        ['notifications', tenantId, userId]
        ['analytics', tenantId, 'usage']
        ['org', tenantId, orgId]
        ```

        ## API Request Headers (Axios interceptor)
        | Header | Value | Source |
        |--------|-------|--------|
        | `Authorization` | `Bearer {accessToken}` | AuthContext |
        | `X-Tenant-ID` | `{tenantId}` | AuthContext |
        | `X-Correlation-ID` | UUID v4 per request | Generated |

        ## Route → Permission Map
        | Route | Required Permission |
        |-------|---------------------|
        | `/admin/*` | `admin:read` |
        | `/billing/*` | `billing:read` |
        | `/users/*` | `users:read` |
        | `/analytics/*` | `analytics:read` |
        | `/settings/*` | `settings:read` |

        ## Component State Contracts
        ### LoginPage
        ```typescript
        interface LoginFormState {
          email: string;
          password: string;
          tenantSubdomain: string;
          mfaCode: string | null;
          error: string | null;
          isSubmitting: boolean;
        }
        ```

        ## Files
        - `frontend/src/contexts/` – AuthContext, TenantContext
        - `frontend/src/services/` – API service layer
        - `frontend/src/components/auth/` – Auth UI components
        - `frontend/src/pages/` – Page components
    """),

    "013-infrastructure-deployment": textwrap.dedent("""\
        # Data Model: Infrastructure & Deployment

        > Infrastructure is code; this data model documents the Terraform
        > resource graph, Kubernetes manifest hierarchy, and pipeline inputs.

        ## Terraform Resource Graph

        ### Module: `infra/terraform/modules/networking/`
        | Resource | Provider | Key Inputs |
        |----------|----------|-----------|
        | VPC / VNet | aws/gcp/azure | cidr_block, environment |
        | Subnets (public/private) | provider | az_count |
        | Security Groups / NSG | provider | ingress_rules |
        | Load Balancer | provider | internal, tls_cert_arn |

        ### Module: `infra/terraform/modules/database/`
        | Resource | Provider | Key Inputs |
        |----------|----------|-----------|
        | PostgreSQL instance | aws RDS / gcp CloudSQL / azure DB | version=15, multi_az |
        | Redis cluster | ElastiCache / Memorystore / Azure Cache | node_type, replicas |

        ### Module: `infra/terraform/modules/kubernetes/`
        | Resource | Notes |
        |----------|-------|
        | EKS / GKE / AKS cluster | managed k8s |
        | Node groups | on-demand + spot/preemptible |
        | OIDC provider | For IRSA / Workload Identity |

        ## Kustomize Overlay Structure
        ```
        k8s/
          base/                   # shared manifests
          overlays/
            dev/
            ci/
            staging/
            canary/
            prod/
            dr/
            load-test/
        ```

        ## Kubernetes Manifest Schema (per service)
        | Manifest | Key Fields |
        |----------|-----------|
        | Deployment | replicas, image tag, resources, env from ConfigMap/Secret |
        | ConfigMap | app config, non-sensitive env |
        | ExternalSecret | Vault → k8s Secret via ESO |
        | HPA | minReplicas, maxReplicas, CPU/Memory targets |
        | PodDisruptionBudget | minAvailable |
        | ServiceMonitor | Prometheus scrape config |

        ## Pipeline Input Schema (Dagger TypeScript)
        ```typescript
        interface PipelineInputs {
          imageTag: string;          // git SHA or semver
          environment: Environment;  // dev | ci | staging | canary | prod | dr | load-test
          tenantIds?: string[];       // for tenant-scoped deploys
          dryRun: boolean;
          skipTests?: boolean;        // only allowed in dev
        }
        ```

        ## Secrets Hierarchy (Vault)
        ```
        secret/saas/
          {env}/
            global/           # shared across all tenants
              db_password
              jwt_secret
            tenant/
              {tenant_id}/    # tenant-specific secrets
                db_password
                api_keys/
                  stripe
                  sendgrid
        ```

        ## Environment Parity Rules
        All 7 environments MUST use identical Helm chart versions and base manifests.
        Differences are ONLY in:
        - replica counts
        - resource requests/limits
        - feature flag overrides
        - Vault secret paths

        ## Files
        - `docker-compose.yml` – local dev stack
        - `infrastructure/` – monitoring/logging configs
        - `application-iam.yml` – IAM roles
    """),
}


def main():
    written = 0
    for feature_dir, content in DATA_MODELS.items():
        target = os.path.join(SPECS_DIR, feature_dir, "data-model.md")
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w") as f:
            f.write(content)
        print(f"OK: {feature_dir}/data-model.md")
        written += 1
    print(f"\nData model files written: {written}")


if __name__ == "__main__":
    main()
