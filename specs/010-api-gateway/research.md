# Research: API Gateway (Kong)

## Decision: Kong as Single North-South Gateway

**Decision**: Kong handles: JWT validation, rate limiting, CORS, request transformation (header injection), WAF (Kong + ModSecurity plugin), tenant routing.
**Rationale**: Centralised security policy enforcement. No per-service JWT validation. Token re-validation downstream is explicitly prohibited by constitution VIII. Kong is cloud-agnostic (runs on any K8s).

## Decision: Declarative DB-Less Kong Configuration

**Decision**: Kong configured via YAML (kong.yml, kong-multi-tenant.yml, etc.). DB-less mode for faster recovery and GitOps compatibility.
**Rationale**: Config-as-code: YAML lives in version control. No Kong DB to maintain. Config applies on Kong restart or via Admin API deck sync.

## Decision: Plan-Tiered Rate Limiting

**Decision**: Rate limits enforced per Kong Consumer (tenant). Consumer tagged with plan tier. Rate limit plugin applies per-consumer limits from environment config.
**Rationale**: Different plans have different API call entitlements (max_api_calls_per_month in plan). Kong Consumer is the natural unit for rate limit buckets.

## Decision: Request Transformer for Tenant Header Injection

**Decision**: Kong Request Transformer plugin adds X-Tenant-ID, X-Organization-ID, X-User-ID headers derived from JWT claims.
**Rationale**: Backends trust these headers implicitly (injected by trusted gateway only). Simplifies downstream auth — no JWT re-parsing needed.

## Best Practices Confirmed

- JWT validation: RS256 asymmetric signing; public key fetched from Keycloak JWKS endpoint
- CORS: explicit allowed origins list (no wildcard in production)
- TLS: self-signed cert (local), Let's Encrypt (dev-UAT), commercial cert (pre-prod+)
- WAF: kong-plugin-modsecurity or NGINX ModSecurity; basic rules sandbox-UAT; full OWASP CRS production
- Health check route: /api/health (public, no JWT required) -> each service's actuator
