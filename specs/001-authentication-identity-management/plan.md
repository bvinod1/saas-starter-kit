# Implementation Plan: Authentication & Identity Management

**Branch**: `001-authentication-identity-management` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-authentication-identity-management/spec.md`

## Summary

Keycloak-backed authentication with local email/password and OAuth2 social login (Google, Apple,
Microsoft). Each organisation gets a dedicated Keycloak client at registration. The auth-service
orchestrates user creation in Keycloak, per-tenant PostgreSQL schema provisioning, and JWT token
issuance. Supports MFA (TOTP), account lockout, email verification, and password reset.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Spring Web, Keycloak Admin Client, JJWT, Spring Data JPA
**Storage**: PostgreSQL 15 (auth schema), Keycloak internal DB, Redis (session cache)
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: North-South web service (REST API)
**Performance Goals**: Login < 200 ms p95; registration < 2 s p95 (including Keycloak + DB)
**Constraints**: Password min 8 chars; configurable account lockout; token validation < 50 ms
**Scale/Scope**: 500 concurrent logins; per-tenant isolation in Keycloak + DB

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Keycloak via KeycloakService abstraction; OAuth via Spring Security OAuth2 client |
| II. Cloud Agnostic | PASS | Identity: Keycloak (local) / Cognito (AWS) / Firebase Auth (GCP) / Azure AD B2C |
| III. SCM/CI Agnostic | PASS | No SCM-specific logic in service code |
| V. Traffic-Aware | PASS | North-South REST + JWT; East-West propagates W3C Trace Context |
| VI. Tenant-Centric | PASS | Dedicated Keycloak client per org; tenant slug in token claims |
| VII. Observability | PASS | OTel instrumented; structured JSON logs with tenantId + correlationId; RED metrics |
| VIII. Security | PASS | JWT validated at Kong only; secrets via SecretsProvider |

## Project Structure

### Documentation (this feature)

```
specs/001-authentication-identity-management/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/auth-service.yaml
```

### Source Code (repository root)

```
backend/auth-service/
├── src/main/java/com/saasstarterkit/auth/
│   ├── config/           # SecurityConfig, OAuth2Config, KeycloakConfig
│   ├── controller/       # AuthController
│   ├── dto/              # LoginRequest, RegisterRequest, RegisterResponse, TokenResponse
│   ├── entity/           # User, Role
│   ├── exception/        # UserAlreadyExistsException, ValidationException
│   ├── repository/
│   ├── service/          # AuthService, KeycloakService, UserService
│   └── util/
└── src/test/java/

database/init/01-init-schema.sql
contracts/openapi/auth-service.yaml
```

**Structure Decision**: Single Spring Boot service. North-South REST. OpenAPI contract in `contracts/openapi/`.

## Complexity Tracking

> No constitution violations.
