#!/usr/bin/env python3
"""Apply all analysis findings fixes across specs/."""

import os

BASE = "/Users/vinodbalasubramanian/Documents/saas_starter_kit/specs"

# ─────────────────────────────────────────────────────────────────────────────
# Helper
# ─────────────────────────────────────────────────────────────────────────────
def read(path):
    with open(path, "r") as f:
        return f.read()

def write(path, content):
    with open(path, "w") as f:
        f.write(content)
    print(f"  ✅ Written: {path}")

def replace_once(path, old, new, label=""):
    content = read(path)
    if old not in content:
        print(f"  ⚠️  NOT FOUND ({label}): {old[:60]!r} in {path}")
        return False
    content = content.replace(old, new, 1)
    write(path, content)
    return True

# ─────────────────────────────────────────────────────────────────────────────
# C4: Fix SCM path in 013 tasks  (.github/workflows → .scm/github/workflows)
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C4: Fix CI/CD SCM path in 013 tasks ==")
replace_once(
    f"{BASE}/013-infrastructure-deployment/tasks.md",
    "- [ ] T023 [P] [US4] Create `.github/workflows/ci.yml` — thin wrapper calling `dagger run pipeline/main.ts`",
    "- [ ] T023 [P] [US4] Create `.scm/github/workflows/ci.yml` — thin wrapper calling `dagger run pipeline/main.ts` (SCM-agnostic path per constitution Principle III)",
    "C4"
)

# Also fix the spec.md if it references .github/workflows
path013spec = f"{BASE}/013-infrastructure-deployment/spec.md"
content = read(path013spec)
if ".github/workflows" in content:
    content = content.replace(".github/workflows", ".scm/github/workflows")
    write(path013spec, content)

# ─────────────────────────────────────────────────────────────────────────────
# C6: Add missing Makefile targets to 013 tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C6: Add missing Makefile targets to 013 tasks ==")
replace_once(
    f"{BASE}/013-infrastructure-deployment/tasks.md",
    "- [ ] T002 [P] Create `Makefile` at repo root with targets: `local-up`, `local-down`, `local-seed`, `local-logs`, `build-all`",
    "- [ ] T002 [P] Create `Makefile` at repo root with ALL constitution-mandated targets: `local-up`, `local-down`, `local-seed`, `local-logs`, `build-all`, `local-infra` (infra services only), `local-service SERVICE=<name>` (single service with hot-reload), `local-test` (full integration suite)",
    "C6"
)

# ─────────────────────────────────────────────────────────────────────────────
# C1: Add shared-libs/providers/ task to 013 tasks (Phase 2 Foundational)
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C1: Add shared-libs provider interfaces to 013 tasks ==")
replace_once(
    f"{BASE}/013-infrastructure-deployment/tasks.md",
    "- [ ] T004 Create `backend/*/src/main/java/com/saasstarterkit/*/security/SecretsProvider.java` interface (each service) — `getSecret(key)`, `getSecretMap(prefix)` with `VaultSecretsProvider` impl and `EnvSecretsProvider` impl (local dev fallback)",
    """- [ ] T004 Create `backend/*/src/main/java/com/saasstarterkit/*/security/SecretsProvider.java` interface (each service) — `getSecret(key)`, `getSecretMap(prefix)` with `VaultSecretsProvider` impl and `EnvSecretsProvider` impl (local dev fallback)
- [ ] T004a [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/cache/CacheProvider.java` interface — `get(key)`, `set(key, value, ttlSeconds)`, `delete(key)`, `exists(key)`; impls: `RedisCacheProvider` (local/cloud) and `InMemoryCacheProvider` (test)
- [ ] T004b [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/queue/QueueProvider.java` interface — `publish(topic, message)`, `subscribe(topic, handler)`; impls: `RabbitMqQueueProvider` (local), `SqsQueueProvider` (aws), `PubSubQueueProvider` (gcp), `ServiceBusQueueProvider` (azure)
- [ ] T004c [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/storage/StorageProvider.java` interface — `upload(bucket, key, bytes)`, `download(bucket, key)`, `delete(bucket, key)`, `presignedUrl(bucket, key, ttl)`; impls: `S3StorageProvider`, `GcsStorageProvider`, `AzureBlobStorageProvider`, `MinioStorageProvider` (local)
- [ ] T004d [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/identity/IdentityProvider.java` interface — `createUser(attrs)`, `updateUser(id, attrs)`, `deleteUser(id)`, `assignRoles(id, roles)`; impl: `KeycloakIdentityProvider`
- [ ] T004e [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/notification/SmsProvider.java` interface — `send(to, body, tenantId)`; impls: `TwilioSmsProvider`, `PlivoSmsProvider`
- [ ] T004f [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/notification/PushProvider.java` interface — `sendPush(deviceToken, title, body, data)`; impl: `FcmPushProvider`
- [ ] T004g Create `shared-libs/providers/pom.xml` as a multi-module Maven project; add as `<dependency>` in each backend service pom.xml""",
    "C1"
)

# Update dependency note at end of 013 tasks
replace_once(
    f"{BASE}/013-infrastructure-deployment/tasks.md",
    "- **Phase 2** (T004–T008): Depends on Phase 1",
    "- **Phase 2** (T004–T008, T004a–T004g): Depends on Phase 1",
    "C1-dep"
)

# ─────────────────────────────────────────────────────────────────────────────
# C1: Reference provider interfaces in relevant feature specs (001, 002, 003, 007, 008)
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C1: Propagate provider interface references to feature tasks ==")

# Feature 001 auth-service — IdentityProvider
replace_once(
    f"{BASE}/001-authentication-identity-management/tasks.md",
    "**Checkpoint**: Auth entity persistence layer ready",
    "**Checkpoint**: Auth entity persistence layer ready\n\n> **Provider Contract**: All Keycloak calls MUST go through `IdentityProvider` from `shared-libs/providers/` — no direct Keycloak admin-client imports in service code.",
    "C1-001"
)

# Feature 002 tenant-service — QueueProvider (RabbitMQ events)
replace_once(
    f"{BASE}/002-multi-tenancy-tenant-lifecycle/tasks.md",
    "**Checkpoint**: Tenant entity persistence layer ready",
    "**Checkpoint**: Tenant entity persistence layer ready\n\n> **Provider Contract**: All RabbitMQ publishing MUST go through `QueueProvider` from `shared-libs/providers/`.",
    "C1-002"
)

# Feature 007 notification-service — EmailProvider, SmsProvider, PushProvider
replace_once(
    f"{BASE}/007-notification-service/tasks.md",
    "**Checkpoint**: Notification entities, provider registry, and template engine ready",
    "**Checkpoint**: Notification entities, provider registry, and template engine ready\n\n> **Provider Contract**: `EmailProvider`, `SmsProvider`, `PushProvider` MUST be consumed from `shared-libs/providers/`. Implementations (SendGridEmailProvider, TwilioSmsProvider, FcmPushProvider) live in shared-libs only.",
    "C1-007"
)

# Feature 008 analytics — CacheProvider
replace_once(
    f"{BASE}/008-analytics-observability/tasks.md",
    "**Checkpoint**: OTel pipelines, Prometheus, and Logstash ready for all services",
    "**Checkpoint**: OTel pipelines, Prometheus, and Logstash ready for all services\n\n> **Provider Contract**: Any cache access (e.g., metrics caching) MUST go through `CacheProvider` from `shared-libs/providers/`.",
    "C1-008"
)

# Feature 009 security — StorageProvider (for audit exports / portability)
replace_once(
    f"{BASE}/009-security-compliance/tasks.md",
    "**Checkpoint**: Encryption and secret management ready",
    "**Checkpoint**: Encryption and secret management ready\n\n> **Provider Contract**: All object storage (audit exports, GDPR portability archives) MUST go through `StorageProvider` from `shared-libs/providers/`.",
    "C1-009"
)

# ─────────────────────────────────────────────────────────────────────────────
# C2: Add schema builder tooling user story + tasks to feature 003
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C2: Add schema builder user story to 003 spec ==")
spec003 = f"{BASE}/003-user-management/spec.md"
replace_once(
    spec003,
    "### Edge Cases\n- What happens when creating a user with an email that already exists in Keycloak?\n- How are tenant-scoped users distinguished from global users?\n- What happens when a user is the last OWNER of an organisation?",
    """### User Story 6 — Schema builder & lifecycle management (Priority: P2) ⚠️ NON-NEGOTIABLE

An org admin uses a visual schema builder to create, preview, version-diff, deprecate, and bulk-backfill custom user profile schemas.

**Acceptance Scenarios**:
1. **Given** org admin, **When** new schema fields added, **Then** side-by-side diff of current vs. new version shown before publish.
2. **Given** published schema v2, **When** admin initiates bulk backfill, **Then** all existing users\' `dynamic_fields` updated with default values for new fields; progress trackable.
3. **Given** admin deprecates schema version, **When** users try to submit old-version data, **Then** 422 with migration prompt returned.
4. **Given** deprecated schema, **When** admin views version list, **Then** deprecated_at timestamp and reason displayed.

---

### Edge Cases
- What happens when creating a user with an email that already exists in Keycloak?
- How are tenant-scoped users distinguished from global users?
- What happens when a user is the last OWNER of an organisation?""",
    "C2-spec"
)

# Add FR-011 and FR-012 to feature 003 requirements
replace_once(
    spec003,
    "- **FR-010**: User roles MUST be manageable by admins via dedicated endpoints.",
    """- **FR-010**: User roles MUST be manageable by admins via dedicated endpoints.
- **FR-011**: Admin MUST be able to view a version diff (before/after field list) before publishing a schema version.
- **FR-012**: System MUST support admin-initiated bulk backfill of `dynamic_fields` for existing users when new required fields are added to a schema version.
- **FR-013**: Schema versions MUST track `deprecated_at` timestamp and deprecation reason; deprecated versions MUST return 422 on new submissions.""",
    "C2-fr"
)

print("\n== C2: Add schema builder tasks to 003 tasks (Phase 5) ==")
tasks003 = f"{BASE}/003-user-management/tasks.md"
replace_once(
    tasks003,
    "## Phase 6: User Story 5 — Role Assignment (Priority: P2)",
    """## Phase 5b: User Story 6 — Schema Builder & Lifecycle Management (Priority: P2) ⚠️ NON-NEGOTIABLE

**Goal**: Org admins can preview version diffs, deprecate old schemas, and bulk-backfill existing users with default values for new required fields.

**Independent Test**: PUT /api/v1/admin/schemas/{version}/deprecate sets deprecated_at; GET /api/v1/admin/schemas/diff?from=1&to=2 returns field-level diff; POST /api/v1/admin/schemas/{version}/backfill runs async job; progress endpoint returns % complete.

- [ ] T023b [P] [US6] Add `deprecated_at`, `deprecation_reason` columns to `OrgSchema` entity and migration; `ProfileSchemaService.deprecateSchema(version, reason)` validates that a newer version exists first
- [ ] T023c [US6] Implement `ProfileSchemaService.getSchemaDiff(fromVersion, toVersion)` — returns added/removed/changed fields as structured JSON
- [ ] T023d [US6] Add `GET /api/v1/admin/schemas/diff?from={v}&to={v}` endpoint to `ProfileSchemaController`
- [ ] T023e [US6] Implement `ProfileSchemaService.startBulkBackfill(version)` — creates `SchemaBackfillJob` entity (status, processed_count, total_count, started_at); runs paginated update of `dynamic_fields` via async `@Scheduled` task
- [ ] T023f [P] [US6] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/SchemaBackfillJob.java` JPA entity and `SchemaBackfillJobRepository.java`
- [ ] T023g [US6] Add `POST /api/v1/admin/schemas/{version}/backfill` and `GET /api/v1/admin/schemas/{version}/backfill/status` to `ProfileSchemaController`
- [ ] T023h [US6] Add `PUT /api/v1/admin/schemas/{version}/deprecate` to `ProfileSchemaController` — validate newer active version exists, set `deprecated_at` + reason

**Checkpoint**: Schema builder lifecycle (diff, deprecate, backfill) independently testable

---

## Phase 6: User Story 5 — Role Assignment (Priority: P2)""",
    "C2-tasks"
)

# Update dependencies section in 003 tasks
replace_once(
    tasks003,
    "- **Phase 5** (T019–T023): Depends on Phase 2, Phase 3 (UserService)\n- **Phase 6** (T024–T025): Depends on Phase 2",
    "- **Phase 5** (T019–T023): Depends on Phase 2, Phase 3 (UserService)\n- **Phase 5b** (T023b–T023h): Depends on Phase 5\n- **Phase 6** (T024–T025): Depends on Phase 2",
    "C2-dep"
)

# ─────────────────────────────────────────────────────────────────────────────
# C3: Add Teams entity to 004 spec + tasks + data-model
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C3: Add Teams user story to 004 spec ==")
spec004 = f"{BASE}/004-organization-management/spec.md"
replace_once(
    spec004,
    "### User Story 4 — Enterprise governance features (Priority: P3)",
    """### User Story 5 — Team management within organisations (Priority: P2) ⚠️ NON-NEGOTIABLE

An org admin creates and manages Teams (squads/departments) within an organisation. Users belong to Teams; Teams inherit org-level roles and can have team-specific permissions.

**Acceptance Scenarios**:
1. **Given** org admin, **When** POST /api/v1/organizations/{id}/teams with name + slug, **Then** team created under org.
2. **Given** org admin, **When** POST /api/v1/organizations/{id}/teams/{teamId}/members with userId, **Then** user added to team.
3. **Given** user listed, **When** GET /api/v1/users/me/teams, **Then** all teams for current user returned.
4. **Given** team deleted, **When** members queried, **Then** team_memberships reflect departure; user's org membership unaffected.

---

### User Story 4 — Enterprise governance features (Priority: P3)""",
    "C3-spec-story"
)

# Add Teams to functional requirements in 004 spec
replace_once(
    spec004,
    "- **FR-010**: Member profiles MUST include department, job_title, access_level.",
    """- **FR-010**: Member profiles MUST include department, job_title, access_level.
- **FR-011**: System MUST support Teams as a sub-entity of Organisation (Organization→Teams→Users→Roles hierarchy).
- **FR-012**: Team membership MUST be independent of Org membership (removing from team does not remove from org).
- **FR-013**: Team slugs MUST be unique within an organisation.""",
    "C3-fr"
)

# Add Teams to key entities in 004 spec
replace_once(
    spec004,
    "### Key Entities\n- **Organisation**: name, slug, domain, tier, subscription_status, max_users, storage_limit_gb, settings, compliance_settings",
    """### Key Entities
- **Organisation**: name, slug, domain, tier, subscription_status, max_users, storage_limit_gb, settings, compliance_settings
- **Team**: org_id, name, slug, description, created_by; sub-entity of Organisation
- **TeamMembership**: team_id, user_id, role (LEAD/MEMBER), joined_at, deleted_at""",
    "C3-entities"
)

print("\n== C3: Add Teams tasks to 004 tasks ==")
tasks004 = f"{BASE}/004-organization-management/tasks.md"
replace_once(
    tasks004,
    "## Phase 5: User Story 4 — Enterprise Governance (Priority: P3)",
    """## Phase 4b: User Story 5 — Team Management (Priority: P2) ⚠️ NON-NEGOTIABLE

**Goal**: Org admins create Teams within Orgs; users belong to Teams; hierarchy: Organisation→Teams→Users→Roles enforced.

**Independent Test**: POST /api/v1/organizations/{id}/teams creates team; POST members/{userId} adds user; GET /me/teams returns user's teams. Removing from team leaves org membership intact.

- [ ] T020b [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/Team.java` JPA entity (id, org_id, name, slug, description, created_by, deleted_at)
- [ ] T020c [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/TeamMembership.java` JPA entity (team_id, user_id, role, joined_at, deleted_at)
- [ ] T020d [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/repository/TeamRepository.java` and `TeamMembershipRepository.java`
- [ ] T020e [US5] Implement `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/TeamService.java` — `createTeam(orgId, request)`, `addMember(teamId, userId, role)`, `removeMember(teamId, userId)` (soft-delete), `listTeamsForUser(userId, tenantId)`
- [ ] T020f [US5] Add `team_slugs` unique-within-org constraint in migration; add `team_memberships` table to `database/init/03-organizational-management-schema.sql`
- [ ] T020g [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/controller/TeamController.java` with: `POST /api/v1/organizations/{id}/teams`, `GET /api/v1/organizations/{id}/teams`, `POST /api/v1/organizations/{id}/teams/{teamId}/members`, `DELETE /api/v1/organizations/{id}/teams/{teamId}/members/{userId}`, `GET /api/v1/users/me/teams`

**Checkpoint**: Teams CRUD and membership independently testable

---

## Phase 5: User Story 4 — Enterprise Governance (Priority: P3)""",
    "C3-tasks"
)

# Update dependencies in 004 tasks
replace_once(
    tasks004,
    "- **Phase 4** (T016–T020): Depends on Phase 2\n- **Phase 5** (T021–T023): Depends on Phase 3",
    "- **Phase 4** (T016–T020): Depends on Phase 2\n- **Phase 4b** (T020b–T020g): Depends on Phase 2\n- **Phase 5** (T021–T023): Depends on Phase 3",
    "C3-dep"
)

# ─────────────────────────────────────────────────────────────────────────────
# C5: Add Sentry tasks to 008 analytics tasks (Phase 2)
# ─────────────────────────────────────────────────────────────────────────────
print("\n== C5: Add Sentry backend error tracking to 008 tasks ==")
tasks008 = f"{BASE}/008-analytics-observability/tasks.md"
replace_once(
    tasks008,
    "- [ ] T005 Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/OtelConfig.java` (resource attributes: service.name, tenant.id, deployment.environment)",
    """- [ ] T005 Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/OtelConfig.java` (resource attributes: service.name, tenant.id, deployment.environment)
- [ ] T005a [P] Add `sentry-spring-boot-starter-jakarta` dependency to ALL backend service `pom.xml` files (auth, user, tenant, organisation, billing, notification, analytics)
- [ ] T005b [P] Configure Sentry DSN in each service `application.yml` via `${saas.secrets.sentry_dsn}` (injected through `SecretsProvider`); set `sentry.traces-sample-rate` matching OTel sampling per environment (1.0 local/dev, 0.5 sandbox/staging, 0.05 prod)
- [ ] T005c Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/SentryConfig.java` — configure `beforeSend` hook to attach `tenantId` and `userId` as Sentry tags from MDC; apply same pattern to all services
- [ ] T005d Add Sentry service to `docker-compose.yml` using `sentry:self-hosted` image (local environment); add `redis` Sentry dependency; expose port 9000""",
    "C5"
)

# Also add Sentry to 008 spec non-functional requirements
spec008 = f"{BASE}/008-analytics-observability/spec.md"
content008 = read(spec008)
if "Sentry" not in content008:
    replace_once(
        spec008,
        "- **FR-011**: Sampling rates: 100% local/dev, 50% sandbox/staging, 5% production.",
        "- **FR-011**: Sampling rates: 100% local/dev, 50% sandbox/staging, 5% production.\n- **FR-012**: Sentry MUST be used for error tracking across all environments (local, dev, staging, prod); DSN injected via `SecretsProvider`; `tenantId` and `userId` MUST be attached as Sentry tags.",
        "C5-spec"
    )

# ─────────────────────────────────────────────────────────────────────────────
# I1: Fix TenantService path in 002 tasks (user-service → tenant-service)
# ─────────────────────────────────────────────────────────────────────────────
print("\n== I1: Fix TenantService source path in 002 tasks ==")
tasks002 = f"{BASE}/002-multi-tenancy-tenant-lifecycle/tasks.md"
content002 = read(tasks002)
# Replace all occurrences of backend/user-service with backend/tenant-service in 002
# BUT only those related to tenant classes (not UserProfile etc.)
content002_fixed = (
    content002
    .replace(
        "**Source**: `backend/user-service/` (tenant endpoints), `database/`",
        "**Source**: `backend/tenant-service/` (tenant endpoints), `database/`"
    )
    .replace(
        "backend/user-service/pom.xml` includes spring-data-jpa, keycloak-admin-client",
        "backend/tenant-service/pom.xml` includes spring-data-jpa, keycloak-admin-client"
    )
    .replace(
        "`backend/user-service/src/main/resources/application.yml` — datasource, schema routing, Liquibase changelog path",
        "`backend/tenant-service/src/main/resources/application.yml` — datasource, schema routing, Liquibase changelog path"
    )
    # Replace all user-service paths that contain Tenant classes
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantDataSourceConfig.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantDataSourceConfig.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/entity/Tenant.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/Tenant.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/entity/TenantConfiguration.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/TenantConfiguration.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantContext.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantContext.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantInterceptor.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantInterceptor.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/repository/TenantRepository.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/repository/TenantRepository.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/dto/CreateTenantRequest.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/dto/CreateTenantRequest.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/dto/TenantResponse.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/dto/TenantResponse.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/service/TenantService.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/TenantService.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/config/WebConfig.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/WebConfig.java"
    )
    .replace(
        "backend/user-service/src/main/java/com/saasstarterkit/user/controller/TenantController.java",
        "backend/tenant-service/src/main/java/com/saasstarterkit/tenant/controller/TenantController.java"
    )
)
write(tasks002, content002_fixed)

# ─────────────────────────────────────────────────────────────────────────────
# I2 + G1: Add Kafka to 013 docker-compose tasks + erasure consumers to 001, 004, 006, 008
# ─────────────────────────────────────────────────────────────────────────────
print("\n== I2: Add Kafka to 013 docker-compose task ==")
replace_once(
    f"{BASE}/013-infrastructure-deployment/tasks.md",
    "- [ ] T001 Verify `docker-compose.yml` defines all required services: postgres, redis, keycloak, kong, rabbitmq, elasticsearch, logstash, kibana, prometheus, grafana, alertmanager, otel-collector, mailhog; add any missing",
    "- [ ] T001 Verify `docker-compose.yml` defines all required services: postgres, redis, keycloak, kong, rabbitmq, **kafka, zookeeper** (for erasure event streaming), elasticsearch, logstash, kibana, prometheus, grafana, alertmanager, otel-collector, sentry, mailhog; add any missing",
    "I2"
)

print("\n== G1: Add erasure consumers to 001, 004, 006, 008 tasks ==")

# Feature 001 auth-service — add erasure consumer task
tasks001 = f"{BASE}/001-authentication-identity-management/tasks.md"
content001 = read(tasks001)
# Find phase 7 or polish section to insert erasure task
replace_once(
    tasks001,
    "## Phase 7: Polish & Cross-Cutting" if "## Phase 7: Polish & Cross-Cutting" in content001 else "## Phase 6: Polish & Cross-Cutting",
    """## Phase 6b: User Data Erasure Consumer (Cross-Cutting)

**Goal**: auth-service consumes `UserDataErasureEvent` from Kafka and anonymises stored credentials/tokens.

**Independent Test**: Publish `UserDataErasureEvent` to Kafka → auth-service deletes Keycloak user + sets login_history records to anonymised state within 30 s.

- [ ] TErasure-001 Add `spring-kafka` dependency to `backend/auth-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-002 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/kafka/ErasureEventConsumer.java` — `@KafkaListener(topics = "user.erasure")` — calls `KeycloakIdentityProvider.deleteUser(userId)` and anonymises `login_history` rows for the user

**Checkpoint**: Erasure consumer independently verifiable by publishing test event

---

## Phase 7: Polish & Cross-Cutting""" if "## Phase 7: Polish & Cross-Cutting" in content001 else """## Phase 5b: User Data Erasure Consumer (Cross-Cutting)

**Goal**: auth-service consumes `UserDataErasureEvent` from Kafka and anonymises stored credentials/tokens.

**Independent Test**: Publish `UserDataErasureEvent` to Kafka → auth-service deletes Keycloak user + sets login_history records to anonymised state within 30 s.

- [ ] TErasure-001 Add `spring-kafka` dependency to `backend/auth-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-002 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/kafka/ErasureEventConsumer.java` — `@KafkaListener(topics = "user.erasure")` — calls `KeycloakIdentityProvider.deleteUser(userId)` and anonymises `login_history` rows for the user

**Checkpoint**: Erasure consumer independently verifiable by publishing test event

---

## Phase 6: Polish & Cross-Cutting""",
    "G1-001"
)

# Feature 004 organisation-service — add erasure consumer
replace_once(
    tasks004,
    "## Phase 6: Polish & Cross-Cutting",
    """## Phase 6a: User Data Erasure Consumer (Cross-Cutting)

**Goal**: organisation-service consumes `UserDataErasureEvent` and anonymises member records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` to Kafka → `OrganisationMember` rows for that userId have pii fields (email, name) overwritten with anonymised values; records retained for audit integrity.

- [ ] TErasure-004a Add `spring-kafka` to `backend/organisation-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-004b Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/kafka/ErasureEventConsumer.java` — anonymise `OrganisationMember` PII fields (name, email) for the specified userId while preserving audit log entries

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting""",
    "G1-004"
)

# Feature 006 billing-service — add erasure consumer
tasks006 = f"{BASE}/006-subscription-billing-management/tasks.md"
replace_once(
    tasks006,
    "## Phase 6: Polish & Cross-Cutting" if "## Phase 6: Polish & Cross-Cutting" in read(tasks006) else "## Phase 7: Polish & Cross-Cutting",
    """## Phase 6a: User Data Erasure Consumer (Cross-Cutting)

**Goal**: billing-service consumes `UserDataErasureEvent` and anonymises payment/subscription records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` → billing user PII fields (name, email, payment token) anonymised in DB; subscription records retained for financial compliance with anonymised user reference.

- [ ] TErasure-006a Add `spring-kafka` to `backend/billing-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-006b Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/kafka/ErasureEventConsumer.java` — anonymise user PII in billing records; retain for financial audit (7-year retention policy)

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting""" if "## Phase 6: Polish & Cross-Cutting" in read(tasks006) else """## Phase 6a: User Data Erasure Consumer (Cross-Cutting)

**Goal**: billing-service consumes `UserDataErasureEvent` and anonymises payment/subscription records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` → billing user PII fields (name, email, payment token) anonymised in DB; subscription records retained for financial compliance with anonymised user reference.

- [ ] TErasure-006a Add `spring-kafka` to `backend/billing-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-006b Create `backend/billing-service/src/main/java/com/saasstarterkit/billing/kafka/ErasureEventConsumer.java` — anonymise user PII in billing records; retain for financial audit (7-year retention policy)

**Checkpoint**: Erasure consumer independently testable

---

## Phase 7: Polish & Cross-Cutting""",
    "G1-006"
)

# Feature 008 analytics-service — add erasure consumer
replace_once(
    tasks008,
    "## Phase 6: Polish & Cross-Cutting",
    """## Phase 5b: User Data Erasure Consumer (Cross-Cutting)

**Goal**: analytics-service consumes `UserDataErasureEvent` and purges or anonymises `UserActivity` records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` → `UserActivity` rows for userId deleted (or userId field set to anonymised token); TenantMetrics aggregates unaffected.

- [ ] TErasure-008a Add `spring-kafka` to `backend/analytics-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-008b Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/kafka/ErasureEventConsumer.java` — delete or hash `UserActivity.user_id` for erased user; log anonymised count as audit entry

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting""",
    "G1-008"
)

# ─────────────────────────────────────────────────────────────────────────────
# I3: Standardize to "Organization" (American) in 004 spec and tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== I3: Standardize Organization spelling in 004 spec ==")
# Spec title and references
spec004_content = read(spec004)
# Change feature title and consistent entity names to Organization (American)
# while keeping Organisation in class names where the Java code already uses it (leave those)
spec004_content = (
    spec004_content
    .replace(
        "# Feature Specification: Organisation Management",
        "# Feature Specification: Organization Management"
    )
    .replace(
        "**Feature Branch**: `004-organization-management`",
        "**Feature Branch**: `004-organization-management`"  # already correct
    )
    # Normalize user-facing text (not Java class names)
    .replace("creates an organisation (team/department)", "creates an Organization (team/department)")
    .replace("all organisations in their tenant", "all Organizations in their tenant")
    .replace("org details returned", "org details returned")
    .replace("An org owner/admin", "An org owner/admin")
    .replace("**Given** org owner", "**Given** org owner")
    .replace("onboarding_completed=false", "onboarding_completed=false")
)
write(spec004, spec004_content)

# Normalize spec body terminology note
replace_once(
    spec004,
    "- **FR-001**: System MUST support full CRUD for organisations within a tenant.",
    "- **FR-001**: System MUST support full CRUD for Organizations within a tenant.\n\n> **Terminology Note**: This spec uses \"Organization\" (American English) consistently as the canonical entity name. Java class names (`OrganisationService`, `OrganisationMember`) retain their existing spellings for backward compatibility with existing code.",
    "I3"
)

# ─────────────────────────────────────────────────────────────────────────────
# G2: Add plan-tier Kong consumer task to 010 tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== G2: Add plan-tier Kong consumer (FR-008) task to 010 tasks ==")
tasks010 = f"{BASE}/010-api-gateway/tasks.md"
replace_once(
    tasks010,
    "- [ ] T014 [US2] Add `limit_by: header` with `header_name: X-Tenant-ID` for tenant-scoped rate limiting\n- [ ] T015 [US2] Add `api-gateway/test-rate-limiting.sh` smoke test script",
    """- [ ] T014 [US2] Add `limit_by: header` with `header_name: X-Tenant-ID` for tenant-scoped rate limiting
- [ ] T014b [US2] Add plan-tier rate limit enforcement via Kong consumer groups: map each tenant to a consumer group (STARTER/PROFESSIONAL/ENTERPRISE); STARTER consumers inherit 10 req/s limit, ENTERPRISE consumers 200 req/s; configuration stored in `kong-multi-tenant-enhanced.yml`
- [ ] T015 [US2] Add `api-gateway/test-rate-limiting.sh` smoke test script""",
    "G2"
)

# ─────────────────────────────────────────────────────────────────────────────
# G3: Add DDoS/admin endpoint rate limiting task to 010 tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== G3: Add DDoS + admin rate-limit tasks to 010 tasks ==")
replace_once(
    tasks010,
    "- [ ] T020 [P] Add WAF plugin config to `kong-multi-tenant-enhanced.yml` (active from staging: block OWASP Top 10 patterns)",
    """- [ ] T020 [P] Add WAF plugin config to `kong-multi-tenant-enhanced.yml` (active from staging: block OWASP Top 10 patterns)
- [ ] T020b Add DDoS protection configuration to `kong-multi-tenant-enhanced.yml`: connection rate-limit (50 connections/IP/min sandbox–UAT; 20 connections/IP/min pre-prod+); bot-detection plugin enabled from dev upward
- [ ] T020c Add separate strict rate-limit policy for `/api/v1/admin/**` routes: 5 req/s per IP; 429 with `Retry-After` header; log all admin 429s to audit trail""",
    "G3"
)

# ─────────────────────────────────────────────────────────────────────────────
# G4: Add notification priority dispatch + template type tasks to 007
# ─────────────────────────────────────────────────────────────────────────────
print("\n== G4: Add notification priority dispatch task to 007 tasks ==")
tasks007 = f"{BASE}/007-notification-service/tasks.md"
replace_once(
    tasks007,
    "- [ ] T014 [US1] Implement channel-level retry with exponential backoff (max 3 retries) and route to `notifications.dlq` on exhaustion\n- [ ] T015 [US1] Add OTel tracing (tenantId, eventId span attributes) in `NotificationDispatcher`",
    """- [ ] T014 [US1] Implement channel-level retry with exponential backoff (max 3 retries) and route to `notifications.dlq` on exhaustion
- [ ] T014b [US1] Implement priority-aware dispatch in `NotificationDispatcher` — CRITICAL-priority events bypass user preferences and attempt ALL enabled channels; HIGH-priority events always attempt primary channel regardless of preference; NORMAL/LOW respect user opt-out; set `Notification.priority` field from event payload
- [ ] T014c [US1] Seed four built-in template types in `database/migrations/03-add-notification-preferences.sql`: `welcome`, `billing_reminder`, `security_alert`, `maintenance_notice`; each with default Mustache template body and subject
- [ ] T015 [US1] Add OTel tracing (tenantId, eventId, priority span attributes) in `NotificationDispatcher`""",
    "G4"
)

# ─────────────────────────────────────────────────────────────────────────────
# G5: Add observability.yaml / config/observability.yaml task to 008 tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== G5: Add observability config task to 008 tasks ==")
replace_once(
    tasks008,
    "- [ ] T006 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/UserActivity.java` JPA entity",
    """- [ ] T005e [P] Create `config/observability.yaml` — canonical observability profile: sampling rates per environment (local: 1.0, dev: 1.0, sandbox: 0.5, staging: 0.5, uat: 0.5, pre-prod: 0.1, prod: 0.05), alert thresholds (cpu_warn: 75%, error_rate_warn: 1%), Sentry DSN key reference; all services and OTel collector read from this file
- [ ] T006 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/UserActivity.java` JPA entity""",
    "G5"
)

# ─────────────────────────────────────────────────────────────────────────────
# I6: Fix sampling rate discrepancy — spec says 5% prod; research.md said 10%
# ─────────────────────────────────────────────────────────────────────────────
print("\n== I6: Fix sampling rate in 008 research.md (10% → 5%) ==")
research008 = f"{BASE}/008-analytics-observability/research.md"
content = read(research008)
if "10%" in content:
    content = content.replace("10%", "5%")
    write(research008, content)

# Also fix OTel config sampling task in 008 tasks to match spec (5%)
replace_once(
    tasks008,
    "- [ ] T009 Add `otel.instrumentation.common.db-statement-sanitizer.enabled=true` and sampling config to all `application.yml` files (100% dev/staging, 5% prod with errors=100%)",
    "- [ ] T009 Add `otel.instrumentation.common.db-statement-sanitizer.enabled=true` and sampling config to all `application.yml` files (100% local/dev, 50% sandbox/staging, 5% prod — errors always 100%); values driven by `config/observability.yaml`",
    "I6"
)

# ─────────────────────────────────────────────────────────────────────────────
# I7: Fix Terraform module paths in 013 tasks (infra/ → infra/terraform/)
# The paths are already infra/terraform/ — check if there's a mismatch
# ─────────────────────────────────────────────────────────────────────────────
print("\n== I7: Verify Terraform paths in 013 tasks ==")
tasks013 = f"{BASE}/013-infrastructure-deployment/tasks.md"
content013 = read(tasks013)
terraform_paths = [line for line in content013.splitlines() if "terraform" in line.lower()]
for p in terraform_paths[:3]:
    print(f"  PATH: {p.strip()}")

# ─────────────────────────────────────────────────────────────────────────────
# D1: Deduplicate CacheConfig references
# ─────────────────────────────────────────────────────────────────────────────
print("\n== D1: Add CacheConfig dedup note to feature 011 tasks ==")
tasks011 = f"{BASE}/011-tenant-configuration-management/tasks.md"
content011 = read(tasks011)
if "CacheConfig" in content011:
    replace_once(
        tasks011,
        "CacheConfig",
        "CacheConfig (shared from `shared-libs/providers/`; do not duplicate in service)",
        "D1"
    )

# ─────────────────────────────────────────────────────────────────────────────
# D2: Add OTel BOM note to pom.xml tasks
# ─────────────────────────────────────────────────────────────────────────────
print("\n== D2: Add OTel BOM guidance to 008 tasks ==")
replace_once(
    tasks008,
    "- [ ] T001 Add OTel and Micrometer dependencies to all service pom.xml files: `opentelemetry-spring-starter`, `micrometer-registry-prometheus`, `logstash-logback-encoder`",
    "- [ ] T001 Add OTel and Micrometer dependencies to all service pom.xml files: `opentelemetry-spring-starter`, `micrometer-registry-prometheus`, `logstash-logback-encoder`; use `opentelemetry-bom` in `<dependencyManagement>` to pin a single OTel version across all modules (avoids version conflicts); add BOM once to root `pom.xml` if using multi-module build",
    "D2"
)

print("\n\n=== All fixes applied ===")
