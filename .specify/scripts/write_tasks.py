#!/usr/bin/env python3
"""Write tasks.md for all 13 features."""
import os, textwrap

SPECS_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "specs")

TASKS = {

# ─────────────────────────────────────────────────────────────────────────────
"001-authentication-identity-management": textwrap.dedent("""\
    # Tasks: Authentication & Identity Management

    **Feature**: `001-authentication-identity-management`
    **Input**: `specs/001-authentication-identity-management/`
    **Source**: `backend/auth-service/`

    ## Phase 1: Setup

    - [ ] T001 Add Spring Boot 3.x dependencies to `backend/auth-service/pom.xml` (spring-security, keycloak-admin-client, jjwt, spring-data-jpa, spring-web, micrometer-registry-prometheus, opentelemetry-spring-starter)
    - [ ] T002 [P] Configure `backend/auth-service/src/main/resources/application.yml` with Keycloak, PostgreSQL, Redis, and OTel settings
    - [ ] T003 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/SecurityConfig.java` (disable session, stateless JWT)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `database/init/01-init-schema.sql` auth schema tables: `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `refresh_tokens`, `audit_log`
    - [ ] T005 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/User.java` JPA entity
    - [ ] T006 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/Role.java` JPA entity
    - [ ] T007 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/KeycloakService.java` interface + impl (create realm, create client, create user)
    - [ ] T008 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/KeycloakConfig.java` (Keycloak admin client bean)
    - [ ] T009 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/repository/UserRepository.java` and `RoleRepository.java`
    - [ ] T010 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/util/JwtUtil.java` (generate + validate JWT using JJWT)

    **Checkpoint**: Foundation complete — all P1/P2/P3 stories can proceed

    ---

    ## Phase 3: User Story 1 + 2 — Registration & Login (Priority: P1) 🎯 MVP

    **Goal**: A user can register (creates Keycloak user + tenant schema) and login to receive JWT tokens.

    **Independent Test**: POST /api/v1/auth/register returns 201 with tokens; POST /api/v1/auth/login returns 200 with tokens; duplicate email returns 409.

    - [ ] T011 [P] [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/dto/RegisterRequest.java` and `RegisterResponse.java`
    - [ ] T012 [P] [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/dto/LoginRequest.java` and `TokenResponse.java`
    - [ ] T013 [US1] Implement `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/AuthService.java` — `register()` method: validate input, create Keycloak user, call `create_tenant_schema()`, persist User entity, return tokens
    - [ ] T014 [US1] Implement `AuthService.login()` — authenticate via Keycloak, issue JWT access + refresh tokens, persist refresh token hash
    - [ ] T015 [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/controller/AuthController.java` with `POST /api/v1/auth/register` and `POST /api/v1/auth/login`
    - [ ] T016 [US1] Add account lockout logic in `AuthService` (track failed attempts in `audit_log`, lock after configurable threshold)
    - [ ] T017 [US1] Add OTel span instrumentation and MDC logging (tenantId, correlationId) in `AuthService`

    **Checkpoint**: Registration and login independently testable

    ---

    ## Phase 4: User Story 3 — Token Refresh + Validation (Priority: P1)

    **Goal**: Clients can refresh expired access tokens and validate any token.

    **Independent Test**: POST /api/v1/auth/refresh with valid refresh token returns new access token; expired token returns 401. POST /api/v1/auth/validate returns valid/expired flags.

    - [ ] T018 [P] [US3] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/RefreshToken.java` entity and `RefreshTokenRepository.java`
    - [ ] T019 [US3] Implement `AuthService.refreshToken()` — verify token hash, check expiry, rotate refresh token, return new access token
    - [ ] T020 [US3] Implement `AuthService.validateToken()` — parse JWT claims, check expiry, return `TokenValidationResponse`
    - [ ] T021 [US3] Add `POST /api/v1/auth/refresh` and `POST /api/v1/auth/validate` to `AuthController`
    - [ ] T022 [US3] Add logout endpoint `POST /api/v1/auth/logout` — revoke refresh token in DB

    **Checkpoint**: Full token lifecycle independently testable

    ---

    ## Phase 5: User Story 4 + 5 — OAuth2 Social Login + Token Validation (Priority: P2)

    **Goal**: Users can authenticate via Google, Apple, or Microsoft OAuth2.

    **Independent Test**: Callback to `/api/v1/auth/login-success` creates session; `/api/v1/auth/login-failure` returns error.

    - [ ] T023 [P] [US4] Add `spring-security-oauth2-client` to `backend/auth-service/pom.xml`
    - [ ] T024 [P] [US4] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/OAuth2Config.java` (Google, Apple, Microsoft provider config)
    - [ ] T025 [US4] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/OAuthProvider.java` entity (user_id, provider, external_id)
    - [ ] T026 [US4] Implement `OAuth2AuthenticationSuccessHandler` — link provider identity to User, issue JWT tokens
    - [ ] T027 [US4] Implement `OAuth2AuthenticationFailureHandler` — return structured error response

    **Checkpoint**: OAuth2 social login independently testable

    ---

    ## Phase 6: User Story 6 — MFA Enrolment & Enforcement (Priority: P3)

    **Goal**: Users can enrol TOTP MFA; subsequent logins require the OTP code.

    **Independent Test**: POST /api/v1/auth/mfa/enroll returns QR code; login without OTP returns 401 requesting OTP; login with valid OTP returns 200.

    - [ ] T028 [P] [US6] Add `GoogleAuthenticator` TOTP library to `backend/auth-service/pom.xml`
    - [ ] T029 [US6] Implement `MfaService.java` in `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/` — enroll TOTP (generate secret, QR URI), verify code
    - [ ] T030 [US6] Update `AuthService.login()` — if `mfa_enabled=true`, require OTP code before issuing tokens
    - [ ] T031 [US6] Add `POST /api/v1/auth/mfa/enroll` and `POST /api/v1/auth/mfa/verify` to `AuthController`
    - [ ] T032 [US6] Update `User` entity — add `mfa_enabled`, `mfa_secret` (encrypted), `mfa_verified` fields

    **Checkpoint**: MFA enrolment and enforcement independently testable

    ---

    ## Phase 7: Polish & Cross-Cutting

    - [ ] T033 [P] Add Prometheus metrics in `AuthController` (login count, registration count, failure rate)
    - [ ] T034 [P] Add email verification flow: `POST /api/v1/auth/verify-email/{token}` in `AuthController`
    - [ ] T035 [P] Add password reset flow: `POST /api/v1/auth/password-reset/request` and `POST /api/v1/auth/password-reset/confirm`
    - [ ] T036 Run `quickstart.md` smoke tests against the running service

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T010): Depends on Phase 1
    - **Phase 3** (T011–T017): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T018–T022): Depends on Phase 2
    - **Phase 5** (T023–T027): Depends on Phase 2
    - **Phase 6** (T028–T032): Depends on Phase 3 (login flow)
    - **Phase 7** (T033–T036): Depends on all phases

    ### Parallel Opportunities per Story
    - T005, T006, T009 can run in parallel (different entity files)
    - T011, T012 can run in parallel (different DTOs)
    - T023, T024, T025 can run in parallel

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for a fully working registration and login.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"002-multi-tenancy-tenant-lifecycle": textwrap.dedent("""\
    # Tasks: Multi-Tenancy & Tenant Lifecycle Management

    **Feature**: `002-multi-tenancy-tenant-lifecycle`
    **Input**: `specs/002-multi-tenancy-tenant-lifecycle/`
    **Source**: `backend/user-service/` (tenant endpoints), `database/`

    ## Phase 1: Setup

    - [ ] T001 Add Liquibase and PostgreSQL JDBC dependencies to `backend/user-service/pom.xml`
    - [ ] T002 [P] Configure `backend/user-service/src/main/resources/application.yml` — datasource, schema routing, Liquibase changelog path
    - [ ] T003 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantDataSourceConfig.java` (schema-switching datasource)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `database/init/02-multi-tenant-schema.sql` — `tenant_management.tenants` table, `create_tenant_schema()` PL/pgSQL function that provisions `tenant_{slug}` schema with all required tables
    - [ ] T005 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/Tenant.java` JPA entity (id, name, slug, status, plan_id, keycloak_realm, schema_name)
    - [ ] T006 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/TenantConfiguration.java` JPA entity
    - [ ] T007 Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantContext.java` ThreadLocal holder
    - [ ] T008 Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/TenantInterceptor.java` — extract X-Tenant-ID header, populate TenantContext, set MDC + OTel span attribute
    - [ ] T009 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/repository/TenantRepository.java`

    **Checkpoint**: Tenant context propagation ready; all user stories can proceed

    ---

    ## Phase 3: User Story 1 + 5 — Tenant Provisioning & Context (Priority: P1) 🎯 MVP

    **Goal**: Registering creates a tenant schema; every request carries tenant context.

    **Independent Test**: Call `TenantService.createTenant()` → schema `tenant_{slug}` exists in PostgreSQL. Any request with X-Tenant-ID sets `TenantContext.getCurrentTenant()`.

    - [ ] T010 [P] [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/CreateTenantRequest.java` and `TenantResponse.java`
    - [ ] T011 [US1] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/TenantService.java` — `createTenant()`: persist Tenant entity, call `create_tenant_schema()` via native SQL, publish `TenantProvisionedEvent` to RabbitMQ
    - [ ] T012 [US5] Register `TenantInterceptor` in `WebMvcConfigurer` in `backend/user-service/src/main/java/com/saasstarterkit/user/config/WebConfig.java`
    - [ ] T013 [US5] Add OTel span attribute `tenant.id` and MDC field `tenantId` in `TenantInterceptor`
    - [ ] T014 [US5] Add guard: if no X-Tenant-ID and endpoint requires tenant context, return 400

    **Checkpoint**: Tenant provisioning and context propagation independently testable

    ---

    ## Phase 4: User Story 2 — Super-Admin Tenant List (Priority: P1)

    **Goal**: A super-admin can list all tenants with pagination and status filtering.

    **Independent Test**: GET /api/v1/tenants with SUPER_ADMIN token returns paginated list; non-SUPER_ADMIN returns 403.

    - [ ] T015 [P] [US2] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/TenantController.java`
    - [ ] T016 [US2] Implement `TenantService.listTenants(status, pageable)` with SUPER_ADMIN role guard
    - [ ] T017 [US2] Add `GET /api/v1/tenants` endpoint to `TenantController` with `@PreAuthorize("hasRole('SUPER_ADMIN')")`

    **Checkpoint**: Tenant listing independently testable

    ---

    ## Phase 5: User Story 3 + 4 — Tenant Lookup & Suspend/Delete (Priority: P2)

    **Goal**: Tenants can be retrieved by slug, suspended, or deleted.

    **Independent Test**: GET /api/v1/tenants/{slug} returns tenant or 404. DELETE /api/v1/tenants/{id} marks deleted. SUPER_ADMIN only.

    - [ ] T018 [P] [US3] Implement `TenantService.getTenantBySlug(slug)` — query by slug, return 404 if not found
    - [ ] T019 [P] [US4] Implement `TenantService.suspendTenant(id)` and `TenantService.deleteTenant(id)` (drop schema on delete)
    - [ ] T020 [US3] Add `GET /api/v1/tenants/{slugOrId}` to `TenantController`
    - [ ] T021 [US4] Add `PATCH /api/v1/tenants/{id}` (status update) and `DELETE /api/v1/tenants/{id}` to `TenantController`
    - [ ] T022 Add `GET /api/v1/tenants/check-slug?slug=X` slug availability endpoint to `TenantController`

    **Checkpoint**: Full tenant lifecycle independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T023 [P] Add Prometheus counter for tenant provisioning events in `TenantService`
    - [ ] T024 [P] Add Elasticsearch index routing — write `tenantId` field to all log events from `TenantInterceptor`
    - [ ] T025 Run `quickstart.md` smoke tests (create, list, lookup, suspend, delete)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T009): Depends on Phase 1
    - **Phase 3** (T010–T014): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T015–T017): Depends on Phase 2
    - **Phase 5** (T018–T022): Depends on Phase 2 (T009)
    - **Phase 6** (T023–T025): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working tenant provisioning and context.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"003-user-management": textwrap.dedent("""\
    # Tasks: User Management

    **Feature**: `003-user-management`
    **Input**: `specs/003-user-management/`
    **Source**: `backend/user-service/`

    ## Phase 1: Setup

    - [ ] T001 Verify `backend/user-service/pom.xml` includes spring-data-jpa, keycloak-admin-client, spring-amqp, opentelemetry-spring-starter
    - [ ] T002 [P] Configure `backend/user-service/src/main/resources/application.yml` — RabbitMQ, OTel, Keycloak admin settings
    - [ ] T003 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/RabbitMqConfig.java` (exchange, queue, binding declarations)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Add `users`, `user_roles` tables to `database/init/01-init-schema.sql` (or verify in existing migration) with `keycloak_user_id`, `dynamic_fields JSONB`
    - [ ] T005 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/UserProfile.java` JPA entity
    - [ ] T006 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/UserTenantRole.java` JPA entity
    - [ ] T007 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/repository/UserProfileRepository.java` and `UserTenantRoleRepository.java`
    - [ ] T008 Create `backend/user-service/src/main/java/com/saasstarterkit/user/service/KeycloakUserService.java` — adapter wrapping Keycloak Admin Client for user CRUD

    **Checkpoint**: User entity persistence layer ready

    ---

    ## Phase 3: User Story 1 + 2 — Admin User CRUD & Activation (Priority: P1) 🎯 MVP

    **Goal**: Admins can create, update, activate, deactivate, and delete users within their tenant.

    **Independent Test**: POST /api/v1/admin/users creates user in Keycloak + DB; DELETE removes; POST activate/deactivate toggles status. Returns 403 for non-ADMIN.

    - [ ] T009 [P] [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/CreateUserRequest.java`, `UpdateUserRequest.java`, `UserResponse.java`
    - [ ] T010 [US1] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/UserService.java` — `createUser()`: validate email uniqueness, create in Keycloak, persist `UserProfile`, publish `UserCreatedEvent`
    - [ ] T011 [US1] Implement `UserService.updateUser()`, `UserService.deleteUser()` (soft-delete status=DEACTIVATED)
    - [ ] T012 [US2] Implement `UserService.activateUser()` and `UserService.deactivateUser()` — also sync status to Keycloak
    - [ ] T013 [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/AdminUserController.java` with `POST`, `GET`, `PATCH`, `DELETE /api/v1/admin/users` and `POST /api/v1/admin/users/{id}/activate`, `/deactivate`
    - [ ] T014 [US1] Add `@PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_ADMIN')")` to all `AdminUserController` methods
    - [ ] T015 [US1] Add OTel tracing and MDC logging (tenantId, userId, correlationId) in `UserService`

    **Checkpoint**: Admin user CRUD independently testable

    ---

    ## Phase 4: User Story 3 — User Profile Self-Service (Priority: P2)

    **Goal**: Authenticated users can view and update their own profile (avatar, bio, timezone, language, preferences).

    **Independent Test**: GET /api/v1/users/profile returns profile; PUT /api/v1/users/profile updates correctly.

    - [ ] T016 [P] [US3] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/UserProfileUpdateRequest.java`
    - [ ] T017 [US3] Implement `UserService.getMyProfile(userId)` and `UserService.updateMyProfile(userId, request)`
    - [ ] T018 [US3] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/UserProfileController.java` with `GET /api/v1/users/profile` and `PUT /api/v1/users/profile`

    **Checkpoint**: Profile self-service independently testable

    ---

    ## Phase 5: User Story 4 — Dynamic Profile Schema (Priority: P2) ⚠️ NON-NEGOTIABLE

    **Goal**: Org admins define custom user attribute schemas (immutable versioning). Users store values in JSONB. Frontend renders forms from schema.

    **Independent Test**: POST /api/v1/admin/schemas creates versioned schema; GET /api/v1/admin/schemas/active returns active schema with fields; existing user data preserved on schema change.

    - [ ] T019 [P] [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/OrgSchema.java` entity (id, tenant_id, version, status, fields JSONB)
    - [ ] T020 [P] [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/repository/OrgSchemaRepository.java`
    - [ ] T021 [US4] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/ProfileSchemaService.java` — `publishSchema()` (increment version, deprecate previous), `getActiveSchema()`, `validateProfileValues(schema, values)`
    - [ ] T022 [US4] Update `UserService.createUser()` and `updateMyProfile()` to validate `dynamic_fields` JSONB against active schema
    - [ ] T023 [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/ProfileSchemaController.java` with `POST /api/v1/admin/schemas`, `GET /api/v1/admin/schemas/active`, `GET /api/v1/admin/schemas/{version}`

    **Checkpoint**: Dynamic profile schema independently testable with versioning verified

    ---

    ## Phase 6: User Story 5 — Role Assignment (Priority: P2)

    **Goal**: Admins can list, assign, and revoke roles for users in their tenant.

    **Independent Test**: POST /api/v1/admin/users/{id}/roles assigns role; DELETE revokes. GET /api/v1/admin/roles lists tenant roles.

    - [ ] T024 [P] [US5] Implement `UserService.assignRole(userId, roleId)` and `UserService.revokeRole(userId, roleId)` — persist `UserTenantRole`, sync to Keycloak
    - [ ] T025 [US5] Add `GET /api/v1/admin/roles`, `POST /api/v1/admin/users/{id}/roles`, `DELETE /api/v1/admin/users/{id}/roles/{role}` to `AdminUserController`

    **Checkpoint**: Role assignment independently testable

    ---

    ## Phase 7: Polish & Cross-Cutting

    - [ ] T026 [P] Add Prometheus metrics for user creation/deletion counts in `UserService`
    - [ ] T027 [P] Add RabbitMQ dead-letter handling for `UserCreatedEvent` publishes
    - [ ] T028 Run `quickstart.md` smoke tests (create, activate, deactivate, profile update, schema)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T015): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T016–T018): Depends on Phase 2
    - **Phase 5** (T019–T023): Depends on Phase 2, Phase 3 (UserService)
    - **Phase 6** (T024–T025): Depends on Phase 2
    - **Phase 7** (T026–T028): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working admin user CRUD.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"004-organization-management": textwrap.dedent("""\
    # Tasks: Organisation Management

    **Feature**: `004-organization-management`
    **Input**: `specs/004-organization-management/`
    **Source**: `backend/organisation-service/`

    ## Phase 1: Setup

    - [ ] T001 Verify/add dependencies in `backend/organisation-service/pom.xml` (spring-data-jpa, spring-amqp, spring-web, opentelemetry-spring-starter)
    - [ ] T002 [P] Configure `backend/organisation-service/src/main/resources/application.yml` (datasource, RabbitMQ, OTel)
    - [ ] T003 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/config/RabbitMqConfig.java`

    ---

    ## Phase 2: Foundational

    - [ ] T004 Verify `database/init/03-organizational-management-schema.sql` has `organizations`, `organization_members`, `org_audit_log` tables; add if missing
    - [ ] T005 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/Organisation.java` JPA entity (id, tenant_id, name, slug, tier, status, max_users, soft-delete fields, onboarding fields)
    - [ ] T006 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/OrganisationMember.java` JPA entity (org_id, user_id, role, status, department, job_title, invited_by, accepted_at, deleted_at)
    - [ ] T007 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/OrgAuditLog.java` JPA entity
    - [ ] T008 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/repository/OrganisationRepository.java` and `OrganisationMemberRepository.java`
    - [ ] T009 Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/OrgAuditService.java` — async audit log writes via `@Async` to avoid adding latency

    **Checkpoint**: Entity + audit foundation ready

    ---

    ## Phase 3: User Story 1 + 2 — Org CRUD & Listing (Priority: P1) 🎯 MVP

    **Goal**: Users can create and list organisations within their tenant.

    **Independent Test**: POST /api/v1/organizations creates org under tenant schema; GET lists all; duplicate slug returns unique constraint error; missing X-Tenant-ID returns 400.

    - [ ] T010 [P] [US1] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/dto/CreateOrgRequest.java`, `UpdateOrgRequest.java`, `OrgResponse.java`
    - [ ] T011 [US1] Implement `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/OrganisationService.java` — `createOrg()`: validate tenant context, check slug uniqueness, persist, write audit log async
    - [ ] T012 [US1] Implement `OrganisationService.updateOrg()` and `OrganisationService.archiveOrg()` (soft-delete: set deleted_at)
    - [ ] T013 [US2] Implement `OrganisationService.listOrgs(tenantId)` and `OrganisationService.getOrg(id)` — exclude soft-deleted
    - [ ] T014 [US1] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/controller/OrganisationController.java` with `POST`, `GET`, `PATCH`, `DELETE /api/v1/organizations` and `GET /api/v1/organizations/{id}`
    - [ ] T015 [US1] Add OTel tracing and MDC tenantId logging in `OrganisationService`

    **Checkpoint**: Org CRUD independently testable

    ---

    ## Phase 4: User Story 3 — Member Management (Priority: P2)

    **Goal**: Org owners can add/remove members with role assignment and invite tracking.

    **Independent Test**: POST /api/v1/organizations/{id}/members adds member with invited_by + invited_at; DELETE soft-deletes; accepted_at set on acceptance.

    - [ ] T016 [P] [US3] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/dto/AddMemberRequest.java` and `MemberResponse.java`
    - [ ] T017 [US3] Implement `OrganisationService.addMember(orgId, request)` — create `OrganisationMember`, write audit log, publish `MemberAddedEvent`
    - [ ] T018 [US3] Implement `OrganisationService.removeMember(orgId, userId)` — soft-delete (set deleted_at), write audit log
    - [ ] T019 [US3] Implement `OrganisationService.acceptInvite(orgId, userId)` — set accepted_at timestamp
    - [ ] T020 [US3] Add `GET`, `POST /api/v1/organizations/{id}/members` and `DELETE /api/v1/organizations/{id}/members/{userId}` to `OrganisationController`

    **Checkpoint**: Member management independently testable

    ---

    ## Phase 5: User Story 4 — Enterprise Governance (Priority: P3)

    **Goal**: Super-admin can enforce ENTERPRISE tier limits (custom max_users, api_rate_limit) and view onboarding status.

    **Independent Test**: ENTERPRISE org enforces max_users limit; onboarding_step returned when onboarding_completed=false.

    - [ ] T021 [P] [US4] Implement `OrganisationService.enforceUsageLimits(orgId)` — check max_users, storage_limit_gb, api_rate_limit against current usage
    - [ ] T022 [US4] Add `onboarding_completed`, `onboarding_step` fields to `Organisation` entity (update migration)
    - [ ] T023 [US4] Add `GET /api/v1/admin/organizations` (SUPER_ADMIN only, cross-tenant health view) to a new `AdminOrgController`

    **Checkpoint**: Enterprise governance independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T024 [P] Add indexes on `organizations(tenant_id, deleted_at)` WHERE deleted_at IS NULL in migration
    - [ ] T025 [P] Add Prometheus counter for org creation, member add/remove events
    - [ ] T026 Run `quickstart.md` smoke tests (create, list, add member, remove member, archive)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T009): Depends on Phase 1
    - **Phase 3** (T010–T015): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T016–T020): Depends on Phase 2
    - **Phase 5** (T021–T023): Depends on Phase 3
    - **Phase 6** (T024–T026): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working org creation and listing.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"005-role-based-access-control": textwrap.dedent("""\
    # Tasks: Role-Based Access Control (RBAC)

    **Feature**: `005-role-based-access-control`
    **Input**: `specs/005-role-based-access-control/`
    **Source**: `backend/user-service/` (roles domain)

    ## Phase 1: Setup

    - [ ] T001 Add `spring-boot-starter-cache`, `spring-data-redis` to `backend/user-service/pom.xml`
    - [ ] T002 [P] Configure Redis and cache settings in `backend/user-service/src/main/resources/application.yml`
    - [ ] T003 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/CacheConfig.java` — Spring Cache + Redis, TTL 300 s for permission cache

    ---

    ## Phase 2: Foundational

    - [ ] T004 Add `roles`, `permissions`, `user_roles`, `role_permissions` tables to `database/init/01-init-schema.sql` (or verify); seed system roles: SUPER_ADMIN, ADMIN, TENANT_ADMIN, USER
    - [ ] T005 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/Role.java` and `Permission.java` JPA entities
    - [ ] T006 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/RolePermission.java` join entity
    - [ ] T007 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/repository/RoleRepository.java` and `PermissionRepository.java`
    - [ ] T008 Create `backend/user-service/src/main/java/com/saasstarterkit/user/service/PermissionService.java` — `getPermissionsForUser(userId, tenantId)` with `@Cacheable("permissions")`

    **Checkpoint**: RBAC entities + cached permission lookup ready

    ---

    ## Phase 3: User Story 1 + 2 — Role Assignment & Endpoint Enforcement (Priority: P1) 🎯 MVP

    **Goal**: Admins assign/revoke roles; `@PreAuthorize` guards protect every endpoint.

    **Independent Test**: SUPER_ADMIN GET /api/v1/tenants returns 200; USER role gets 403. POST /api/v1/admin/users/{id}/roles assigns role; DELETE revokes.

    - [ ] T009 [P] [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/AssignRoleRequest.java`
    - [ ] T010 [US1] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/RoleService.java` — `listRoles(tenantId)`, `assignRole(userId, roleId, tenantId)`, `revokeRole(userId, roleId, tenantId)`; sync role to Keycloak realm
    - [ ] T011 [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/RoleController.java` with `GET /api/v1/admin/roles`, `POST /api/v1/admin/users/{id}/roles`, `DELETE /api/v1/admin/users/{id}/roles/{role}`
    - [ ] T012 [US2] Add `@PreAuthorize` annotations to all existing controllers in `user-service` and verify 403 on unauthorised access
    - [ ] T013 [US1] Publish `RoleAssignedEvent` / `RoleRevokedEvent` to RabbitMQ on role change for cache invalidation in `RoleService`

    **Checkpoint**: Role listing, assignment, and endpoint guards independently testable

    ---

    ## Phase 4: User Story 3 — Permission-Level Access Checks (Priority: P2)

    **Goal**: Fine-grained `resource:action` permissions gate individual operations beyond role-level checks.

    **Independent Test**: Role with `billing:read` allows GET billing endpoint; without `billing:write` POST billing returns 403.

    - [ ] T014 [P] [US3] Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/PermissionEvaluator.java` implementing `hasPermission(authentication, resource, action)`
    - [ ] T015 [US3] Add permission seeds to `database/init/01-init-schema.sql` — all `resource:action` pairs
    - [ ] T016 [US3] Update `RolePermission` entity; add `@PreAuthorize("hasPermission('billing','read')")` to billing-adjacent controllers
    - [ ] T017 [US3] Add `@CacheEvict(value="permissions", key="#userId+':'+#tenantId")` to `RoleService.assignRole()` and `revokeRole()`

    **Checkpoint**: Permission-level access independently testable

    ---

    ## Phase 5: User Story 4 — OPA East-West Policy Enforcement (Priority: P3)

    **Goal**: Internal service-to-service calls are authorised via OPA policies.

    **Independent Test**: OPA server loaded with RBAC policy; inter-service call with valid workload identity returns permit; unauthorised call returns deny.

    - [ ] T018 [P] [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/OpaClient.java` — HTTP client to OPA REST API
    - [ ] T019 [US4] Create OPA policy file `infrastructure/opa/policies/rbac.rego` — `allow` if user has required permission in RBAC DB
    - [ ] T020 [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/config/OpaInterceptor.java` — applies to East-West (non-Kong) calls only
    - [ ] T021 [US4] Add OPA server service to `docker-compose.yml`

    **Checkpoint**: OPA East-West enforcement independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T022 [P] Add Prometheus counter for permission cache hits vs misses in `PermissionService`
    - [ ] T023 [P] Publish `PermissionChangedEvent` on role_permission update to invalidate all Caches
    - [ ] T024 Run `quickstart.md` smoke tests (assign role, verify access, verify denial, verify cache)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T013): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T014–T017): Depends on Phase 2
    - **Phase 5** (T018–T021): Depends on Phase 3
    - **Phase 6** (T022–T024): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working role assignment and endpoint enforcement.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"006-subscription-billing-management": textwrap.dedent("""\
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
"""),

# ─────────────────────────────────────────────────────────────────────────────
"007-notification-service": textwrap.dedent("""\
    # Tasks: Notification Service

    **Feature**: `007-notification-service`
    **Input**: `specs/007-notification-service/`
    **Source**: `backend/notification-service/`

    ## Phase 1: Setup

    - [ ] T001 Add dependencies to `backend/notification-service/pom.xml` (spring-amqp, spring-data-jpa, mustache, sendgrid-java, twilio-java, firebase-admin, opentelemetry-spring-starter)
    - [ ] T002 [P] Configure `backend/notification-service/src/main/resources/application.yml` (RabbitMQ, SMTP, SendGrid, Twilio, FCM settings, DLQ)
    - [ ] T003 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/EmailProvider.java` interface and `SmsProvider.java` interface

    ---

    ## Phase 2: Foundational

    - [ ] T004 Add `notifications`, `notification_templates`, `user_notification_preferences` tables to `database/migrations/03-add-notification-preferences.sql`
    - [ ] T005 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/entity/Notification.java`, `NotificationTemplate.java`, `UserNotificationPreferences.java` JPA entities
    - [ ] T006 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/repository/NotificationRepository.java`, `NotificationTemplateRepository.java`, `UserNotificationPreferencesRepository.java`
    - [ ] T007 Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/ChannelProviderRegistry.java` — maps channel → active provider; configurable via properties
    - [ ] T008 Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/TemplateRenderService.java` — renders Mustache templates with variable map

    **Checkpoint**: Notification entities, provider registry, and template engine ready

    ---

    ## Phase 3: User Story 1 + 4 — Event-Driven Dispatch & Multi-Channel (Priority: P1) 🎯 MVP

    **Goal**: RabbitMQ event consumed → preferences checked → template rendered → delivered to all enabled channels independently.

    **Independent Test**: Publish event to `notification.queue`; email channel receives rendered email; SMS not sent if user opted out; delivery status=SENT stored in DB.

    - [ ] T009 [P] [US1] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/NotificationEvent.java` (eventId, eventType, userId, tenantId, templateKey, variables)
    - [ ] T010 [US1] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/NotificationDispatcher.java` — listens on `notification.queue`, resolves user preferences, selects channels, delegates to channel handlers
    - [ ] T011 [US4] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/EmailNotificationService.java` — render template, call `EmailProvider`, persist delivery status
    - [ ] T012 [US4] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/SmsNotificationService.java` — same pattern for SMS channel
    - [ ] T013 [US4] Implement `InAppNotificationService.java` in same package — persist in-app notification, no external call
    - [ ] T014 [US1] Implement channel-level retry with exponential backoff (max 3 retries) and route to `notifications.dlq` on exhaustion
    - [ ] T015 [US1] Add OTel tracing (tenantId, eventId span attributes) in `NotificationDispatcher`

    **Checkpoint**: Event-driven multi-channel dispatch independently testable

    ---

    ## Phase 4: User Story 2 — Notification Preferences (Priority: P1)

    **Goal**: Users can set per-channel opt-in/out preferences.

    **Independent Test**: PUT /api/v1/notifications/preferences disables SMS; next SMS event for that user is SKIPPED. Preference record persisted with tenantId.

    - [ ] T016 [P] [US2] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/UpdatePreferencesRequest.java`
    - [ ] T017 [US2] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/PreferencesService.java` — `getPreferences(userId, tenantId)`, `updatePreferences()` with default-true for missing records
    - [ ] T018 [US2] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationPreferencesController.java` with `GET` and `PUT /api/v1/notifications/preferences`

    **Checkpoint**: User preferences independently testable

    ---

    ## Phase 5: User Story 3 + 5 — Template Management & History (Priority: P2)

    **Goal**: Admins manage templates with Mustache syntax validation; admins view delivery history with retry.

    **Independent Test**: POST /api/v1/admin/notification-templates creates template; invalid Mustache returns 400. GET /api/v1/admin/notifications?status=FAILED filters correctly.

    - [ ] T019 [P] [US3] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/CreateTemplateRequest.java`
    - [ ] T020 [US3] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/TemplateService.java` — `createTemplate()` with Mustache syntax validation, `updateTemplate()`, `deleteTemplate()`
    - [ ] T021 [US3] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationTemplateController.java` with full CRUD under `/api/v1/admin/notification-templates`
    - [ ] T022 [US5] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/NotificationHistoryService.java` — `listNotifications(tenantId, status, channel, dateRange, pageable)`, `retryNotification(notificationId)`
    - [ ] T023 [US5] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationHistoryController.java` with `GET /api/v1/admin/notifications` and `POST /api/v1/admin/notifications/{id}/retry`

    **Checkpoint**: Template management and history independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T024 [P] Implement `SendGridEmailProvider.java` and `TwilioSmsProvider.java` in `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/impl/`
    - [ ] T025 [P] Add Prometheus counters for notifications sent/failed per channel in `NotificationDispatcher`
    - [ ] T026 Run `quickstart.md` smoke tests (publish event, verify delivery, check preferences, template CRUD)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T015): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T016–T018): Depends on Phase 2
    - **Phase 5** (T019–T023): Depends on Phase 2, Phase 3
    - **Phase 6** (T024–T026): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working event-driven notification dispatch.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"008-analytics-observability": textwrap.dedent("""\
    # Tasks: Analytics & Observability

    **Feature**: `008-analytics-observability`
    **Input**: `specs/008-analytics-observability/`
    **Source**: `backend/analytics-service/`, `infrastructure/`

    ## Phase 1: Setup

    - [ ] T001 Add OTel and Micrometer dependencies to all service pom.xml files: `opentelemetry-spring-starter`, `micrometer-registry-prometheus`, `logstash-logback-encoder`
    - [ ] T002 [P] Configure `infrastructure/otel-collector/otel-collector-config.yaml` (OTLP receivers, Prometheus exporter, Jaeger exporter)
    - [ ] T003 [P] Configure `infrastructure/prometheus/prometheus.yml` (scrape configs for all services with per-service job labels)
    - [ ] T004 [P] Configure `infrastructure/logstash/pipeline/` (parse JSON logs, add tenantId, route to tenant-scoped ES index `saas-logs-{tenant_id}-*`)

    ---

    ## Phase 2: Foundational

    - [ ] T005 Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/OtelConfig.java` (resource attributes: service.name, tenant.id, deployment.environment)
    - [ ] T006 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/UserActivity.java` JPA entity (user_id, tenant_id, action, metadata JSONB, timestamp)
    - [ ] T007 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/TenantMetrics.java` JPA entity (tenant_id, period_start, period_end, api_calls, storage_gb, active_users)
    - [ ] T008 [P] Create repositories: `UserActivityRepository.java`, `TenantMetricsRepository.java`
    - [ ] T009 Add `otel.instrumentation.common.db-statement-sanitizer.enabled=true` and sampling config to all `application.yml` files (100% dev/staging, 5% prod with errors=100%)

    **Checkpoint**: OTel pipelines, Prometheus, and Logstash ready for all services

    ---

    ## Phase 3: User Story 1 + 2 — Service Health & Distributed Tracing (Priority: P1) 🎯 MVP

    **Goal**: All services expose health + RED metrics scraped by Prometheus; every cross-service request carries a single trace ID with tenant.id span attribute.

    **Independent Test**: `curl /actuator/health` on every service returns UP. Prometheus scrapes metrics with `tenant_id` label. Single request generates linked spans across auth → user → notification.

    - [ ] T010 [P] [US1] Add `spring-boot-actuator` with `/actuator/health`, `/actuator/prometheus` endpoints to all service `application.yml` and `pom.xml`
    - [ ] T011 [P] [US1] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/metrics/ServiceMetrics.java` — define standard counters: `saas_http_requests_total`, `saas_http_request_duration_seconds` histogram
    - [ ] T012 [US2] Add `W3CTraceContextPropagator` as default propagator in all service `OtelConfig.java` files
    - [ ] T013 [US2] Add `tenant.id` as span attribute via `TenantContext` in all service OTel filters (read from `TenantInterceptor`)
    - [ ] T014 [US1] Configure `infrastructure/alertmanager/alertmanager.yml` — routes: dev/staging → Slack, prod → PagerDuty
    - [ ] T015 [US1] Add Prometheus alert rules file `infrastructure/prometheus/alerts.yaml` (CPU > 75%, error_rate > 1%, service down)

    **Checkpoint**: Health, metrics, and distributed tracing independently verifiable

    ---

    ## Phase 4: User Story 3 — Centralised Log Aggregation (Priority: P2)

    **Goal**: All service logs land in Elasticsearch with tenantId field; Kibana filters by tenant.

    **Independent Test**: Emit log in any service → query ES `saas-logs-*` index → document has `tenant_id`. Kibana filter by tenantId returns only that tenant's logs.

    - [ ] T016 [P] [US3] Add `logstash-logback-encoder` and `logback-spring.xml` with JSON layout to each service's `src/main/resources/`
    - [ ] T017 [US3] Configure `infrastructure/logstash/config/logstash.yml` and pipeline to add `tenant_id` field from log event
    - [ ] T018 [US3] Set Kibana index pattern and saved search in `infrastructure/kibana/tenant-rbac-config.yml`

    **Checkpoint**: Log aggregation independently testable

    ---

    ## Phase 5: User Story 4 + 5 — Usage Analytics & Alert Routing (Priority: P2)

    **Goal**: analytics-service records per-tenant usage; Alertmanager routes by severity and environment.

    **Independent Test**: RabbitMQ feature usage event → `UserActivity` row exists in DB. TenantMetrics aggregated for billing period. Error alert in prod → PagerDuty; same in dev → Slack only.

    - [ ] T019 [P] [US4] Implement `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/service/AnalyticsService.java` — `recordActivity(event)`, `computeTenantMetrics(tenantId, period)`
    - [ ] T020 [US4] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/listener/FeatureUsageEventListener.java` — RabbitMQ consumer for `analytics.feature.usage` queue
    - [ ] T021 [US4] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/controller/AnalyticsController.java` with `GET /api/v1/analytics/tenants/{tenantId}/usage`
    - [ ] T022 [US5] Update `infrastructure/alertmanager/alertmanager.yml` with environment-based inhibit rules and receiver grouping

    **Checkpoint**: Usage analytics and alert routing independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T023 [P] Create Grafana dashboard provisioning files in `infrastructure/grafana/dashboards/` (service health, tenant usage)
    - [ ] T024 [P] Add OTel sampling config to `infrastructure/otel-collector/otel-collector-config.yaml` — tail sampling: always-on for errors
    - [ ] T025 Run `quickstart.md` smoke tests (health endpoints, trace propagation, ES log ingestion, metrics scrape)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T004): No dependencies
    - **Phase 2** (T005–T009): Depends on Phase 1
    - **Phase 3** (T010–T015): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T016–T018): Depends on Phase 2
    - **Phase 5** (T019–T022): Depends on Phase 2
    - **Phase 6** (T023–T025): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working health monitoring and distributed tracing.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"009-security-compliance": textwrap.dedent("""\
    # Tasks: Security & Compliance

    **Feature**: `009-security-compliance`
    **Input**: `specs/009-security-compliance/`
    **Source**: `backend/user-service/` (compliance domain)

    ## Phase 1: Setup

    - [ ] T001 Add dependencies to `backend/user-service/pom.xml` (vault-spring-starter, spring-kafka, jasypt-spring-boot)
    - [ ] T002 [P] Configure `backend/user-service/src/main/resources/application.yml` — Vault address/token, Kafka brokers, AES key path
    - [ ] T003 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/SecretsProvider.java` interface + `VaultSecretsProvider.java` impl (no hardcoded secrets)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `database/migrations/05-gdpr-hipaa-compliance.sql` — `data_consents`, `data_subject_requests`, `protected_health_information`, `security_alerts`, `compliance_checks` tables; add `is_encrypted` and `data_hash` columns
    - [ ] T005 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/DataConsent.java`, `DataSubjectRequest.java`, `SecurityAlert.java`, `ComplianceCheck.java`, `ProtectedHealthInformation.java` JPA entities
    - [ ] T006 [P] Create repositories for each compliance entity
    - [ ] T007 Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/AesEncryptionService.java` — AES-256-GCM encrypt/decrypt; key fetched from `SecretsProvider`
    - [ ] T008 Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/ImmutableAuditLogger.java` — append-only write to `security_alerts` table with separate write-only DB user

    **Checkpoint**: Compliance entities, encryption, and immutable audit ready

    ---

    ## Phase 3: User Story 1 + 4 — GDPR Consent & Security Monitoring (Priority: P1) 🎯 MVP

    **Goal**: User manages consent; system detects brute force and raises scored security alerts.

    **Independent Test**: New user → ESSENTIAL consent auto-created. Withdraw MARKETING consent → status=WITHDRAWN, timestamp recorded. 5+ failed logins from same IP → HIGH alert in security_alerts.

    - [ ] T009 [P] [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/ConsentUpdateRequest.java`
    - [ ] T010 [US1] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/ConsentService.java` — `initializeConsents(userId)` on registration, `updateConsent(userId, type, consented)`, `getConsents(userId)`
    - [ ] T011 [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/ConsentController.java` with `GET /api/v1/users/{id}/consent` and `PUT /api/v1/users/{id}/consent`
    - [ ] T012 [US4] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/security/SecurityMonitoringService.java` — `detectBruteForce(userId, ip)`, `detectAnomalousApiUsage(tenantId, endpoint)`, `createSecurityAlert(type, severity, riskScore)`
    - [ ] T013 [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/SecurityAlertController.java` with `GET /api/v1/admin/security/alerts` (filterable by severity, status)

    **Checkpoint**: GDPR consent and basic security monitoring independently testable

    ---

    ## Phase 4: User Story 2 — Data Subject Rights (Priority: P1)

    **Goal**: Users submit ACCESS, ERASURE, or PORTABILITY requests; erasure saga notifies all services.

    **Independent Test**: POST /api/v1/users/{id}/data-requests with type=ERASURE → request created, `UserDataErasureEvent` published to Kafka. PORTABILITY request generates JSON export.

    - [ ] T014 [P] [US2] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/DataSubjectRequestDto.java`
    - [ ] T015 [US2] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/DataSubjectRequestService.java` — `submitRequest()`, `processErasure()` (publish `UserDataErasureEvent` to Kafka), `processPortability()` (generate JSON/CSV export)
    - [ ] T016 [US2] Create `backend/user-service/src/main/java/com/saasstarterkit/user/kafka/ErasureEventProducer.java` — publish `UserDataErasureEvent`
    - [ ] T017 [US2] Add `POST /api/v1/users/{id}/data-requests` and `GET /api/v1/users/{id}/data-requests` to a new `DataSubjectRequestController.java`

    **Checkpoint**: Data subject rights independently testable

    ---

    ## Phase 5: User Story 3 + 5 — HIPAA PHI & Compliance Dashboard (Priority: P2)

    **Goal**: HIPAA tenants store encrypted PHI with access audit; admins view compliance scores.

    **Independent Test**: PHI record created with is_encrypted=true, data_hash stored. PHI read → audit log entry. Compliance endpoint returns score per framework.

    - [ ] T018 [P] [US3] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/PhiService.java` — `storePhi(userId, category, data)` (encrypt + hash), `readPhi(userId, category, purpose)` (decrypt + audit log)
    - [ ] T019 [US3] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/PhiController.java` with `POST` and `GET /api/v1/users/{id}/phi` (HIPAA-enabled tenants only)
    - [ ] T020 [US5] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/ComplianceService.java` — compute score per framework (GDPR, HIPAA, SOX, PCI-DSS) based on fulfilled checks
    - [ ] T021 [US5] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/ComplianceController.java` with `GET /api/v1/admin/compliance/scores`

    **Checkpoint**: PHI protection and compliance dashboard independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T022 [P] Add Kafka consumer in `notification-service` to handle `UserDataErasureEvent` and anonymise notification history
    - [ ] T023 [P] Add SAST scan config (SpotBugs + OWASP dependency check) to `backend/*/pom.xml` in `verify` phase
    - [ ] T024 Run `quickstart.md` smoke tests (consent, erasure request, PHI store/read, compliance score)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T013): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T014–T017): Depends on Phase 2
    - **Phase 5** (T018–T021): Depends on Phase 2, Phase 3
    - **Phase 6** (T022–T024): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for GDPR consent and brute-force security alerting.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"010-api-gateway": textwrap.dedent("""\
    # Tasks: API Gateway (Kong)

    **Feature**: `010-api-gateway`
    **Input**: `specs/010-api-gateway/`
    **Config**: `api-gateway/`

    ## Phase 1: Setup

    - [ ] T001 Verify `docker-compose.yml` Kong service uses `kong:3.x` image, `KONG_DATABASE=off`, mounts `api-gateway/kong-multi-tenant-enhanced.yml` as declarative config
    - [ ] T002 [P] Create `api-gateway/base-config.yaml` — shared service and route definitions extracted from existing YAML files
    - [ ] T003 [P] Document the YAML split strategy: `kong.yml` (base), `kong-multi-tenant.yml` (tenant plugins), `kong-organisation.yml` (org routes), `kong-multi-tenant-enhanced.yml` (full production config)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Define all backend service entries in `api-gateway/kong-multi-tenant-enhanced.yml`: `auth-service`, `user-service`, `billing-service`, `notification-service`, `analytics-service`, `organisation-service`, `tenant-service` with health check paths
    - [ ] T005 [P] Configure JWT plugin globally in `kong-multi-tenant-enhanced.yml` — `key_claim_name: sub`, `claims_to_verify: exp, iat`
    - [ ] T006 [P] Configure Request Transformer plugin globally — inject `X-Tenant-ID`, `X-User-ID`, `X-Organization-ID` from JWT claims into downstream headers
    - [ ] T007 Configure CORS plugin — allowed origins: `localhost:3000`, production domain pattern; methods: GET, POST, PUT, PATCH, DELETE, OPTIONS

    **Checkpoint**: Kong base config with JWT + tenant header injection ready

    ---

    ## Phase 3: User Story 1 + 3 — Request Routing & Multi-Tenant Context (Priority: P1) 🎯 MVP

    **Goal**: Every authenticated request routed to correct service with X-Tenant-ID injected; invalid JWT returns 401.

    **Independent Test**: `curl -H "Authorization: Bearer {valid_jwt}" http://localhost:8000/api/v1/users` returns 200 with X-Tenant-ID injected. `curl` with expired JWT returns 401 (no backend hit).

    - [ ] T008 [US1] Define routes for all backend services in `kong-multi-tenant-enhanced.yml` with `strip_path: true` and service mappings
    - [ ] T009 [US1] Configure JWT plugin per route to validate tenant claim matches `X-Tenant-ID` header
    - [ ] T010 [US3] Add subdomain-based tenant resolver plugin config to `kong-multi-tenant.yml` — `{tenant}.platform.com` → injects `X-Tenant-ID`
    - [ ] T011 [US3] Test multi-tenant routing with `api-gateway/test-multi-tenant.sh`

    **Checkpoint**: JWT validation and tenant header injection independently testable

    ---

    ## Phase 4: User Story 2 — Rate Limiting (Priority: P1)

    **Goal**: Requests exceeding plan-tier limits return 429; limits configurable per consumer.

    **Independent Test**: Create Kong consumer for STARTER tier; exceed 300 req/min → 429 with Retry-After header. ENTERPRISE consumer allows 10 000 req/min.

    - [ ] T012 [P] [US2] Add rate-limiting plugin to `kong-multi-tenant-enhanced.yml` with per-plan configs: starter (10/s, 300/min), pro (50/s, 2000/min), enterprise (200/s, 10000/min)
    - [ ] T013 [US2] Define Kong Consumer entries in YAML for each plan tier
    - [ ] T014 [US2] Add `limit_by: header` with `header_name: X-Tenant-ID` for tenant-scoped rate limiting
    - [ ] T015 [US2] Add `api-gateway/test-rate-limiting.sh` smoke test script

    **Checkpoint**: Rate limiting per plan independently testable

    ---

    ## Phase 5: User Story 4 + 5 — CORS & Admin Monitoring (Priority: P2)

    **Goal**: CORS enforced for approved origins; admin views per-endpoint stats.

    **Independent Test**: Request from `localhost:3000` returns CORS headers. Request from unknown origin returns 403. Admin API exposes routes and plugin stats.

    - [ ] T016 [P] [US4] Configure CORS plugin in `kong-multi-tenant-enhanced.yml` with explicit `origins` list
    - [ ] T017 [US4] Add preflight OPTIONS route handling with 204 response
    - [ ] T018 [US5] Expose Kong Admin API on internal port 8001 only (not proxied via Kong itself); update `docker-compose.yml`
    - [ ] T019 [US5] Add maintenance mode response plugin stub in `kong-multi-tenant-enhanced.yml`

    **Checkpoint**: CORS and admin API independently testable

    ---

    ## Phase 6: Polish & Cross-Cutting

    - [ ] T020 [P] Add WAF plugin config to `kong-multi-tenant-enhanced.yml` (active from staging: block OWASP Top 10 patterns)
    - [ ] T021 [P] Add TLS termination config notes to `api-gateway/README.md` (self-signed local, Let's Encrypt dev–UAT)
    - [ ] T022 Run `api-gateway/test-multi-tenant.sh` full smoke test suite
    - [ ] T023 Run `api-gateway/deploy-multi-tenant.sh` to validate config reload without restart

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T007): Depends on Phase 1
    - **Phase 3** (T008–T011): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T012–T015): Depends on Phase 2
    - **Phase 5** (T016–T019): Depends on Phase 2
    - **Phase 6** (T020–T023): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working authenticated routing with tenant injection.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"011-tenant-configuration-management": textwrap.dedent("""\
    # Tasks: Tenant Configuration Management

    **Feature**: `011-tenant-configuration-management`
    **Input**: `specs/011-tenant-configuration-management/`
    **Source**: `backend/tenant-service/`

    ## Phase 1: Setup

    - [ ] T001 Add dependencies to `backend/tenant-service/pom.xml` (spring-data-jpa, spring-cache, spring-data-redis, vault-spring-starter)
    - [ ] T002 [P] Configure `backend/tenant-service/src/main/resources/application.yml` — Redis cache, Vault address, AES key path, cache TTL 300 s
    - [ ] T003 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/CacheConfig.java` (Spring Cache + Redis, cache name `config`)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `database/migrations/04-create-tenant-configurations.sql` — `tenant_configurations` table (tenant_id, config_key, config_value TEXT, environment, is_encrypted, is_active, data_type, updated_by)
    - [ ] T005 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/TenantConfiguration.java` JPA entity
    - [ ] T006 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/repository/TenantConfigurationRepository.java` (findByTenantIdAndConfigKeyAndEnvironment)
    - [ ] T007 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/security/AesConfigEncryptionService.java` — AES-256-GCM; key from Vault via `SecretsProvider`
    - [ ] T008 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/ConfigResolver.java` — resolves config through hierarchy: tenant → environment-global → global default

    **Checkpoint**: Config entity, encryption, and resolver ready

    ---

    ## Phase 3: User Story 1 + 2 — Hierarchical Resolution & Encrypted Secrets (Priority: P1) 🎯 MVP

    **Goal**: Service requests config → tenant value returned if exists, else env global, else global default. Encrypted values decrypted transparently.

    **Independent Test**: Set tenant config for key X → `getConfiguration(tenantId, X, env)` returns tenant value. Set env config only → env value returned. No config → null with WARNING log. Encrypted value → plaintext returned.

    - [ ] T009 [P] [US1] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/dto/ConfigResponse.java` and `SetConfigRequest.java`
    - [ ] T010 [US1] Implement `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/TenantConfigService.java` — `getConfiguration(tenantId, key, env, type)` with `@Cacheable(value="config", key="#tenantId+':'+#key+':'+#env")`; calls `ConfigResolver` for fallback; applies type conversion
    - [ ] T011 [US2] Implement `TenantConfigService.setConfiguration(tenantId, key, env, value, isEncrypted)` — encrypt if `is_encrypted=true`, persist, `@CacheEvict`
    - [ ] T012 [US1] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/controller/TenantConfigController.java` with `GET /api/v1/tenants/{tenantId}/config/{key}` and `PUT /api/v1/tenants/{tenantId}/config/{key}`
    - [ ] T013 [US2] Add `@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM')")` to write operations in `TenantConfigController`

    **Checkpoint**: Hierarchical config resolution and encrypted secrets independently testable

    ---

    ## Phase 4: User Story 3 + 4 — Environment Scoping & Type Safety (Priority: P2)

    **Goal**: Configs scoped to specific environments; typed retrieval returns correct Java type.

    **Independent Test**: Config with environment=production not returned when env=staging query made. `Integer.class` request on integer config returns Integer. Wrong type → null + error log.

    - [ ] T014 [P] [US3] Update `TenantConfigurationRepository` — add `findByTenantIdAndConfigKeyAndEnvironmentIn(tenantId, key, [env, "default"])` query
    - [ ] T015 [US3] Update `ConfigResolver.resolve()` — environment precedence: exact match → "default" → global hardcoded default
    - [ ] T016 [US4] Add type conversion in `TenantConfigService.getConfiguration()` — switch on `TenantConfiguration.data_type` to cast to String, Integer, Boolean, Long, Double, or parse JSONB

    **Checkpoint**: Environment scoping and type-safe retrieval independently testable

    ---

    ## Phase 5: Polish & Cross-Cutting

    - [ ] T017 [P] Add `DELETE /api/v1/tenants/{tenantId}/config/cache` cache-clear endpoint in `TenantConfigController`
    - [ ] T018 [P] Add Prometheus timer for config resolution (cache hit vs DB fallback latency) in `TenantConfigService`
    - [ ] T019 Run `quickstart.md` smoke tests (set, get, hierarchy, encryption, type conversion)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T013): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T014–T016): Depends on Phase 2, Phase 3
    - **Phase 5** (T017–T019): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working hierarchical config with encrypted secrets.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"012-frontend-application": textwrap.dedent("""\
    # Tasks: Frontend Application

    **Feature**: `012-frontend-application`
    **Input**: `specs/012-frontend-application/`
    **Source**: `frontend/src/`

    ## Phase 1: Setup

    - [ ] T001 Verify `frontend/package.json` includes: react@18, react-router-dom@6, axios, react-hook-form, @tanstack/react-query, tailwindcss; run `npm install`
    - [ ] T002 [P] Configure `frontend/tailwind.config.js` with content paths and custom theme tokens
    - [ ] T003 [P] Configure `frontend/src/services/api.ts` — Axios instance with base URL, request interceptor (inject `Authorization: Bearer {token}` + `X-Tenant-ID` + `X-Correlation-ID` headers), response interceptor (401 → trigger token refresh)

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `frontend/src/contexts/AuthContext.tsx` — `AuthState` interface, `AuthProvider` component (store user + accessToken in-memory, tenantId in localStorage, restore from localStorage on mount, expose `login()`, `logout()`, `refreshToken()`)
    - [ ] T005 [P] Create `frontend/src/components/auth/ProtectedRoute.tsx` — `requireAuth` guard (redirect to `/auth/login` if not authenticated), `requireAdmin` guard (redirect to `/app/dashboard` if not admin)
    - [ ] T006 [P] Create `frontend/src/components/layout/AppLayout.tsx` — sidebar navigation, top bar with user info and logout button
    - [ ] T007 Create `frontend/src/App.tsx` — React Router routes: public (`/`, `/auth/login`, `/auth/register`), protected (`/app/*`), admin (`/admin/*`); wrap with `QueryClientProvider` + `AuthProvider`

    **Checkpoint**: Auth context, route guards, and layouts ready

    ---

    ## Phase 3: User Story 1 + 2 — Registration, Login & Protected Routes (Priority: P1) 🎯 MVP

    **Goal**: Visitor registers and is redirected to login; user logs in and accesses protected dashboard; unauthenticated access redirected.

    **Independent Test**: Navigate to `/auth/register` → form renders. Submit valid form → success toast + redirect to `/auth/login`. Login with valid creds → redirect to `/app/dashboard`. Navigate to `/app/dashboard` unauthenticated → redirected to `/auth/login`. Non-admin visits `/admin/*` → redirected to `/app/dashboard`.

    - [ ] T008 [P] [US1] Create `frontend/src/pages/auth/RegisterPage.tsx` — form with firstName, lastName, email, password, confirmPassword, companyName using React Hook Form + Tailwind; submit to `POST /api/v1/auth/register` via axios
    - [ ] T009 [P] [US1] Create `frontend/src/services/authService.ts` — `register()`, `login()`, `logout()`, `refreshToken()`, `validateToken()` typed API calls
    - [ ] T010 [US2] Create `frontend/src/pages/auth/LoginPage.tsx` — form with email, password; on success call `AuthContext.login()`, redirect to `/app/dashboard`; on MFA required show OTP input
    - [ ] T011 [US2] Wire `ProtectedRoute` into all `/app/*` routes in `App.tsx`
    - [ ] T012 [US1] Add toast notification context (`frontend/src/contexts/ToastContext.tsx`) and `Toast.tsx` component (success/error/info/warning variants)

    **Checkpoint**: Registration, login, protected routes, and toasts independently testable

    ---

    ## Phase 4: User Story 3 — Dashboard (Priority: P1)

    **Goal**: Authenticated user sees KPI cards (active users, API calls, response time) and recent activity feed.

    **Independent Test**: Navigate to `/app/dashboard` → 4 KPI cards render with loading spinners → data loads → numbers shown. Activity feed shows latest 10 entries.

    - [ ] T013 [P] [US3] Create `frontend/src/services/analyticsService.ts` — `getUsageSummary(tenantId)`, `getActivityFeed(tenantId)`
    - [ ] T014 [P] [US3] Create `frontend/src/components/ui/KpiCard.tsx` (metric, value, trend, loading state)
    - [ ] T015 [US3] Create `frontend/src/pages/app/DashboardPage.tsx` — use React Query `useQuery(['analytics', tenantId, 'usage'])` for KPIs; `useQuery(['analytics', tenantId, 'activity'])` for feed; show skeleton loader during fetch

    **Checkpoint**: Dashboard independently testable

    ---

    ## Phase 5: User Story 4 — Billing Page (Priority: P2)

    **Goal**: User views current plan, usage metrics with limits, and invoice history.

    **Independent Test**: Navigate to `/app/billing` → plan card shows plan name/price/next billing date → usage bars show API calls/storage/users against limits → invoice table lists all invoices.

    - [ ] T016 [P] [US4] Create `frontend/src/services/billingService.ts` — `getCurrentSubscription(tenantId)`, `getInvoices(tenantId)`, `listPlans()`
    - [ ] T017 [P] [US4] Create `frontend/src/components/billing/UsageBar.tsx` (label, current, limit, warning at 80%)
    - [ ] T018 [US4] Create `frontend/src/pages/app/BillingPage.tsx` — plan card, 3 usage bars, invoice table; all data via React Query with stale-while-revalidate

    **Checkpoint**: Billing page independently testable

    ---

    ## Phase 6: User Story 5 — Admin Panel (Priority: P2)

    **Goal**: Admin accesses full management tools (tenants, users, monitoring, security, notifications, API gateway, system config), all code-split.

    **Independent Test**: Admin navigates to each `/admin/*` route → correct lazy-loaded page renders. Non-admin cannot access any `/admin/*` route.

    - [ ] T019 [P] [US5] Create `frontend/src/pages/admin/TenantManagementPage.tsx` — tenant list with status/plan/user count; `frontend/src/services/tenantService.ts`
    - [ ] T020 [P] [US5] Create `frontend/src/pages/admin/UserManagementPage.tsx` — user list, activate/deactivate actions; update `authService.ts` with user management calls
    - [ ] T021 [P] [US5] Create `frontend/src/pages/admin/MonitoringDashboardPage.tsx` — per-service health cards from analytics API
    - [ ] T022 [P] [US5] Create `frontend/src/pages/admin/SecurityCompliancePage.tsx` — security alert list + compliance scores
    - [ ] T023 [P] [US5] Create `frontend/src/pages/admin/BillingManagementPage.tsx`, `NotificationManagementPage.tsx`, `ApiGatewayManagementPage.tsx`, `SystemConfigurationPage.tsx`
    - [ ] T024 [US5] Configure all admin pages as `React.lazy()` + `Suspense` in `App.tsx` for code splitting

    **Checkpoint**: All admin pages independently accessible and testable

    ---

    ## Phase 7: Polish & Cross-Cutting

    - [ ] T025 [P] Add `frontend/src/contexts/TenantContext.tsx` — selected tenantId persisted to localStorage `saas.tenantId`
    - [ ] T026 [P] Add `npm run build` optimisation: verify LCP < 3 s on simulated 3G with `lighthouse` CI
    - [ ] T027 Run `frontend/test-registration.sh` end-to-end smoke test

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T007): Depends on Phase 1
    - **Phase 3** (T008–T012): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T013–T015): Depends on Phase 2, Phase 3
    - **Phase 5** (T016–T018): Depends on Phase 2
    - **Phase 6** (T019–T024): Depends on Phase 2, Phase 3
    - **Phase 7** (T025–T027): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for working registration, login, and protected routing.
"""),

# ─────────────────────────────────────────────────────────────────────────────
"013-infrastructure-deployment": textwrap.dedent("""\
    # Tasks: Infrastructure & Deployment

    **Feature**: `013-infrastructure-deployment`
    **Input**: `specs/013-infrastructure-deployment/`
    **Source**: `docker-compose.yml`, `setup/`, `infrastructure/`

    ## Phase 1: Setup

    - [ ] T001 Verify `docker-compose.yml` defines all required services: postgres, redis, keycloak, kong, rabbitmq, elasticsearch, logstash, kibana, prometheus, grafana, alertmanager, otel-collector, mailhog; add any missing
    - [ ] T002 [P] Create `Makefile` at repo root with targets: `local-up`, `local-down`, `local-seed`, `local-logs`, `build-all`
    - [ ] T003 [P] Create `setup/seed-demo-data.sh` — provisions 2 demo tenants and 5 users via REST API calls after all services are healthy

    ---

    ## Phase 2: Foundational

    - [ ] T004 Create `backend/*/src/main/java/com/saasstarterkit/*/security/SecretsProvider.java` interface (each service) — `getSecret(key)`, `getSecretMap(prefix)` with `VaultSecretsProvider` impl and `EnvSecretsProvider` impl (local dev fallback)
    - [ ] T005 [P] Configure HashiCorp Vault service in `docker-compose.yml` (dev mode) with `vault` policy and `secret/saas/dev/global/` path seeded in `setup/vault-seed.sh`
    - [ ] T006 [P] Create `config/providers.yaml` — active cloud provider selection: `provider: local|aws|gcp|azure`
    - [ ] T007 [P] Create `config/scm.yaml` — active SCM: `scm: github|gitlab|bitbucket`
    - [ ] T008 Create health check wrapper `setup/wait-for-healthy.sh` — polls all service `/actuator/health` endpoints until UP (used by `local-up` Makefile target)

    **Checkpoint**: Local stack fully startable and secret injection ready

    ---

    ## Phase 3: User Story 1 + 5 — Local Stack & Secret Management (Priority: P1) 🎯 MVP

    **Goal**: `make local-up` starts all services; secrets injected from Vault, not environment variables.

    **Independent Test**: `docker-compose up -d` → all containers healthy within 120 s. `make local-seed` → demo tenants exist. Any service starting without Vault reaches `EnvSecretsProvider` fallback. No secret appears in any application.yml.

    - [ ] T009 [US1] Update `setup/start-all-services.sh` to call `docker-compose up -d` and then `setup/wait-for-healthy.sh`
    - [ ] T010 [US1] Add `make local-seed` target to `Makefile` invoking `setup/seed-demo-data.sh`
    - [ ] T011 [US5] Update all service `application.yml` files — replace hardcoded secrets with `${saas.secrets.db_password}` etc.; bind to `VaultSecretsProvider.getSecret()`
    - [ ] T012 [US5] Create `setup/vault-seed.sh` — initialises Vault dev server, enables kv-v2, writes all required secrets

    **Checkpoint**: Local stack and secret management independently testable

    ---

    ## Phase 4: User Story 3 — Seven-Environment Parity (Priority: P2)

    **Goal**: Kustomize overlays produce correct manifests for all 7 environments from a shared base.

    **Independent Test**: `kustomize build k8s/overlays/dev` → Deployment uses 1 replica. `kustomize build k8s/overlays/prod` → Deployment uses 3 replicas, canary strategy, strict resource limits.

    - [ ] T013 [P] [US3] Create `k8s/base/` — Deployment, Service, ConfigMap, HPA, PodDisruptionBudget, ServiceMonitor manifests for each service (parameterised image tag)
    - [ ] T014 [P] [US3] Create `k8s/overlays/dev/`, `k8s/overlays/staging/`, `k8s/overlays/prod/` kustomization.yaml with correct replica counts, resource limits, and rollout strategies
    - [ ] T015 [US3] Create `k8s/overlays/ci/`, `k8s/overlays/canary/`, `k8s/overlays/dr/`, `k8s/overlays/load-test/` kustomization.yaml stubs
    - [ ] T016 [US3] Add `cronjob-scale-to-zero.yaml` to dev/sandbox/staging/UAT overlays (overnight scale-down via CronJob)

    **Checkpoint**: All 7 Kustomize overlays build without errors

    ---

    ## Phase 5: User Story 2 — Cloud-Agnostic Terraform (Priority: P2)

    **Goal**: `config/providers.yaml: provider: aws` + `terraform apply` provisions EKS, RDS, ElastiCache; no code changes for GCP or Azure.

    **Independent Test**: `terraform plan -var="environment=dev"` completes for aws module. Swap to gcp module, plan completes. No service code modified.

    - [ ] T017 [P] [US2] Create `infra/terraform/modules/aws/main.tf` — EKS, RDS PostgreSQL 15, ElastiCache Redis, S3 bucket, IAM roles
    - [ ] T018 [P] [US2] Create `infra/terraform/modules/gcp/main.tf` — GKE, Cloud SQL, Memorystore, GCS
    - [ ] T019 [P] [US2] Create `infra/terraform/modules/azure/main.tf` — AKS, Azure PostgreSQL, Azure Cache for Redis, Blob Storage
    - [ ] T020 [US2] Create `infra/terraform/environments/dev/main.tf` — reads `config/providers.yaml`, calls appropriate module with `environment=dev` vars
    - [ ] T021 [US2] Create `infra/terraform/modules/local/main.tf` — docker-compose substitute (for local-only use); outputs same variable names as cloud modules

    **Checkpoint**: Cloud-agnostic Terraform modules independently testable with `terraform plan`

    ---

    ## Phase 6: User Story 4 — CI/CD Pipeline (Priority: P2)

    **Goal**: Dagger pipeline runs build, test, SAST, DAST, container scan on every push. SCM files are thin wrappers.

    **Independent Test**: `dagger run pipeline/main.ts --environment=dev --dry-run=true` completes. CRITICAL SAST finding fails the build. Merge to develop triggers deploy to dev.

    - [ ] T022 [P] [US4] Create `pipeline/main.ts` Dagger TypeScript pipeline — steps: `buildImage()`, `runUnitTests()`, `runSast()`, `runDast()`, `scanContainer()`, `deployToEnvironment()`
    - [ ] T023 [P] [US4] Create `.github/workflows/ci.yml` — thin wrapper calling `dagger run pipeline/main.ts`
    - [ ] T024 [US4] Create `pipeline/steps/sast.ts` — SpotBugs + OWASP Dependency Check; fail pipeline on CRITICAL findings
    - [ ] T025 [US4] Add ArgoCD `Application` manifest in `k8s/argocd/` pointing to `k8s/overlays/{env}` for GitOps deployments
    - [ ] T026 [US4] Add Unleash feature flag server to `docker-compose.yml` and Kubernetes base manifests

    **Checkpoint**: CI/CD pipeline independently triggerable and verifiable

    ---

    ## Phase 7: Polish & Cross-Cutting

    - [ ] T027 [P] Update `README.md` with full local setup steps, `make` targets, and environment variable table
    - [ ] T028 [P] Add `setup/MULTI_TENANCY_IMPLEMENTATION_SUMMARY.md` update with deployment topology diagram
    - [ ] T029 Run `quickstart.md` smoke tests (`docker-compose up`, `make local-seed`, `make local-down`)

    ---

    ## Dependencies & Execution Order

    - **Phase 1** (T001–T003): No dependencies
    - **Phase 2** (T004–T008): Depends on Phase 1
    - **Phase 3** (T009–T012): Depends on Phase 2 — **MVP scope**
    - **Phase 4** (T013–T016): Depends on Phase 2
    - **Phase 5** (T017–T021): Depends on Phase 2
    - **Phase 6** (T022–T026): Depends on Phase 3, Phase 4
    - **Phase 7** (T027–T029): Depends on all phases

    ### MVP Scope
    Complete **Phase 1 + Phase 2 + Phase 3** for a fully startable local stack with Vault secrets.
"""),

}  # end TASKS dict


def main():
    written = 0
    for feature_dir, content in TASKS.items():
        target = os.path.join(SPECS_DIR, feature_dir, "tasks.md")
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w") as f:
            f.write(content)
        print(f"OK: {feature_dir}/tasks.md")
        written += 1
    print(f"\nTask files written: {written}")


if __name__ == "__main__":
    main()
