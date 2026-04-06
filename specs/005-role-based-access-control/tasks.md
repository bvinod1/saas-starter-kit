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
