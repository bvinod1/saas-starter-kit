# Feature Specification: Organization Management

**Feature Branch**: `004-organization-management`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Create an organisation within a tenant (Priority: P1)

A tenant user creates an Organization (team/department) with a name, slug, and description.

**Acceptance Scenarios**:
1. **Given** authenticated user with X-Tenant-ID, **When** POST /api/organizations with name + slug, **Then** organisation created under tenant schema.
2. **Given** missing X-Tenant-ID, **When** POST /api/organizations, **Then** 400 "No tenant context found".
3. **Given** duplicate slug within tenant, **When** POST /api/organizations, **Then** unique constraint error.

---

### User Story 2 — List and view organisations (Priority: P1)

A tenant user lists all Organizations in their tenant and views individual org details.

**Acceptance Scenarios**:
1. **Given** valid tenant context, **When** GET /api/organizations, **Then** all orgs in tenant returned.
2. **Given** valid org ID, **When** GET /api/organizations/{id}, **Then** org details returned.
3. **Given** unknown org ID, **When** GET /api/organizations/{id}, **Then** 404.

---

### User Story 3 — Manage organisation members (Priority: P2)

An org owner/admin adds or removes members and assigns roles (owner/admin/member).

**Acceptance Scenarios**:
1. **Given** org owner, **When** member added, **Then** membership created with invited_by + invited_at.
2. **Given** member accepting invite, **When** acceptance confirmed, **Then** accepted_at timestamp recorded.
3. **Given** org owner removing member, **When** DELETE member, **Then** soft-delete with deleted_at.

---

### User Story 5 — Team management within organisations (Priority: P2) ⚠️ NON-NEGOTIABLE

An org admin creates and manages Teams (squads/departments) within an organisation. Users belong to Teams; Teams inherit org-level roles and can have team-specific permissions.

**Acceptance Scenarios**:
1. **Given** org admin, **When** POST /api/v1/organizations/{id}/teams with name + slug, **Then** team created under org.
2. **Given** org admin, **When** POST /api/v1/organizations/{id}/teams/{teamId}/members with userId, **Then** user added to team.
3. **Given** user listed, **When** GET /api/v1/users/me/teams, **Then** all teams for current user returned.
4. **Given** team deleted, **When** members queried, **Then** team_memberships reflect departure; user's org membership unaffected.

---

### User Story 4 — Enterprise governance features (Priority: P3)

A super-admin views cross-tenant org health: tier, usage limits, storage, compliance settings.

**Acceptance Scenarios**:
1. **Given** SUPER_ADMIN, **When** org tier is ENTERPRISE, **Then** custom max_users and api_rate_limit enforced.
2. **Given** org with onboarding_completed=false, **When** queried, **Then** onboarding_step returned.

---

### Edge Cases
- What happens when an org slug conflicts across tenants?
- What if the last owner leaves an organisation?
- How does soft-delete interact with unique constraints?

## Requirements

### Functional Requirements
- **FR-001**: System MUST support full CRUD for Organizations within a tenant.

> **Terminology Note**: This spec uses "Organization" (American English) consistently as the canonical entity name. Java class names (`OrganisationService`, `OrganisationMember`) retain their existing spellings for backward compatibility with existing code.
- **FR-002**: Organisation operations MUST require valid tenant context.
- **FR-003**: System MUST support member invitation flow with invite tracking.
- **FR-004**: System MUST support org tiers: STARTER, PROFESSIONAL, ENTERPRISE.
- **FR-005**: System MUST enforce usage limits: max_users, storage_limit_gb, api_rate_limit.
- **FR-006**: System MUST record comprehensive audit logs for all org and member actions.
- **FR-007**: Audit logs MUST include: action, resource_type, risk_level (LOW/MEDIUM/HIGH/CRITICAL), ip_address, compliance_category.
- **FR-008**: Soft-delete MUST be used for orgs and members (deleted_at field).
- **FR-009**: System MUST track onboarding progress (onboarding_completed, onboarding_step).
- **FR-010**: Member profiles MUST include department, job_title, access_level.
- **FR-011**: System MUST support Teams as a sub-entity of Organisation (Organization→Teams→Users→Roles hierarchy).
- **FR-012**: Team membership MUST be independent of Org membership (removing from team does not remove from org).
- **FR-013**: Team slugs MUST be unique within an organisation.

### Key Entities
- **Organisation**: name, slug, domain, tier, subscription_status, max_users, storage_limit_gb, settings, compliance_settings
- **Team**: org_id, name, slug, description, created_by; sub-entity of Organisation
- **TeamMembership**: team_id, user_id, role (LEAD/MEMBER), joined_at, deleted_at
- **OrganisationMember**: user_id, role, status, department, job_title, invited_by, accepted_at, two_factor_enabled
- **OrgAuditLog**: action, resource_type, resource_id, risk_level, compliance_category
- **MemberAuditLog**: member-scoped audit record

### Non-Functional Requirements
- Audit log writes MUST be asynchronous to avoid adding latency to member operations
- Soft-deleted records MUST be excluded from all default queries
