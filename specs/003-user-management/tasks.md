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

## Phase 5b: User Story 6 — Schema Builder & Lifecycle Management (Priority: P2) ⚠️ NON-NEGOTIABLE

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
- **Phase 5b** (T023b–T023h): Depends on Phase 5
- **Phase 6** (T024–T025): Depends on Phase 2
- **Phase 7** (T026–T028): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working admin user CRUD.
