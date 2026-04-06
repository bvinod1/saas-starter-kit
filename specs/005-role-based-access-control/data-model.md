# Data Model: Role-Based Access Control

## Entity: Role
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | NULL = system role |
| name | VARCHAR(100) | |
| description | TEXT | |
| is_system | BOOLEAN | Immutable built-in role |
| created_at | TIMESTAMPTZ | |

## Entity: Permission
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| resource | VARCHAR(100) | e.g. `users`, `billing` |
| action | VARCHAR(50) | CREATE, READ, UPDATE, DELETE, EXECUTE |
| description | TEXT | |

## Entity: RolePermission
| Field | Type | Notes |
|-------|------|-------|
| role_id | UUID (FK → roles.id) | |
| permission_id | UUID (FK → permissions.id) | |
| PK | (role_id, permission_id) | |

## Entity: UserRole
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | |
| role_id | UUID (FK → roles.id) | |
| tenant_id | UUID | Scope |
| granted_by | UUID | |
| granted_at | TIMESTAMPTZ | |
| expires_at | TIMESTAMPTZ | Nullable |

## Entity: PolicyCacheEntry (Redis)
| Field | Type | Notes |
|-------|------|-------|
| key | STRING | `rbac:{tenant_id}:{user_id}` |
| permissions | JSON ARRAY | Resolved permission list |
| ttl | 300s | Cache TTL |

## Relationships
```
Tenant 1 ──< Role
Role M ──< RolePermission >── M Permission
User M ──< UserRole >── M Role
```

## Indexes
- `roles(tenant_id, name)` UNIQUE
- `user_roles(user_id, tenant_id)`
- `user_roles(role_id)`

## Migration References
- `database/init/01-init-schema.sql` (roles, permissions)
- `database/init/03-organizational-management-schema.sql` (org-scoped roles)
