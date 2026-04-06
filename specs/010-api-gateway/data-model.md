# Data Model: API Gateway

> Kong operates DB-less; all state is in declarative YAML. No relational model.

## Kong Entity Hierarchy
```
Service
 └── Route
      └── Plugin (per-route or global)
Consumer (represents a tenant API key)
Upstream
 └── Target (backend instance)
```

## Service Definition Schema
| Field | Type | Example |
|-------|------|---------|
| name | string | `auth-service` |
| url | string | `http://auth-service:8080` |
| connect_timeout | integer ms | 5000 |
| read_timeout | integer ms | 30000 |
| retries | integer | 2 |

## Route Definition Schema
| Field | Type | Example |
|-------|------|---------|
| name | string | `auth-routes` |
| paths | string[] | `["/api/v1/auth"]` |
| methods | string[] | `["GET","POST"]` |
| strip_path | boolean | true |
| service.name | string | `auth-service` |

## Plugin Schema (Rate Limiting)
| Parameter | Value |
|-----------|-------|
| plugin | `rate-limiting` |
| config.second | plan-tier dependent |
| config.minute | plan-tier dependent |
| config.policy | `local` |
| config.limit_by | `header` (X-Tenant-ID) |

## Consumer Schema (Tenant API Key)
| Field | Type | Notes |
|-------|------|-------|
| username | string | `tenant-{tenant_id}` |
| custom_id | string | Tenant UUID |
| keyauth_credentials[].key | string | SHA-256 hashed API key |

## Upstream Health Check Configuration
```yaml
healthchecks:
  active:
    http_path: /actuator/health
    interval: 10
    healthy.successes: 2
    unhealthy.http_failures: 3
```

## Per-Plan Rate Limits
| Plan | Requests/sec | Requests/min |
|------|-------------|--------------|
| starter | 10 | 300 |
| pro | 50 | 2000 |
| enterprise | 200 | 10000 |

## Config Files
- `api-gateway/kong-multi-tenant-enhanced.yml` – production config
- `api-gateway/kong-multi-tenant.yml` – base config
- `api-gateway/kong-organisation.yml` – org-specific routes
