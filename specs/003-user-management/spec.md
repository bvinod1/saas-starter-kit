# Feature Specification: User Management

**Feature Branch**: `003-user-management`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Admin creates and manages users (Priority: P1)

An admin can create, update, activate, deactivate, and delete users within their tenant.

**Acceptance Scenarios**:
1. **Given** ADMIN role, **When** POST /api/admin/users with valid payload, **Then** user created and returned.
2. **Given** ADMIN role, **When** DELETE /api/admin/users/{id}, **Then** user removed.
3. **Given** non-ADMIN, **When** any admin user endpoint, **Then** 403 Forbidden.

---

### User Story 2 — Admin activates/deactivates user account (Priority: P1)

An admin can toggle user account status without deleting the account.

**Acceptance Scenarios**:
1. **Given** inactive user, **When** POST /api/admin/users/{id}/activate, **Then** user status = active.
2. **Given** active user, **When** POST /api/admin/users/{id}/deactivate, **Then** user status = inactive, user cannot login.

---

### User Story 3 — User profile management (Priority: P2)

A user can view and update their own profile including avatar, bio, timezone, language, and preferences.

**Acceptance Scenarios**:
1. **Given** authenticated user, **When** GET /api/users/profile, **Then** full profile returned.
2. **Given** authenticated user, **When** PUT /api/users/profile with valid payload, **Then** profile updated.

---

### User Story 4 — Dynamic user profile schema (Priority: P2)

An organisation admin can define custom user attribute schemas. Users are tagged with the schema
version they completed, and the frontend renders forms dynamically from the schema definition.

**Acceptance Scenarios**:
1. **Given** org admin, **When** schema published, **Then** schema version incremented, old version preserved.
2. **Given** frontend, **When** active schema fetched, **Then** form fields, validations, conditional visibility rendered per schema.
3. **Given** schema field change, **When** existing users viewed, **Then** historical profile data preserved.

---

### User Story 5 — Admin assigns roles (Priority: P2)

An admin can list, assign, and revoke roles for users in their tenant.

**Acceptance Scenarios**:
1. **Given** ADMIN, **When** GET /api/admin/roles, **Then** tenant roles listed.
2. **Given** ADMIN, **When** POST /api/admin/users/{id}/roles, **Then** role assigned.
3. **Given** ADMIN, **When** DELETE /api/admin/users/{id}/roles/{role}, **Then** role revoked.

---

### User Story 6 — Schema builder & lifecycle management (Priority: P2) ⚠️ NON-NEGOTIABLE

An org admin uses a visual schema builder to create, preview, version-diff, deprecate, and bulk-backfill custom user profile schemas.

**Acceptance Scenarios**:
1. **Given** org admin, **When** new schema fields added, **Then** side-by-side diff of current vs. new version shown before publish.
2. **Given** published schema v2, **When** admin initiates bulk backfill, **Then** all existing users' `dynamic_fields` updated with default values for new fields; progress trackable.
3. **Given** admin deprecates schema version, **When** users try to submit old-version data, **Then** 422 with migration prompt returned.
4. **Given** deprecated schema, **When** admin views version list, **Then** deprecated_at timestamp and reason displayed.

---

### Edge Cases
- What happens when creating a user with an email that already exists in Keycloak?
- How are tenant-scoped users distinguished from global users?
- What happens when a user is the last OWNER of an organisation?

## Requirements

### Functional Requirements
- **FR-001**: System MUST support full CRUD for tenant-scoped users.
- **FR-002**: System MUST support activate/deactivate without deletion.
- **FR-003**: System MUST store user profiles in `tenant_{slug}.users` with keycloak_user_id FK.
- **FR-004**: System MUST support dynamic user profile schemas with immutable versioning (draft → published → deprecated).
- **FR-005**: Dynamic schema MUST store profile values as JSONB.
- **FR-006**: System MUST propagate user creation to both Keycloak and PostgreSQL.
- **FR-007**: System MUST support timezone, language, and preferences per user.
- **FR-008**: All user operations MUST be logged with tenant + correlation IDs.
- **FR-009**: System MUST support OTel distributed tracing per user operation.
- **FR-010**: User roles MUST be manageable by admins via dedicated endpoints.
- **FR-011**: Admin MUST be able to view a version diff (before/after field list) before publishing a schema version.
- **FR-012**: System MUST support admin-initiated bulk backfill of `dynamic_fields` for existing users when new required fields are added to a schema version.
- **FR-013**: Schema versions MUST track `deprecated_at` timestamp and deprecation reason; deprecated versions MUST return 422 on new submissions.

### Key Entities
- **User**: `keycloak_user_id`, `email`, `first_name`, `last_name`, `avatar_url`, `bio`, `timezone`, `language`, `preferences`, `status`
- **OrgSchema**: Schema definition with version, status, field list
- **UserProfileValues**: JSONB profile values keyed to schema version
- **UserRole**: Many-to-many between user and role
- **TenantUser**: Cross-tenant registry entry

### Non-Functional Requirements
- User list query MUST be < 200 ms for up to 10 000 users per tenant
- Profile updates MUST be immediately consistent
