# Research: Role-Based Access Control

## Decision: Keycloak + Local RBAC Mirror

**Decision**: Roles defined in Keycloak realms AND mirrored in local auth.roles/auth.permissions tables. Token carries role claims from Keycloak; local tables used for fine-grained permission checks.
**Rationale**: Keycloak handles SSO role propagation across services; local permission table enables resource:action granularity without custom Keycloak attribute configuration.

## Decision: @PreAuthorize for North-South; OPA for East-West

**Decision**: Spring Security @PreAuthorize enforces role checks at controller layer (North-South). OPA policies enforce service-to-service authorisation (East-West).
**Rationale**: Clean separation of auth concerns per traffic class. @PreAuthorize is declarative and testable. OPA provides deny-all default with explicit allow-list.

## Decision: Spring Cache for Permission Checks

**Decision**: Permission lookups cached with key = userId + resource + action. Cache evicted on role change.
**Rationale**: Permission check is hot path; must add < 5 ms overhead. DB lookup on every request is unacceptable at scale.

## Best Practices Confirmed

- Minimum role set: SUPER_ADMIN, ADMIN, TENANT_ADMIN, USER, VIEWER, GUEST
- Role hierarchy: SUPER_ADMIN > ADMIN > TENANT_ADMIN > USER > VIEWER > GUEST
- Permissions follow resource:action naming: users:read, users:write, billing:read, billing:write, admin:all
- Token role claims updated on next refresh; clients must refresh tokens after role changes
- OPA bundle deployed as sidecar or standalone policy engine; policies loaded from git
