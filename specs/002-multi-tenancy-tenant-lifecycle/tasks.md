# Tasks: Multi-Tenancy & Tenant Lifecycle Management

**Feature**: `002-multi-tenancy-tenant-lifecycle`
**Input**: `specs/002-multi-tenancy-tenant-lifecycle/`
**Source**: `backend/tenant-service/` (tenant endpoints), `database/`

## Phase 1: Setup

- [ ] T001 Add Liquibase and PostgreSQL JDBC dependencies to `backend/user-service/pom.xml`
- [ ] T002 [P] Configure `backend/tenant-service/src/main/resources/application.yml` — datasource, schema routing, Liquibase changelog path
- [ ] T003 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantDataSourceConfig.java` (schema-switching datasource)

---

## Phase 2: Foundational

- [ ] T004 Create `database/init/02-multi-tenant-schema.sql` — `tenant_management.tenants` table, `create_tenant_schema()` PL/pgSQL function that provisions `tenant_{slug}` schema with all required tables
- [ ] T005 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/Tenant.java` JPA entity (id, name, slug, status, plan_id, keycloak_realm, schema_name)
- [ ] T006 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/TenantConfiguration.java` JPA entity
- [ ] T007 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantContext.java` ThreadLocal holder
- [ ] T008 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/TenantInterceptor.java` — extract X-Tenant-ID header, populate TenantContext, set MDC + OTel span attribute
- [ ] T009 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/repository/TenantRepository.java`

**Checkpoint**: Tenant context propagation ready; all user stories can proceed

> **Provider Contract**: All RabbitMQ publishing MUST go through `QueueProvider` from `shared-libs/providers/`.

---

## Phase 3: User Story 1 + 5 — Tenant Provisioning & Context (Priority: P1) 🎯 MVP

**Goal**: Registering creates a tenant schema; every request carries tenant context.

**Independent Test**: Call `TenantService.createTenant()` → schema `tenant_{slug}` exists in PostgreSQL. Any request with X-Tenant-ID sets `TenantContext.getCurrentTenant()`.

- [ ] T010 [P] [US1] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/dto/CreateTenantRequest.java` and `TenantResponse.java`
- [ ] T011 [US1] Implement `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/TenantService.java` — `createTenant()`: persist Tenant entity, call `create_tenant_schema()` via native SQL, publish `TenantProvisionedEvent` to RabbitMQ
- [ ] T012 [US5] Register `TenantInterceptor` in `WebMvcConfigurer` in `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/WebConfig.java`
- [ ] T013 [US5] Add OTel span attribute `tenant.id` and MDC field `tenantId` in `TenantInterceptor`
- [ ] T014 [US5] Add guard: if no X-Tenant-ID and endpoint requires tenant context, return 400

**Checkpoint**: Tenant provisioning and context propagation independently testable

---

## Phase 4: User Story 2 — Super-Admin Tenant List (Priority: P1)

**Goal**: A super-admin can list all tenants with pagination and status filtering.

**Independent Test**: GET /api/v1/tenants with SUPER_ADMIN token returns paginated list; non-SUPER_ADMIN returns 403.

- [ ] T015 [P] [US2] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/controller/TenantController.java`
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
