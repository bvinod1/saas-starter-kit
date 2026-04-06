# Feature Specification: Analytics & Observability

**Feature Branch**: `008-analytics-observability`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Service health monitoring (Priority: P1)

An admin views real-time health of all microservices: uptime %, response time, error rate, CPU/memory/disk.

**Acceptance Scenarios**:
1. **Given** Prometheus scraping all services, **When** admin opens monitoring dashboard, **Then** per-service health cards shown.
2. **Given** Notification Service CPU > 75%, **When** alert rule fires, **Then** Alertmanager sends warning to Slack.
3. **Given** service unreachable, **When** health check fails, **Then** status shows "unhealthy" in dashboard.

---

### User Story 2 — Distributed tracing (Priority: P1)

Each request across services carries W3C Trace Context. Traces are exported to Jaeger/OTEL collector,
allowing full request journey reconstruction.

**Acceptance Scenarios**:
1. **Given** user request entering Kong, **When** forwarded to backend services, **Then** single trace ID links all spans.
2. **Given** tenant context, **When** trace created, **Then** X-Tenant-ID present as span attribute.

---

### User Story 3 — Centralised log aggregation (Priority: P2)

All services emit structured JSON logs. Logstash ships them to Elasticsearch with tenant-scoped indices.

**Acceptance Scenarios**:
1. **Given** log emitted by any service, **When** indexed in Elasticsearch, **Then** tenantId field present.
2. **Given** admin on Kibana, **When** filtering by tenant, **Then** only that tenant's logs returned.

---

### User Story 4 — Tenant metrics and usage analytics (Priority: P2)

The analytics service records per-tenant UserActivity and TenantMetrics for capacity planning and billing.

**Acceptance Scenarios**:
1. **Given** user performs action, **When** UserActivity recorded, **Then** tenant_id and user_id present.
2. **Given** billing cycle end, **When** TenantMetrics queried, **Then** aggregated API calls, storage, user counts returned.

---

### User Story 5 — Alert routing and incident response (Priority: P2)

Alertmanager routes fired alerts to appropriate channels (Slack dev, PagerDuty prod) based on severity and environment.

**Acceptance Scenarios**:
1. **Given** error_rate > 1% in production, **When** alert fires, **Then** PagerDuty notified immediately.
2. **Given** warning alert in dev, **When** alert fires, **Then** only Slack notified.

---

### Edge Cases
- What if OTel collector is unreachable?
- How are high-cardinality tenant metrics stored efficiently?
- How are sampling rates adjusted per environment?

## Requirements

### Functional Requirements
- **FR-001**: Every service MUST instrument OTel traces, metrics, and structured JSON logs.
- **FR-002**: All services MUST expose RED metrics (Rate, Errors, Duration) per endpoint.
- **FR-003**: All services MUST have liveness and readiness probes.
- **FR-004**: Traces MUST use W3C Trace Context propagation across East-West calls.
- **FR-005**: Tenant context MUST be present as a span attribute in every trace.
- **FR-006**: Logs MUST be structured JSON with tenantId, correlationId, service, level, message.
- **FR-007**: Elasticsearch indices MUST be tenant-scoped.
- **FR-008**: Prometheus MUST scrape all services; Grafana MUST provide dashboards.
- **FR-009**: Alertmanager MUST route by severity and environment.
- **FR-010**: Analytics service MUST record UserActivity and TenantMetrics entities.
- **FR-011**: Sampling rates: 100% local/dev, 50% sandbox/staging, 5% production.
- **FR-012**: Sentry MUST be used for error tracking across all environments (local, dev, staging, prod); DSN injected via `SecretsProvider`; `tenantId` and `userId` MUST be attached as Sentry tags.
- **FR-012**: OTel backend MUST be switchable via `config/observability.yaml` without code change.

### Key Entities
- **UserActivity**: user_id, tenant_id, action, metadata, timestamp
- **TenantMetrics**: tenant_id, period_start, period_end, api_calls, storage_gb, active_users
- **OTelSpan**: trace_id, span_id, service, operation, duration, tenant_id (attribute)

### Non-Functional Requirements
- Trace overhead MUST be < 2 ms per request
- Metrics storage MUST be > 30 days by default
- Log ingestion MUST handle > 10 000 events/second across all services
