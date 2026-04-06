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

## Entity: Team ⚠️ NON-NEGOTIABLE (C3 — Constitution Principle VI)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| org_id | UUID (FK → organizations.id) | |
| name | VARCHAR(150) | |
| slug | VARCHAR(80) | Unique within org |
| description | TEXT | Nullable |
| created_by | UUID (FK → user_profiles.id) | |
| created_at | TIMESTAMPTZ | |
| deleted_at | TIMESTAMPTZ | Nullable; soft-delete |

## Entity: TeamMembership
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| team_id | UUID (FK → teams.id) | |
| user_id | UUID (FK → user_profiles.id) | |
| role | VARCHAR(30) | LEAD, MEMBER |
| joined_at | TIMESTAMPTZ | |
| deleted_at | TIMESTAMPTZ | Nullable; soft-delete |

## Relationships
```
Tenant 1 ──< Organization (self-referential tree via parent_org_id)
Organization 1 ──< Team
Team 1 ──< TeamMembership
Organization 1 ──< OrganizationMembership
Organization 1 ──< OrganizationAuditEvent
```
**Hierarchy**: Tenant → Organization → Team → User (with Roles at each level)

## Indexes
- `organizations(tenant_id, parent_org_id)`
- `organizations(tenant_id, deleted_at)` WHERE deleted_at IS NULL
- `org_memberships(org_id, user_id)` UNIQUE
- `org_memberships(user_id, tenant_id)`
- `teams(org_id, slug)` UNIQUE (slug unique within org)
- `teams(org_id, deleted_at)` WHERE deleted_at IS NULL
- `team_memberships(team_id, user_id)` UNIQUE WHERE deleted_at IS NULL

## State Transitions (Organization)
```
ACTIVE → ARCHIVED (soft delete; deleted_at set)
```

## Migration References
- `database/init/03-organizational-management-schema.sql`
