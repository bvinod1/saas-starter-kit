# Research: Analytics & Observability

## Decision: OpenTelemetry as Single Observability Standard

**Decision**: All services use OTel SDK for traces, metrics, and logs. Backend (Jaeger/Prometheus/Elasticsearch) configurable per environment via config/observability.yaml.
**Rationale**: Constitution VII is NON-NEGOTIABLE. OTel is the CNCF standard; vendor-neutral; backend-agnostic. One instrumentation, swap exporters per environment.

## Decision: Tenant ID as OTel Span Attribute

**Decision**: tenant.id added as baggage + span attribute on every OTel span. Propagated via W3C Trace Context + W3C Baggage headers East-West.
**Rationale**: Enables tenant-scoped trace queries in Jaeger/Grafana Tempo. Supports cross-service debugging within a tenant's trace.

## Decision: Elasticsearch Tenant-Scoped Index Pattern

**Decision**: Log index names follow pattern: logs-{tenantId}-{YYYY-MM}. Kibana RBAC configured per tenant.
**Rationale**: Complete log isolation per tenant. Kibana tenant RBAC prevents cross-tenant log access. ILM (Index Lifecycle Management) per tenant index.

## Decision: Per-Environment Sampling Rates

**Decision**: 100% sampling (local/dev), 50% (sandbox/staging/UAT), 5% (production). Configurable via config/observability.yaml.
**Rationale**: Full sampling in dev provides complete debugging data. Production 5% sampling controls storage cost and collector overhead while maintaining statistical validity.

## Best Practices Confirmed

- RED metrics (Rate, Errors, Duration) per endpoint via Micrometer + Prometheus scrape
- Liveness = /actuator/health/liveness; Readiness = /actuator/health/readiness (Spring Boot Actuator)
- Grafana dashboards for: service overview, per-tenant usage, billing metrics, security events
- Alertmanager routes: warning -> Slack; critical -> PagerDuty (production only)
- UserActivity records: user_id, tenant_id, action, ip_address, user_agent, timestamp
