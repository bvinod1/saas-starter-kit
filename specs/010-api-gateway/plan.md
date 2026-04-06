# Implementation Plan: API Gateway (Kong)

**Branch**: `010-api-gateway` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Kong is the single North-South ingress for all 13 features. It validates JWTs (backends never
re-validate), injects tenant headers (X-Tenant-ID, X-Organization-ID, X-User-ID), enforces
per-tenant/per-plan rate limiting, handles CORS, and applies WAF rules from staging upward.
Config is declarative YAML, version-controlled, and hot-reloadable.

## Technical Context

**Language/Version**: Kong 3.x (declarative YAML); Bash (deploy scripts)
**Primary Dependencies**: Kong JWT plugin, Rate Limiting, Request Transformer, CORS, OTel plugin
**Storage**: Kong DB-less (YAML) or Kong DB (PostgreSQL)
**Testing**: Kong integration tests, Postman/Newman collections, test-multi-tenant.sh
**Target Platform**: Linux server (Docker / Kubernetes ingress)
**Project Type**: North-South API Gateway
**Performance Goals**: < 5 ms latency overhead p99; > 10 000 req/s in production
**Constraints**: Config hot-reload via Admin API; no service restart on config change
**Scale/Scope**: All 13 features' endpoints; multi-tenant; 7 environments

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | Kong is the interface-first North-South boundary; all JWT validation happens here |
| II. Cloud Agnostic | PASS | Kong runs on any Kubernetes (EKS/GKE/AKS/kind) |
| V. Traffic-Aware | PASS | This IS the North-South boundary; TLS termination here |
| VI. Tenant-Centric | PASS | X-Tenant-ID injected at Kong; all downstream services receive it |
| VII. Observability | PASS | OTel trace root created at Kong; distributed trace spans initiated here |
| VIII. Security | PASS | TLS termination; JWT validation; WAF from staging up; DDoS from sandbox up |

## Project Structure

### Documentation (this feature)

```
specs/010-api-gateway/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/kong-admin.yaml
```

### Source Code (repository root)

```
api-gateway/
├── kong.yml
├── kong-multi-tenant.yml
├── kong-multi-tenant-enhanced.yml
├── kong-organisation.yml
├── kong-organizational-management.yml
├── deploy-multi-tenant.sh
└── test-multi-tenant.sh

frontend/src/pages/admin/ApiGatewayManagement.tsx
```

**Structure Decision**: All Kong config in api-gateway/. Kong is the only North-South boundary.

## Complexity Tracking

> No constitution violations.
