# Data Model: Analytics & Observability

## OpenTelemetry Resource Attributes (all spans)
| Attribute | Value |
|-----------|-------|
| `service.name` | service identifier |
| `tenant.id` | UUID of current tenant |
| `deployment.environment` | dev / staging / prod / … |

## Entity: MetricDefinition (Prometheus labels)
Metrics emitted by each service follow naming: `saas_{service}_{metric}_{unit}`.

| Metric | Type | Labels |
|--------|------|--------|
| `saas_http_requests_total` | Counter | service, method, path, status, tenant_id |
| `saas_http_request_duration_seconds` | Histogram | service, method, path, tenant_id |
| `saas_active_users_gauge` | Gauge | tenant_id |
| `saas_subscription_mrr_cents` | Gauge | plan_id |
| `saas_notification_sent_total` | Counter | channel, status, tenant_id |

## Entity: TenantUsageSummary (PostgreSQL — analytics-service)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| period_start | DATE | |
| period_end | DATE | |
| active_users | INTEGER | |
| api_calls | BIGINT | |
| storage_bytes | BIGINT | |
| computed_at | TIMESTAMPTZ | |

## Entity: FeatureUsageEvent (time-series — future: TimescaleDB)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| user_id | UUID | Nullable |
| feature | VARCHAR(100) | |
| action | VARCHAR(50) | |
| occurred_at | TIMESTAMPTZ | |
| metadata | JSONB | |

## Log Schema (ElasticSearch index: `saas-logs-*`)
```json
{
  "timestamp": "ISO8601",
  "level": "INFO|WARN|ERROR",
  "service": "user-service",
  "tenant_id": "uuid",
  "trace_id": "hex",
  "span_id": "hex",
  "message": "...",
  "context": {}
}
```

## Sampling Strategy
| Environment | Traces sampled |
|-------------|---------------|
| dev | 100 % |
| staging | 100 % |
| prod | 10 % (errors always 100 %) |

## Indexes
- `tenant_usage_summary(tenant_id, period_start)` UNIQUE
- `feature_usage_events(tenant_id, occurred_at)`
- `feature_usage_events(feature, tenant_id, occurred_at)`

## Migration References
- No dedicated migration; analytics-service manages its own schema
