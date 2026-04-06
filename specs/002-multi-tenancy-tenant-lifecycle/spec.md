# Feature Specification: Multi-Tenancy & Tenant Lifecycle Management

**Feature Branch**: `002-multi-tenancy-tenant-lifecycle`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Automatic tenant provisioning on registration (Priority: P1)

When a new organisation is registered, the system auto-creates a dedicated PostgreSQL schema
(`tenant_{slug}`) with all required tables using the `create_tenant_schema()` database function.

**Acceptance Scenarios**:
1. **Given** successful registration, **When** `create_tenant(name, slug, realm, clientId)` called, **Then** schema `tenant_{slug}` exists with users, organizations, subscriptions tables.
2. **Given** duplicate slug, **When** tenant creation attempted, **Then** unique constraint error returned.

---

### User Story 2 — Super-admin lists all tenants (Priority: P1)

A super-admin can retrieve a paginated list of all tenants with their status, plan, and user count.

**Acceptance Scenarios**:
1. **Given** SUPER_ADMIN role, **When** GET /api/tenants, **Then** list of all tenants returned.
2. **Given** non-SUPER_ADMIN, **When** GET /api/tenants, **Then** 403 Forbidden.

---

### User Story 3 — Tenant lookup by slug (Priority: P2)

Any authenticated caller retrieves a specific tenant by its slug to resolve tenant context.

**Acceptance Scenarios**:
1. **Given** valid slug, **When** GET /api/tenants/{slug}, **Then** tenant details returned.
2. **Given** unknown slug, **When** GET /api/tenants/{slug}, **Then** 404 returned.

---

### User Story 4 — Suspend/delete tenant (Priority: P2)

A super-admin can deactivate or permanently delete a tenant and all its data.

**Acceptance Scenarios**:
1. **Given** SUPER_ADMIN, **When** DELETE /api/tenants/{id}, **Then** tenant marked deleted and schema dropped.
2. **Given** non-SUPER_ADMIN, **When** DELETE /api/tenants/{id}, **Then** 403.

---

### User Story 5 — Tenant context propagation (Priority: P1)

Every API request automatically resolves tenant context from headers, path, or subdomain and
scopes all database queries to the tenant's schema.

**Acceptance Scenarios**:
1. **Given** request with X-Tenant-ID header, **When** processed by interceptor, **Then** TenantContext.getCurrentTenant() returns correct ID.
2. **Given** request with no tenant context, **When** processed, **Then** 400 with missing-tenant error.

---

### Edge Cases
- Slug availability check before creation to prevent race conditions.
- What happens when `create_tenant_schema` fails mid-execution?
- How is a suspended tenant's data retained vs. a deleted tenant?

## Requirements

### Functional Requirements
- **FR-001**: System MUST provision a dedicated PostgreSQL schema per tenant on registration.
- **FR-002**: System MUST maintain a master tenant registry in `tenant_management.tenants`.
- **FR-003**: System MUST propagate tenant context via X-Tenant-ID header on every request.
- **FR-004**: System MUST support tenant statuses: active, inactive, suspended, deleted.
- **FR-005**: System MUST expose slug availability check endpoint.
- **FR-006**: System MUST enforce SUPER_ADMIN role for global tenant operations.
- **FR-007**: Tenant schema MUST include: users, user_roles, organizations, organization_members, subscriptions, invoices, notifications, audit_logs.
- **FR-008**: Tenant context MUST be propagated to OpenTelemetry spans and MDC logs.
- **FR-009**: Elasticsearch indices MUST be tenant-scoped.
- **FR-010**: System MUST enforce max_users limit per tenant.

### Key Entities
- **Tenant**: Master record (name, slug, domain, keycloak_realm_id, status, max_users, settings)
- **TenantSchema**: Registry of created schemas per tenant
- **TenantUser**: Cross-tenant user registry linking Keycloak user IDs to tenants

### Non-Functional Requirements
- Schema provisioning MUST complete in < 5 s
- Tenant context resolution MUST add < 5 ms overhead per request
- Multi-tenant queries MUST be fully isolated with no cross-tenant data leakage
