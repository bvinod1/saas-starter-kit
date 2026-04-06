# Data Model: Authentication & Identity Management

## Entity: User (Keycloak)
Managed by Keycloak; local mirror held in `auth-service`.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | Keycloak user ID |
| username | VARCHAR(150) | UNIQUE per realm |
| email | VARCHAR(255) | UNIQUE per realm |
| tenant_id | UUID (FK → tenants.id) | Realm partition |
| mfa_enabled | BOOLEAN | TOTP/WEBAUTHN toggle |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

## Entity: RefreshToken
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID (FK → users.id) | |
| tenant_id | UUID (FK → tenants.id) | |
| token_hash | BYTEA | SHA-256 of opaque token |
| expires_at | TIMESTAMPTZ | |
| revoked | BOOLEAN | |
| created_at | TIMESTAMPTZ | |

## Entity: AuditLog
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | Nullable for anonymous |
| tenant_id | UUID | |
| event_type | VARCHAR(60) | LOGIN, LOGOUT, MFA_ENROLL, … |
| ip_address | INET | |
| user_agent | TEXT | |
| occurred_at | TIMESTAMPTZ | |

## Relationships
```
Tenant 1 ──< User 1 ──< RefreshToken
User 1 ──< AuditLog
```

## Indexes
- `users(tenant_id, email)` UNIQUE
- `refresh_tokens(token_hash)` UNIQUE
- `refresh_tokens(user_id, expires_at)`
- `audit_log(tenant_id, occurred_at)`

## State Transitions (User Account)
```
PENDING_VERIFICATION → ACTIVE → SUSPENDED → DEACTIVATED
ACTIVE → MFA_PENDING_SETUP (on policy change)
```

## Migration References
- `database/init/01-init-schema.sql` – users table
- `database/migrations/05-gdpr-hipaa-compliance.sql` – audit extension
