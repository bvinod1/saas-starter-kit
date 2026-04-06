# Implementation Plan: Organisation Management

**Branch**: `004-organization-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Within each tenant, users create and manage organisations (teams/departments). A separate
organisation-service handles enterprise governance: tiers, usage limits, compliance settings,
onboarding tracking, and comprehensive audit logging (org-level + member-level). All org
operations require valid tenant context. Soft-delete throughout.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Data JPA, Spring Web, Spring Security
**Storage**: PostgreSQL 15 (tenant_{slug}.organizations, organizational_management schema)
**Testing**: JUnit 5, Testcontainers
**Target Platform**: Linux server
**Project Type**: North-South + East-West service
**Performance Goals**: Org list < 150 ms; audit log writes async (< 5 ms added latency)
**Constraints**: Soft-delete enforced; tenant context required for every request
**Scale/Scope**: Hundreds of orgs per tenant; thousands of members

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Audit emit via async service; no direct DB writes in controllers |
| II. Cloud Agnostic | PASS | JPA; no cloud-specific storage |
| V. Traffic-Aware | PASS | North-South REST; audit events async East-West |
| VI. Tenant-Centric | PASS | TenantContext required for every org operation |
| VII. Observability | PASS | Audit logs with risk_level scoring |
| VIII. Security | PASS | Audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/004-organization-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/organization-service.yaml
```

### Source Code (repository root)

```
backend/user-service/src/main/java/com/saasstarterkit/user/
├── controller/       # OrganizationController
├── entity/           # TenantOrganization, OrganizationMember
└── service/          # OrganizationService

backend/organisation-service/
└── src/main/           # Enterprise governance service

database/init/03-organizational-management-schema.sql
contracts/openapi/organization-service.yaml
```

**Structure Decision**: Basic org CRUD in user-service; enterprise governance in organisation-service.

## Complexity Tracking

> No constitution violations.
