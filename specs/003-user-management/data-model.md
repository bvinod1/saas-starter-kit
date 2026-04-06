# Data Model: User Management

## Entity: UserProfile
Stored in `user-service` PostgreSQL; Keycloak holds credentials.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | Matches Keycloak user ID |
| tenant_id | UUID (FK → tenants.id) | Row-level isolation |
| email | VARCHAR(255) | |
| first_name | VARCHAR(100) | |
| last_name | VARCHAR(100) | |
| phone | VARCHAR(30) | |
| avatar_url | TEXT | |
| status | user_status ENUM | ACTIVE, SUSPENDED, DEACTIVATED |
| dynamic_fields | JSONB | Per-tenant custom profile schema |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

## Entity: UserTenantRole
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID (FK → user_profiles.id) | |
| tenant_id | UUID | |
| role_id | UUID (FK → roles.id) | |
| granted_at | TIMESTAMPTZ | |
| granted_by | UUID | |

## Entity: UserConsent (GDPR)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | |
| tenant_id | UUID | |
| consent_type | VARCHAR(60) | MARKETING, ANALYTICS, ESSENTIAL |
| consented | BOOLEAN | |
| consented_at | TIMESTAMPTZ | |
| ip_address | INET | |

## Relationships
```
Tenant 1 ──< UserProfile
UserProfile 1 ──< UserTenantRole
UserProfile 1 ──< UserConsent
OrgSchema 1 ──< SchemaBackfillJob
```

## Dynamic Profile Schema
Tenant administrators define additional fields via `TenantProfileSchema` (JSONB):
```json
{
  "fields": [
    { "name": "department", "type": "string", "required": true },
    { "name": "employee_id", "type": "string", "required": false }
  ]
}
```
Stored in `dynamic_fields` JSONB; validated at service layer against schema.

## Entity: OrgSchema (extended — C2 fix)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| version | INTEGER | Auto-incremented on publish |
| status | VARCHAR(20) | DRAFT, PUBLISHED, DEPRECATED |
| fields | JSONB | Field definitions array |
| deprecated_at | TIMESTAMPTZ | Nullable; set on deprecation |
| deprecation_reason | TEXT | Nullable |
| created_at | TIMESTAMPTZ | |

## Entity: SchemaBackfillJob ⚠️ NON-NEGOTIABLE (C2)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| schema_id | UUID (FK → org_schemas.id) | Target schema version |
| tenant_id | UUID | |
| status | VARCHAR(20) | PENDING, RUNNING, COMPLETED, FAILED |
| total_count | INTEGER | Total users to backfill |
| processed_count | INTEGER | Users processed so far |
| started_at | TIMESTAMPTZ | |
| completed_at | TIMESTAMPTZ | Nullable |
| error_message | TEXT | Nullable; set on FAILED |

## Indexes
- `user_profiles(tenant_id, email)` UNIQUE
- `user_profiles(tenant_id, status)`
- `user_tenant_roles(user_id, tenant_id)`
- `org_schemas(tenant_id, status)` WHERE status = 'PUBLISHED'
- `schema_backfill_jobs(tenant_id, status)`

## Migration References
- `database/init/01-init-schema.sql`
- `database/migrations/05-gdpr-hipaa-compliance.sql`
