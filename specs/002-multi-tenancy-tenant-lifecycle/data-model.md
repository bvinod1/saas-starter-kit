# Data Model: Multi-Tenancy & Tenant Lifecycle

## Entity: Tenant
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| name | VARCHAR(200) | Display name |
| subdomain | VARCHAR(100) | UNIQUE; URL slug |
| status | tenant_status ENUM | PENDING, ACTIVE, SUSPENDED, ARCHIVED |
| plan_id | VARCHAR(50) | Maps to billing plan |
| schema_name | VARCHAR(63) | PostgreSQL schema: `tenant_{id}` |
| keycloak_realm | VARCHAR(100) | Dedicated realm name |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

## Entity: TenantConfiguration
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID (FK → tenants.id) UNIQUE | |
| features | JSONB | Feature flag map |
| limits | JSONB | Resource quotas |
| custom_domain | VARCHAR(255) | Optional |
| updated_at | TIMESTAMPTZ | |

## Entity: TenantProvisioningJob
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID (FK) | |
| status | job_status ENUM | QUEUED, RUNNING, DONE, FAILED |
| error_message | TEXT | |
| started_at | TIMESTAMPTZ | |
| completed_at | TIMESTAMPTZ | |

## Relationships
```
Tenant 1 ──1 TenantConfiguration
Tenant 1 ──< TenantProvisioningJob
Tenant 1 ──< (schema: tenant_{id}) schema-isolated tables
```

## Indexes
- `tenants(subdomain)` UNIQUE
- `tenants(status, created_at)`
- `tenant_provisioning_jobs(tenant_id, status)`

## State Transitions (Tenant)
```
PENDING → PROVISIONING → ACTIVE → SUSPENDED → ARCHIVED
PROVISIONING → FAILED (on error; retryable)
```

## Migration References
- `database/init/02-multi-tenant-schema.sql`
- `database/migrations/04-create-tenant-configurations.sql`
