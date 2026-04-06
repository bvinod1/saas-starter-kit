# Tasks: Tenant Configuration Management

**Feature**: `011-tenant-configuration-management`
**Input**: `specs/011-tenant-configuration-management/`
**Source**: `backend/tenant-service/`

## Phase 1: Setup

- [ ] T001 Add dependencies to `backend/tenant-service/pom.xml` (spring-data-jpa, spring-cache, spring-data-redis, vault-spring-starter)
- [ ] T002 [P] Configure `backend/tenant-service/src/main/resources/application.yml` — Redis cache, Vault address, AES key path, cache TTL 300 s
- [ ] T003 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/config/CacheConfig.java` (Spring Cache + Redis, cache name `config`) — consume `CacheProvider` from `shared-libs/providers/`; do not duplicate cache logic in service

---

## Phase 2: Foundational

- [ ] T004 Create `database/migrations/04-create-tenant-configurations.sql` — `tenant_configurations` table (tenant_id, config_key, config_value TEXT, environment, is_encrypted, is_active, data_type, updated_by)
- [ ] T005 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/entity/TenantConfiguration.java` JPA entity
- [ ] T006 [P] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/repository/TenantConfigurationRepository.java` (findByTenantIdAndConfigKeyAndEnvironment)
- [ ] T007 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/security/AesConfigEncryptionService.java` — AES-256-GCM; key from Vault via `SecretsProvider`
- [ ] T008 Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/ConfigResolver.java` — resolves config through hierarchy: tenant → environment-global → global default

**Checkpoint**: Config entity, encryption, and resolver ready

---

## Phase 3: User Story 1 + 2 — Hierarchical Resolution & Encrypted Secrets (Priority: P1) 🎯 MVP

**Goal**: Service requests config → tenant value returned if exists, else env global, else global default. Encrypted values decrypted transparently.

**Independent Test**: Set tenant config for key X → `getConfiguration(tenantId, X, env)` returns tenant value. Set env config only → env value returned. No config → null with WARNING log. Encrypted value → plaintext returned.

- [ ] T009 [P] [US1] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/dto/ConfigResponse.java` and `SetConfigRequest.java`
- [ ] T010 [US1] Implement `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/service/TenantConfigService.java` — `getConfiguration(tenantId, key, env, type)` with `@Cacheable(value="config", key="#tenantId+':'+#key+':'+#env")`; calls `ConfigResolver` for fallback; applies type conversion
- [ ] T011 [US2] Implement `TenantConfigService.setConfiguration(tenantId, key, env, value, isEncrypted)` — encrypt if `is_encrypted=true`, persist, `@CacheEvict`
- [ ] T012 [US1] Create `backend/tenant-service/src/main/java/com/saasstarterkit/tenant/controller/TenantConfigController.java` with `GET /api/v1/tenants/{tenantId}/config/{key}` and `PUT /api/v1/tenants/{tenantId}/config/{key}`
- [ ] T013 [US2] Add `@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SYSTEM')")` to write operations in `TenantConfigController`

**Checkpoint**: Hierarchical config resolution and encrypted secrets independently testable

---

## Phase 4: User Story 3 + 4 — Environment Scoping & Type Safety (Priority: P2)

**Goal**: Configs scoped to specific environments; typed retrieval returns correct Java type.

**Independent Test**: Config with environment=production not returned when env=staging query made. `Integer.class` request on integer config returns Integer. Wrong type → null + error log.

- [ ] T014 [P] [US3] Update `TenantConfigurationRepository` — add `findByTenantIdAndConfigKeyAndEnvironmentIn(tenantId, key, [env, "default"])` query
- [ ] T015 [US3] Update `ConfigResolver.resolve()` — environment precedence: exact match → "default" → global hardcoded default
- [ ] T016 [US4] Add type conversion in `TenantConfigService.getConfiguration()` — switch on `TenantConfiguration.data_type` to cast to String, Integer, Boolean, Long, Double, or parse JSONB

**Checkpoint**: Environment scoping and type-safe retrieval independently testable

---

## Phase 5: Polish & Cross-Cutting

- [ ] T017 [P] Add `DELETE /api/v1/tenants/{tenantId}/config/cache` cache-clear endpoint in `TenantConfigController`
- [ ] T018 [P] Add Prometheus timer for config resolution (cache hit vs DB fallback latency) in `TenantConfigService`
- [ ] T019 Run `quickstart.md` smoke tests (set, get, hierarchy, encryption, type conversion)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T008): Depends on Phase 1
- **Phase 3** (T009–T013): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T014–T016): Depends on Phase 2, Phase 3
- **Phase 5** (T017–T019): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working hierarchical config with encrypted secrets.
