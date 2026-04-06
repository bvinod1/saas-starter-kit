# Feature Specification: Role-Based Access Control (RBAC)

**Feature Branch**: `005-role-based-access-control`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Role assignment and listing (Priority: P1)

An admin views all available roles and assigns or revokes roles for users in their tenant.

**Acceptance Scenarios**:
1. **Given** ADMIN, **When** GET /api/admin/roles, **Then** all tenant roles returned.
2. **Given** ADMIN, **When** POST /api/admin/users/{id}/roles with role name, **Then** role assigned.
3. **Given** ADMIN, **When** DELETE /api/admin/users/{id}/roles/{role}, **Then** role revoked.
4. **Given** non-ADMIN, **When** any role management endpoint, **Then** 403.

---

### User Story 2 — Endpoint access enforcement (Priority: P1)

Spring Security `@PreAuthorize` annotations enforce role checks before any controller method runs.

**Acceptance Scenarios**:
1. **Given** user with SUPER_ADMIN role, **When** accessing tenant list endpoint, **Then** 200.
2. **Given** user without SUPER_ADMIN, **When** accessing tenant list endpoint, **Then** 403.
3. **Given** user with ADMIN role, **When** accessing admin user endpoints, **Then** 200.

---

### User Story 3 — Permission-level access checks (Priority: P2)

Fine-grained `resource:action` permissions beyond role-level checks allow per-feature access control.

**Acceptance Scenarios**:
1. **Given** role with `billing:read` permission, **When** GET billing endpoint, **Then** 200.
2. **Given** role without `billing:write`, **When** POST billing endpoint, **Then** 403.

---

### User Story 4 — OPA policy enforcement (East-West) (Priority: P3)

Internal service-to-service calls are authorized via OPA policies derived from the RBAC model.

**Acceptance Scenarios**:
1. **Given** service call with valid workload identity, **When** OPA evaluates, **Then** permit if policy allows.
2. **Given** unauthorised workload, **When** OPA evaluates, **Then** deny.

---

### Edge Cases
- What happens if a user has conflicting roles?
- How are role changes reflected in active sessions?
- What happens when a role is deleted while users still have it assigned?

## Requirements

### Functional Requirements
- **FR-001**: System MUST define roles: SUPER_ADMIN, ADMIN, TENANT_ADMIN, USER (minimum).
- **FR-002**: Roles MUST be stored in `auth.roles` with many-to-many via `auth.user_roles`.
- **FR-003**: Permissions MUST be stored as resource:action pairs in `auth.permissions`.
- **FR-004**: Role-permission mapping MUST be stored in `auth.role_permissions`.
- **FR-005**: All controller methods MUST be secured via `@PreAuthorize` annotations.
- **FR-006**: Keycloak realm MUST mirror local role definitions.
- **FR-007**: OPA policies MUST govern East-West service authorisation from dev environment upward.
- **FR-008**: Role changes MUST be reflected in tokens on next refresh.
- **FR-009**: System MUST support at minimum: Owner, Admin, Member, Viewer, Guest roles.
- **FR-010**: Permission checks MUST be cacheable to avoid DB round-trips per request.

### Key Entities
- **Role**: name, description
- **Permission**: name, resource, action
- **UserRole**: user_id → role_id (many-to-many)
- **RolePermission**: role_id → permission_id (many-to-many)

### Non-Functional Requirements
- Permission check MUST add < 5 ms overhead (via cache)
- Role list MUST be returned in < 100 ms
