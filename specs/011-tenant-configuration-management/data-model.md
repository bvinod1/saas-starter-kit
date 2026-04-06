# Data Model: Tenant Configuration Management

## Configuration Hierarchy
```
Global Defaults (system-wide, code/YAML)
    ↓ override
Plan Defaults (per subscription tier)
    ↓ override
Tenant Overrides (admin-set, DB)
    ↓ override
Organization Overrides (org-level, DB)
```

## Entity: GlobalConfigDefault
| Field | Type | Notes |
|-------|------|-------|
| key | VARCHAR(200) (PK) | Dot-notation: `feature.mfa.required` |
| value | TEXT | Serialized value |
| data_type | VARCHAR(20) | STRING, INTEGER, BOOLEAN, JSON |
| description | TEXT | |

## Entity: PlanConfigDefault
| Field | Type | Notes |
|-------|------|-------|
| plan_id | VARCHAR(50) | |
| key | VARCHAR(200) | |
| value | TEXT | |
| PK | (plan_id, key) | |

## Entity: TenantConfig
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| key | VARCHAR(200) | |
| value | TEXT | AES-256-GCM encrypted if sensitive |
| is_secret | BOOLEAN | Vault-backed if true |
| updated_by | UUID | |
| updated_at | TIMESTAMPTZ | |
| UNIQUE | (tenant_id, key) | |

## Entity: OrgConfig
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| org_id | UUID | |
| tenant_id | UUID | For RLS |
| key | VARCHAR(200) | |
| value | TEXT | |
| UNIQUE | (org_id, key) | |

## Entity: ConfigCacheEntry (Redis)
| Key pattern | TTL |
|-------------|-----|
| `config:{tenant_id}:{key}` | 300 s |
| `config:{tenant_id}:*` | Bulk invalidated on update |

## Sensitive Config Storage
- is_secret = true → value is Vault path reference
- Vault path: `secret/saas/{env}/tenant/{tenant_id}/{key}`
- Vault dynamic secrets for DB credentials

## Relationships
```
Tenant 1 ──< TenantConfig
Org 1 ──< OrgConfig
GlobalConfigDefault <── resolved by ── ConfigResolver service
```

## Indexes
- `tenant_config(tenant_id, key)` UNIQUE
- `org_config(org_id, key)` UNIQUE
- `global_config_default(key)` PK

## Migration References
- `database/migrations/04-create-tenant-configurations.sql`
