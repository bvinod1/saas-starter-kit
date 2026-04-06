# Implementation Plan: Role-Based Access Control (RBAC)

**Branch**: `005-role-based-access-control` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Granular RBAC backed by Keycloak roles and local role-permission mappings (resource:action).
Spring Security @PreAuthorize annotations enforce role checks at every controller method.
OPA policies govern East-West service authorisation. Permissions cached via Spring Cache.
Role changes take effect on next token refresh.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Keycloak Spring Boot Adapter, OPA (East-West)
**Storage**: PostgreSQL 15 (auth.roles, auth.permissions, auth.user_roles, auth.role_permissions)
**Testing**: JUnit 5, Spring Security Test, MockMvc
**Target Platform**: Linux server
**Project Type**: Cross-cutting concern (all backend services)
**Performance Goals**: Permission check < 5 ms via Spring Cache
**Constraints**: Role changes reflected on next token refresh only; OPA deny-all by default
**Scale/Scope**: 5+ default roles; 50+ permissions; cached per request

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | @PreAuthorize declarative; OPA as policy engine interface |
| V. Traffic-Aware | PASS | JWT role claims validated North-South at Kong; OPA East-West |
| VI. Tenant-Centric | PASS | Roles are tenant-scoped in Keycloak realms |
| VII. Observability | PASS | Authorization failures logged with tenantId + userId |
| VIII. Security | PASS | OPA deny-all default; explicit allow per service pair required |

## Project Structure

### Documentation (this feature)

```
specs/005-role-based-access-control/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/rbac-events.yaml
```

### Source Code (repository root)

```
backend/auth-service/src/main/java/com/saasstarterkit/auth/
├── entity/           # Role
├── config/           # SecurityConfig
└── repository/       # RoleRepository

backend/user-service/src/main/java/com/saasstarterkit/user/
├── controller/       # AdminController (role endpoints)
└── service/          # AdminService (role management)

database/init/01-init-schema.sql   # auth.roles, auth.permissions, auth.role_permissions
```

**Structure Decision**: Role data in auth-service; admin role assignment ops in user-service.

## Complexity Tracking

> No constitution violations.
