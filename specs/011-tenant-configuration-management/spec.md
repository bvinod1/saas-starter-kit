# Feature Specification: Tenant Configuration Management

**Feature Branch**: `011-tenant-configuration-management`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Hierarchical config resolution (Priority: P1)

When a service requests a config value for a tenant + key + environment, the system resolves
it through a hierarchy: tenant-specific → environment-global → global default.

**Acceptance Scenarios**:
1. **Given** tenant-specific config for key X, **When** queried, **Then** tenant value returned.
2. **Given** no tenant config for key X but env global config exists, **When** queried, **Then** env global value returned.
3. **Given** no tenant or env config, **When** queried, **Then** global default returned.
4. **Given** no config at any level, **When** queried, **Then** null returned with warning log.

---

### User Story 2 — Encrypted secret storage (Priority: P1)

A tenant admin stores sensitive config values (API keys, credentials). They are AES-256 encrypted
at rest and decrypted transparently on read.

**Acceptance Scenarios**:
1. **Given** config marked `is_encrypted=true`, **When** stored, **Then** value encrypted in DB.
2. **Given** encrypted config read, **When** service calls getConfiguration(), **Then** plaintext value returned.
3. **Given** wrong encryption key, **When** decryption attempted, **Then** error logged, null returned.

---

### User Story 3 — Environment-scoped configuration (Priority: P2)

Config values can be scoped to specific environments (dev, staging, production) and override each other.

**Acceptance Scenarios**:
1. **Given** config with environment=production, **When** queried in staging, **Then** staging config returned instead.
2. **Given** config with environment=default, **When** queried for any environment with no match, **Then** default value used.

---

### User Story 4 — Type-safe config retrieval (Priority: P2)

Services retrieve configs as typed values (String, Integer, Boolean, Long, Double, JSONB) without manual parsing.

**Acceptance Scenarios**:
1. **Given** Integer config, **When** getConfiguration(tenantId, key, env, Integer.class), **Then** Integer returned.
2. **Given** Boolean config, **When** getConfiguration(tenantId, key, env, Boolean.class), **Then** Boolean returned.
3. **Given** malformed value for type, **When** type conversion fails, **Then** null returned with error log.

---

### Edge Cases
- What happens when a config key is deleted mid-request?
- How are config cache invalidations handled on update?
- How is encryption key rotation managed without downtime?

## Requirements

### Functional Requirements
- **FR-001**: System MUST support hierarchical config resolution: tenant → environment → global default.
- **FR-002**: Sensitive values MUST support AES-256 encryption with `is_encrypted` flag.
- **FR-003**: Config MUST be cacheable via Spring Cache with key composed of tenantId + configKey + environment.
- **FR-004**: System MUST support type-safe retrieval for: String, Integer, Boolean, Long, Double, JSONB.
- **FR-005**: Config MUST be scoped per environment: dev, staging, production, default.
- **FR-006**: Encryption key MUST be injected via SecretsProvider interface, never hardcoded.
- **FR-007**: Config updates MUST invalidate relevant cache entries.
- **FR-008**: System MUST log warnings when config key not found at any level.
- **FR-009**: Config API MUST require appropriate authorisation (tenant admin or system).
- **FR-010**: Config MUST support JSONB type for complex structured values.

### Key Entities
- **TenantConfiguration**: tenant_id, config_key, config_value, environment, is_encrypted, is_active, data_type

### Non-Functional Requirements
- Cached config reads MUST complete in < 5 ms
- Cache miss resolution (DB fallback) MUST complete in < 50 ms
- Encryption/decryption overhead MUST be < 10 ms
