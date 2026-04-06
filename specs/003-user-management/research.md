# Research: User Management

## Decision: Dual Storage — Keycloak + PostgreSQL

**Decision**: User identity (password hash, OAuth links, MFA) stored in Keycloak; user profile (avatar, bio, preferences, timezone) stored in per-tenant PostgreSQL.
**Rationale**: Clean separation of concerns. Keycloak owns authentication; app DB owns profile. Avoids coupling auth provider to profile schema changes.
**Alternatives considered**:
- Profile in Keycloak attributes: Limited query capabilities; Keycloak not designed for complex profile queries

## Decision: Dynamic Profile Schemas with Immutable Versioning

**Decision**: OrgSchema entity stores field definitions as JSONB; UserProfileValues stores values as JSONB keyed to schema version. Schema lifecycle: draft → published → deprecated. Published versions are immutable.
**Rationale**: Constitution VI is NON-NEGOTIABLE for dynamic user profile schemas. Organisations must define custom attributes without code deployments. Historical data preserved across schema versions.
**Alternatives considered**:
- Hard-coded profile columns: Violates constitution NON-NEGOTIABLE requirement
- EAV (Entity-Attribute-Value): Worse query performance; harder to type-check

## Decision: OTel + MDC Tracing Per User Operation

**Decision**: UserController wraps every operation in TracingService.executeWithSpan(); LoggingService sets MDC context (tenantId, userId, correlationId).
**Rationale**: Constitution VII requires per-operation observability; correlationId enables request tracing across logs and traces.

## Decision: Activate/Deactivate Without Deletion

**Decision**: User status field (active/inactive) controls access without data deletion.
**Rationale**: GDPR right to erasure is separate from deactivation. Account reactivation is possible. Audit trail preserved.

## Best Practices Confirmed

- createUser() must be transactional: Keycloak user created first; PostgreSQL user record second; rollback Keycloak on DB failure
- Profile JSONB indexed with GIN index for fast attribute lookup
- Schema version embedded in UserProfileValues record; frontend fetches active schema to render form
- Bulk backfill procedure required when new required fields added to a schema version
