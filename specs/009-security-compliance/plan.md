# Implementation Plan: Security & Compliance

**Branch**: `009-security-compliance` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

GDPR consent management (12 consent types), data subject rights (16 request types), HIPAA PHI
protection with AES-256 encryption and SHA-256 hash verification, security event detection with
risk scoring, and compliance framework dashboards (GDPR/HIPAA/SOX/PCI-DSS). UserDataErasureEvent
broadcast via Kafka triggers purge handlers in all services. Secrets via SecretsProvider.
Audit logs immutable in production.

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring Security, Javax Crypto (AES-256), Spring Data JPA, Kafka (erasure events)
**Storage**: PostgreSQL (data_consents, data_subject_requests, protected_health_information)
**Testing**: JUnit 5, Testcontainers (Kafka, PostgreSQL)
**Target Platform**: Linux server
**Project Type**: North-South + East-West service (compliance + security events)
**Performance Goals**: Erasure requests complete within 30-day GDPR legal window
**Constraints**: PHI decryption requires access purpose logging; audit logs write-only in prod
**Scale/Scope**: All tenants; compliance framework selectable per tenant

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | SecretsProvider for encryption keys; no hardcoded secrets anywhere |
| II. Cloud Agnostic | PASS | Vault (local), Secrets Manager (AWS), Key Vault (Azure) via SecretsProvider |
| V. Traffic-Aware | PASS | Consent API North-South; UserDataErasureEvent East-West Kafka |
| VI. Tenant-Centric | PASS | Every consent/PHI record carries tenant_id; compliance selectable per tenant |
| VII. Observability | PASS | Audit logs are immutable compliance observability; security alerts with risk_score |
| VIII. Security | PASS | AES-256 encryption; SAST/DAST in CI; audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/009-security-compliance/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/compliance-events.yaml
```

### Source Code (repository root)

```
backend/user-service/
├── src/main/java/com/saasstarterkit/user/
│   └── temp-backup/      # ComplianceController.java, ComplianceService.java (to be promoted)

database/migrations/05-gdpr-hipaa-compliance.sql

frontend/src/pages/admin/SecurityCompliance.tsx
contracts/asyncapi/compliance-events.yaml   # UserDataErasureEvent AsyncAPI schema
```

**Structure Decision**: Compliance logic promoted into user-service. Erasure event is East-West Kafka.

## Complexity Tracking

> No constitution violations.
