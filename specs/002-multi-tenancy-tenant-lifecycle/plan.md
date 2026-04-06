# Implementation Plan: Multi-Tenancy & Tenant Lifecycle Management

**Branch**: `002-multi-tenancy-tenant-lifecycle` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Schema-per-tenant isolation model. Every tenant gets a dedicated PostgreSQL schema
(tenant_{slug}) provisioned via create_tenant_schema() database function at registration.
The user-service exposes tenant lifecycle CRUD scoped to SUPER_ADMIN. X-Tenant-ID context
is injected at Kong and propagated through HTTP interceptor, MDC, and OTel spans.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Data JPA, Spring Web, PostgreSQL JDBC, Liquibase
**Storage**: PostgreSQL 15 (tenant_management schema + dynamic tenant schemas)
**Testing**: JUnit 5, Testcontainers (PostgreSQL)
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: North-South + East-West service
**Performance Goals**: Schema provisioning < 5 s; tenant context resolution < 5 ms overhead
**Constraints**: Zero cross-tenant data leakage; exclusive schema per tenant
**Scale/Scope**: Thousands of tenants; each with dedicated schema

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Tenant DB access via JPA repositories only |
| II. Cloud Agnostic | PASS | PostgreSQL (local/cloud); schema-per-tenant strategy configurable |
| V. Traffic-Aware | PASS | Tenant context resolved North-South at Kong; propagated East-West via headers |
| VI. Tenant-Centric | PASS | This IS the tenant isolation feature; every query scoped to tenant schema |
| VII. Observability | PASS | tenantId in every OTel span attribute and MDC log field |
| VIII. Security | PASS | SUPER_ADMIN role required for global tenant operations |

## Project Structure

### Documentation (this feature)

```
specs/002-multi-tenancy-tenant-lifecycle/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/tenant-service.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   ├── controller/       # TenantController
│   ├── entity/           # Tenant, TenantConfiguration, TenantUser
│   ├── repository/
│   ├── service/          # TenantService, TenantUserService
│   └── config/           # TenantContext (ThreadLocal), TenantInterceptor

database/init/02-multi-tenant-schema.sql
database/migrations/04-create-tenant-configurations.sql
contracts/openapi/tenant-service.yaml
```

**Structure Decision**: Tenant lifecycle in user-service. DB function provisions schemas dynamically.

## Complexity Tracking

> No constitution violations.
