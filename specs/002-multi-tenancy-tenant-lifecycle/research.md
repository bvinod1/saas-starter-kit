# Research: Multi-Tenancy & Tenant Lifecycle Management

## Decision: Schema-per-Tenant Isolation

**Decision**: Each tenant gets a dedicated PostgreSQL schema (tenant_{slug}).
**Rationale**: Strong data isolation without separate DB instances. Schema-level pg_dump for tenant backup. Natural row filtering without additional WHERE clauses. Connection pooling still works (pgBouncer can route via search_path). Simpler GDPR erasure (drop tenant schema).
**Alternatives considered**:
- DB-per-tenant: Operationally prohibitive at scale (connection limits, $Cost, migration overhead)
- Row-level security: More complex, risk of misconfigured RLS policies leaking data

## Decision: Dynamic Schema Provisioning via DB Function

**Decision**: create_tenant_schema(slug) stored procedure creates all required tables in one transaction.
**Rationale**: Atomic provisioning; easier to version and test; callable from Java via JDBC.
**Alternatives considered**:
- Liquibase per-tenant: Increases migration complexity exponentially with tenant count

## Decision: Tenant Context via X-Tenant-ID Header

**Decision**: Tenant identified by X-Tenant-ID header injected at Kong, propagated via ThreadLocal (TenantContext) and MDC.
**Rationale**: Explicit context propagation; no ambiguity; works for REST, WebSocket, and async jobs.
**Alternatives considered**:
- Subdomain routing: More complex DNS management; doesn't work in all deployment contexts
- JWT claim only: Requires token validation before any tenant query; adds latency

## Decision: master Registry in tenant_management Schema

**Decision**: Shared tenant_management schema holds tenant master registry, schema registry, and cross-tenant user registry.
**Rationale**: SUPER_ADMIN needs cross-tenant visibility; billing needs tenant->plan mapping; health checks need tenant status.

## Best Practices Confirmed

- Tenant slug: lowercase, hyphen-separated, UUID suffix for uniqueness (max 50 chars)
- set_config('app.current_tenant', tenant_id, false) in PostgreSQL for RLS support
- TenantContext.getCurrentTenant() always checked before any DB query; returns 400 if missing
- search_path set per connection to tenant schema; reset on connection return to pool
- OTel span attribute: tenant.id attached to every span
