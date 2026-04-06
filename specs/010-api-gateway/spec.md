# Feature Specification: API Gateway (Kong)

**Feature Branch**: `010-api-gateway`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Authenticated request routing (Priority: P1)

Every inbound request passes through Kong. Kong validates the JWT, injects tenant headers, and
forwards the request to the appropriate backend service.

**Acceptance Scenarios**:
1. **Given** valid JWT in Authorization header, **When** request hits Kong, **Then** forwarded to backend with X-Tenant-ID injected.
2. **Given** expired/invalid JWT, **When** request hits Kong, **Then** 401 returned without reaching backend.
3. **Given** cross-tenant request attempt, **When** Kong validates tenant headers, **Then** 403 if tenant mismatch.

---

### User Story 2 — Rate limiting enforcement (Priority: P1)

Requests exceeding configured rate limits per consumer/plan are rejected.

**Acceptance Scenarios**:
1. **Given** 1 001st request within 1 hour for standard tier, **When** request hits Kong, **Then** 429 Too Many Requests.
2. **Given** Enterprise plan, **When** configured with higher rate limit, **Then** limit applied per tenant.

---

### User Story 3 — Multi-tenant routing (Priority: P1)

Kong routes requests to the correct service instance or applies tenant-specific plugins based on X-Tenant-ID.

**Acceptance Scenarios**:
1. **Given** X-Tenant-ID: tenant_123, **When** request hits Kong, **Then** downstream headers include X-Tenant-ID, X-Organization-ID, X-User-ID.
2. **Given** subdomain-based tenant (acme.platform.com), **When** Kong resolves, **Then** correct X-Tenant-ID injected.

---

### User Story 4 — CORS enforcement (Priority: P2)

Kong enforces CORS policy allowing only approved frontend origins.

**Acceptance Scenarios**:
1. **Given** request from localhost:3000, **When** CORS plugin runs, **Then** Access-Control-Allow-Origin returned.
2. **Given** request from unauthorized origin, **When** CORS plugin runs, **Then** 403.

---

### User Story 5 — Admin monitors endpoint stats (Priority: P2)

An admin views per-endpoint request counts, error rates, and response times via the admin UI.

**Acceptance Scenarios**:
1. **Given** admin, **When** API gateway management page loaded, **Then** 24h request counts, error rates, avg response time shown.
2. **Given** endpoint in maintenance mode, **When** request arrives, **Then** 503 with maintenance message.

---

### Edge Cases
- What happens when Kong's DB is unreachable (DB-less mode)?
- How are rate limit counters shared across Kong instances?
- How is Kong config versioned and deployed?

## Requirements

### Functional Requirements
- **FR-001**: Kong MUST validate JWT tokens at the gateway; backends MUST NOT re-validate.
- **FR-002**: Kong MUST inject X-Tenant-ID, X-Organization-ID, X-User-ID headers downstream.
- **FR-003**: Kong MUST enforce per-tenant and per-plan rate limiting.
- **FR-004**: Kong MUST support subdomain and header based tenant resolution.
- **FR-005**: Kong MUST enforce CORS for approved frontend origins.
- **FR-006**: Kong config MUST be declarative (YAML) and version-controlled.
- **FR-007**: Kong configurations: kong.yml (base), kong-multi-tenant.yml, kong-organisation.yml MUST all be maintained.
- **FR-008**: Kong MUST support plan-tiered access (e.g., analytics endpoints restricted to Professional+).
- **FR-009**: WAF rules MUST be applied from staging upward; full WAF in production.
- **FR-010**: Kong MUST support DDoS protection from sandbox upward.
- **FR-011**: Rate limit rules MUST be manageable from admin UI or API.
- **FR-012**: TLS termination MUST occur at Kong (self-signed local, Let's Encrypt dev–UAT, full TLS pre-prod+).

### Key Entities
- **Route**: path pattern, method, upstream service
- **Plugin**: rate-limiting, jwt, cors, request-transformer, response-transformer
- **Consumer**: tenant or user identity for rate limit buckets
- **RateLimitRule**: pattern, limit, burst, tenant scope, status

### Non-Functional Requirements
- Kong MUST add < 5 ms latency overhead p99
- Kong MUST support > 10 000 req/s in production
- Config changes MUST be applied without service restart (hot reload via Admin API)
