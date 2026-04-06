# Tasks: API Gateway (Kong)

**Feature**: `010-api-gateway`
**Input**: `specs/010-api-gateway/`
**Config**: `api-gateway/`

## Phase 1: Setup

- [ ] T001 Verify `docker-compose.yml` Kong service uses `kong:3.x` image, `KONG_DATABASE=off`, mounts `api-gateway/kong-multi-tenant-enhanced.yml` as declarative config
- [ ] T002 [P] Create `api-gateway/base-config.yaml` — shared service and route definitions extracted from existing YAML files
- [ ] T003 [P] Document the YAML split strategy: `kong.yml` (base), `kong-multi-tenant.yml` (tenant plugins), `kong-organisation.yml` (org routes), `kong-multi-tenant-enhanced.yml` (full production config)

---

## Phase 2: Foundational

- [ ] T004 Define all backend service entries in `api-gateway/kong-multi-tenant-enhanced.yml`: `auth-service`, `user-service`, `billing-service`, `notification-service`, `analytics-service`, `organisation-service`, `tenant-service` with health check paths
- [ ] T005 [P] Configure JWT plugin globally in `kong-multi-tenant-enhanced.yml` — `key_claim_name: sub`, `claims_to_verify: exp, iat`
- [ ] T006 [P] Configure Request Transformer plugin globally — inject `X-Tenant-ID`, `X-User-ID`, `X-Organization-ID` from JWT claims into downstream headers
- [ ] T007 Configure CORS plugin — allowed origins: `localhost:3000`, production domain pattern; methods: GET, POST, PUT, PATCH, DELETE, OPTIONS

**Checkpoint**: Kong base config with JWT + tenant header injection ready

---

## Phase 3: User Story 1 + 3 — Request Routing & Multi-Tenant Context (Priority: P1) 🎯 MVP

**Goal**: Every authenticated request routed to correct service with X-Tenant-ID injected; invalid JWT returns 401.

**Independent Test**: `curl -H "Authorization: Bearer {valid_jwt}" http://localhost:8000/api/v1/users` returns 200 with X-Tenant-ID injected. `curl` with expired JWT returns 401 (no backend hit).

- [ ] T008 [US1] Define routes for all backend services in `kong-multi-tenant-enhanced.yml` with `strip_path: true` and service mappings
- [ ] T009 [US1] Configure JWT plugin per route to validate tenant claim matches `X-Tenant-ID` header
- [ ] T010 [US3] Add subdomain-based tenant resolver plugin config to `kong-multi-tenant.yml` — `{tenant}.platform.com` → injects `X-Tenant-ID`
- [ ] T011 [US3] Test multi-tenant routing with `api-gateway/test-multi-tenant.sh`

**Checkpoint**: JWT validation and tenant header injection independently testable

---

## Phase 4: User Story 2 — Rate Limiting (Priority: P1)

**Goal**: Requests exceeding plan-tier limits return 429; limits configurable per consumer.

**Independent Test**: Create Kong consumer for STARTER tier; exceed 300 req/min → 429 with Retry-After header. ENTERPRISE consumer allows 10 000 req/min.

- [ ] T012 [P] [US2] Add rate-limiting plugin to `kong-multi-tenant-enhanced.yml` with per-plan configs: starter (10/s, 300/min), pro (50/s, 2000/min), enterprise (200/s, 10000/min)
- [ ] T013 [US2] Define Kong Consumer entries in YAML for each plan tier
- [ ] T014 [US2] Add `limit_by: header` with `header_name: X-Tenant-ID` for tenant-scoped rate limiting
- [ ] T014b [US2] Add plan-tier rate limit enforcement via Kong consumer groups: map each tenant to a consumer group (STARTER/PROFESSIONAL/ENTERPRISE); STARTER consumers inherit 10 req/s limit, ENTERPRISE consumers 200 req/s; configuration stored in `kong-multi-tenant-enhanced.yml`
- [ ] T015 [US2] Add `api-gateway/test-rate-limiting.sh` smoke test script

**Checkpoint**: Rate limiting per plan independently testable

---

## Phase 5: User Story 4 + 5 — CORS & Admin Monitoring (Priority: P2)

**Goal**: CORS enforced for approved origins; admin views per-endpoint stats.

**Independent Test**: Request from `localhost:3000` returns CORS headers. Request from unknown origin returns 403. Admin API exposes routes and plugin stats.

- [ ] T016 [P] [US4] Configure CORS plugin in `kong-multi-tenant-enhanced.yml` with explicit `origins` list
- [ ] T017 [US4] Add preflight OPTIONS route handling with 204 response
- [ ] T018 [US5] Expose Kong Admin API on internal port 8001 only (not proxied via Kong itself); update `docker-compose.yml`
- [ ] T019 [US5] Add maintenance mode response plugin stub in `kong-multi-tenant-enhanced.yml`

**Checkpoint**: CORS and admin API independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T020 [P] Add WAF plugin config to `kong-multi-tenant-enhanced.yml` (active from staging: block OWASP Top 10 patterns)
- [ ] T020b Add DDoS protection configuration to `kong-multi-tenant-enhanced.yml`: connection rate-limit (50 connections/IP/min sandbox–UAT; 20 connections/IP/min pre-prod+); bot-detection plugin enabled from dev upward
- [ ] T020c Add separate strict rate-limit policy for `/api/v1/admin/**` routes: 5 req/s per IP; 429 with `Retry-After` header; log all admin 429s to audit trail
- [ ] T021 [P] Add TLS termination config notes to `api-gateway/README.md` (self-signed local, Let's Encrypt dev–UAT)
- [ ] T022 Run `api-gateway/test-multi-tenant.sh` full smoke test suite
- [ ] T023 Run `api-gateway/deploy-multi-tenant.sh` to validate config reload without restart

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T007): Depends on Phase 1
- **Phase 3** (T008–T011): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T012–T015): Depends on Phase 2
- **Phase 5** (T016–T019): Depends on Phase 2
- **Phase 6** (T020–T023): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working authenticated routing with tenant injection.
