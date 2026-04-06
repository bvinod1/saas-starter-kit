# Tasks: Analytics & Observability

**Feature**: `008-analytics-observability`
**Input**: `specs/008-analytics-observability/`
**Source**: `backend/analytics-service/`, `infrastructure/`

## Phase 1: Setup

- [ ] T001 Add OTel and Micrometer dependencies to all service pom.xml files: `opentelemetry-spring-starter`, `micrometer-registry-prometheus`, `logstash-logback-encoder`; use `opentelemetry-bom` in `<dependencyManagement>` to pin a single OTel version across all modules (avoids version conflicts); add BOM once to root `pom.xml` if using multi-module build
- [ ] T002 [P] Configure `infrastructure/otel-collector/otel-collector-config.yaml` (OTLP receivers, Prometheus exporter, Jaeger exporter)
- [ ] T003 [P] Configure `infrastructure/prometheus/prometheus.yml` (scrape configs for all services with per-service job labels)
- [ ] T004 [P] Configure `infrastructure/logstash/pipeline/` (parse JSON logs, add tenantId, route to tenant-scoped ES index `saas-logs-{tenant_id}-*`)

---

## Phase 2: Foundational

- [ ] T005 Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/OtelConfig.java` (resource attributes: service.name, tenant.id, deployment.environment)
- [ ] T005a [P] Add `sentry-spring-boot-starter-jakarta` dependency to ALL backend service `pom.xml` files (auth, user, tenant, organisation, billing, notification, analytics)
- [ ] T005b [P] Configure Sentry DSN in each service `application.yml` via `${saas.secrets.sentry_dsn}` (injected through `SecretsProvider`); set `sentry.traces-sample-rate` matching OTel sampling per environment (1.0 local/dev, 0.5 sandbox/staging, 0.05 prod)
- [ ] T005c Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/config/SentryConfig.java` — configure `beforeSend` hook to attach `tenantId` and `userId` as Sentry tags from MDC; apply same pattern to all services
- [ ] T005d Add Sentry service to `docker-compose.yml` using `sentry:self-hosted` image (local environment); add `redis` Sentry dependency; expose port 9000
- [ ] T005e [P] Create `config/observability.yaml` — canonical observability profile: sampling rates per environment (local: 1.0, dev: 1.0, sandbox: 0.5, staging: 0.5, uat: 0.5, pre-prod: 0.1, prod: 0.05), alert thresholds (cpu_warn: 75%, error_rate_warn: 1%), Sentry DSN key reference; all services and OTel collector read from this file
- [ ] T006 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/UserActivity.java` JPA entity (user_id, tenant_id, action, metadata JSONB, timestamp)
- [ ] T007 [P] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/entity/TenantMetrics.java` JPA entity (tenant_id, period_start, period_end, api_calls, storage_gb, active_users)
- [ ] T008 [P] Create repositories: `UserActivityRepository.java`, `TenantMetricsRepository.java`
- [ ] T009 Add `otel.instrumentation.common.db-statement-sanitizer.enabled=true` and sampling config to all `application.yml` files (100% local/dev, 50% sandbox/staging, 5% prod — errors always 100%); values driven by `config/observability.yaml`

**Checkpoint**: OTel pipelines, Prometheus, and Logstash ready for all services

> **Provider Contract**: Any cache access (e.g., metrics caching) MUST go through `CacheProvider` from `shared-libs/providers/`.

---

## Phase 3: User Story 1 + 2 — Service Health & Distributed Tracing (Priority: P1) 🎯 MVP

**Goal**: All services expose health + RED metrics scraped by Prometheus; every cross-service request carries a single trace ID with tenant.id span attribute.

**Independent Test**: `curl /actuator/health` on every service returns UP. Prometheus scrapes metrics with `tenant_id` label. Single request generates linked spans across auth → user → notification.

- [ ] T010 [P] [US1] Add `spring-boot-actuator` with `/actuator/health`, `/actuator/prometheus` endpoints to all service `application.yml` and `pom.xml`
- [ ] T011 [P] [US1] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/metrics/ServiceMetrics.java` — define standard counters: `saas_http_requests_total`, `saas_http_request_duration_seconds` histogram
- [ ] T012 [US2] Add `W3CTraceContextPropagator` as default propagator in all service `OtelConfig.java` files
- [ ] T013 [US2] Add `tenant.id` as span attribute via `TenantContext` in all service OTel filters (read from `TenantInterceptor`)
- [ ] T014 [US1] Configure `infrastructure/alertmanager/alertmanager.yml` — routes: dev/staging → Slack, prod → PagerDuty
- [ ] T015 [US1] Add Prometheus alert rules file `infrastructure/prometheus/alerts.yaml` (CPU > 75%, error_rate > 1%, service down)

**Checkpoint**: Health, metrics, and distributed tracing independently verifiable

---

## Phase 4: User Story 3 — Centralised Log Aggregation (Priority: P2)

**Goal**: All service logs land in Elasticsearch with tenantId field; Kibana filters by tenant.

**Independent Test**: Emit log in any service → query ES `saas-logs-*` index → document has `tenant_id`. Kibana filter by tenantId returns only that tenant's logs.

- [ ] T016 [P] [US3] Add `logstash-logback-encoder` and `logback-spring.xml` with JSON layout to each service's `src/main/resources/`
- [ ] T017 [US3] Configure `infrastructure/logstash/config/logstash.yml` and pipeline to add `tenant_id` field from log event
- [ ] T018 [US3] Set Kibana index pattern and saved search in `infrastructure/kibana/tenant-rbac-config.yml`

**Checkpoint**: Log aggregation independently testable

---

## Phase 5: User Story 4 + 5 — Usage Analytics & Alert Routing (Priority: P2)

**Goal**: analytics-service records per-tenant usage; Alertmanager routes by severity and environment.

**Independent Test**: RabbitMQ feature usage event → `UserActivity` row exists in DB. TenantMetrics aggregated for billing period. Error alert in prod → PagerDuty; same in dev → Slack only.

- [ ] T019 [P] [US4] Implement `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/service/AnalyticsService.java` — `recordActivity(event)`, `computeTenantMetrics(tenantId, period)`
- [ ] T020 [US4] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/listener/FeatureUsageEventListener.java` — RabbitMQ consumer for `analytics.feature.usage` queue
- [ ] T021 [US4] Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/controller/AnalyticsController.java` with `GET /api/v1/analytics/tenants/{tenantId}/usage`
- [ ] T022 [US5] Update `infrastructure/alertmanager/alertmanager.yml` with environment-based inhibit rules and receiver grouping

**Checkpoint**: Usage analytics and alert routing independently testable

---

## Phase 5b: User Data Erasure Consumer (Cross-Cutting)

**Goal**: analytics-service consumes `UserDataErasureEvent` and purges or anonymises `UserActivity` records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` → `UserActivity` rows for userId deleted (or userId field set to anonymised token); TenantMetrics aggregates unaffected.

- [ ] TErasure-008a Add `spring-kafka` to `backend/analytics-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-008b Create `backend/analytics-service/src/main/java/com/saasstarterkit/analytics/kafka/ErasureEventConsumer.java` — delete or hash `UserActivity.user_id` for erased user; log anonymised count as audit entry

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T023 [P] Create Grafana dashboard provisioning files in `infrastructure/grafana/dashboards/` (service health, tenant usage)
- [ ] T024 [P] Add OTel sampling config to `infrastructure/otel-collector/otel-collector-config.yaml` — tail sampling: always-on for errors
- [ ] T025 Run `quickstart.md` smoke tests (health endpoints, trace propagation, ES log ingestion, metrics scrape)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T004): No dependencies
- **Phase 2** (T005–T009): Depends on Phase 1
- **Phase 3** (T010–T015): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T016–T018): Depends on Phase 2
- **Phase 5** (T019–T022): Depends on Phase 2
- **Phase 6** (T023–T025): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working health monitoring and distributed tracing.
