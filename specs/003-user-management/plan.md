# Implementation Plan: User Management

**Branch**: `003-user-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Full user lifecycle management within a tenant. Admin CRUD via /api/admin/users; user-facing
profile management via /api/users. Users stored in both Keycloak (identity) and per-tenant
PostgreSQL schemas (profile/preferences). Dynamic user profile schemas with immutable versioning
(draft -> published -> deprecated) allow organisations to define custom user attributes rendered
dynamically in the frontend.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x; TypeScript React 18 (admin UI)
**Primary Dependencies**: Spring Data JPA, Spring Security, Spring Web, Keycloak Admin Client
**Storage**: PostgreSQL 15 (tenant_{slug}.users, OrgSchema, UserProfileValues as JSONB)
**Testing**: JUnit 5, Testcontainers, React Testing Library
**Target Platform**: Linux server + browser
**Project Type**: North-South REST API + React SPA admin panel
**Performance Goals**: User list < 200 ms for 10 000 users; profile update immediately consistent
**Constraints**: Tenant-scoped queries only; schema version immutable once published
**Scale/Scope**: 10 000+ users per tenant

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Keycloak user ops via KeycloakService; no Admin Client in controllers |
| II. Cloud Agnostic | PASS | Identity via identity.interface; storage via JPA |
| V. Traffic-Aware | PASS | North-South REST for CRUD; OTel tracing per user operation |
| VI. Tenant-Centric | PASS | All queries tenant-scoped; dynamic schemas per org (NON-NEGOTIABLE in constitution) |
| VII. Observability | PASS | LoggingService + TracingService wired into UserController |
| VIII. Security | PASS | @PreAuthorize(hasRole(ADMIN)) on all admin endpoints |

## Project Structure

### Documentation (this feature)

```
specs/003-user-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/user-service.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   ├── controller/       # AdminController, UserController
│   ├── domain/           # OrgSchema, UserProfileValues
│   ├── dto/              # AdminUserDTO, UserProfileDTO, CreateUserRequest, UpdateUserRequest
│   ├── entity/           # User, TenantUser, UserRole, Role
│   ├── repository/
│   ├── service/          # UserService, AdminService, LoggingService, TracingService
│   └── security/

frontend/src/pages/admin/UserManagement.tsx
contracts/openapi/user-service.yaml
```

**Structure Decision**: user-service owns user lifecycle. Dynamic schema system adds OrgSchema entity.

## Complexity Tracking

> No constitution violations.
