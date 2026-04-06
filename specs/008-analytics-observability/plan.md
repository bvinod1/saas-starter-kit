# Implementation Plan: Analytics & Observability

**Branch**: `008-analytics-observability` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

OTel is the single observability standard. Every service instruments traces, metrics, and structured
JSON logs. Prometheus scrapes RED metrics; Grafana provides dashboards; Alertmanager routes by
severity and environment. ELK Stack provides centralised log aggregation with tenant-scoped indices.
Analytics service captures UserActivity and TenantMetrics for usage and billing inputs.

## Technical Context

**Language/Version**: Java 17 (analytics-service); YAML/config (infrastructure components)
**Primary Dependencies**: OTel SDK (Java), Micrometer (Prometheus), Logstash, Elasticsearch, Spring Data JPA
**Storage**: PostgreSQL (analytics), Elasticsearch (logs), Prometheus TSDB (metrics)
**Testing**: JUnit 5, OTel testing SDK, Testcontainers
**Target Platform**: Linux server (Docker / Kubernetes)
**Project Type**: Cross-cutting observability concern + analytics microservice
**Performance Goals**: Trace overhead < 2 ms; log ingestion > 10 000 events/sec
**Constraints**: OTel backend switchable via config/observability.yaml without code change
**Scale/Scope**: All services + all tenants; 5% sampling in production

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | OTel SDK abstracts tracing backend; Micrometer abstracts metrics backend |
| II. Cloud Agnostic | PASS | Backend: Jaeger (local) / X-Ray (AWS) / Cloud Trace (GCP) / Azure Monitor |
| V. Traffic-Aware | PASS | Trace root created at Kong (North-South); W3C Trace Context propagated East-West |
| VI. Tenant-Centric | PASS | tenantId in every span attribute, log field, Elasticsearch index |
| VII. Observability | PASS | This IS the observability feature |
| VIII. Security | PASS | PII masked in non-production; audit logs immutable in production |

## Project Structure

### Documentation (this feature)

```
specs/008-analytics-observability/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/analytics-events.yaml
```

### Source Code (repository root)

```
backend/analytics-service/
├── src/main/java/com/saasstarterkit/analytics/
│   ├── entity/           # UserActivity, TenantMetrics
│   └── repository/

infrastructure/
├── prometheus/prometheus.yml
├── alertmanager/alertmanager.yml
├── otel-collector/otel-collector-config.yaml
├── kibana/tenant-rbac-config.yml
└── logstash/

frontend/src/pages/admin/
├── MonitoringDashboard.tsx
└── LoggingDashboard.tsx

config/observability.yaml
```

**Structure Decision**: analytics-service for business metrics; infrastructure/ for observability stack.

## Complexity Tracking

> No constitution violations.
