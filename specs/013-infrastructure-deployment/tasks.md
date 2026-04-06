# Tasks: Infrastructure & Deployment

**Feature**: `013-infrastructure-deployment`
**Input**: `specs/013-infrastructure-deployment/`
**Source**: `docker-compose.yml`, `setup/`, `infrastructure/`

## Phase 1: Setup

- [ ] T001 Verify `docker-compose.yml` defines all required services: postgres, redis, keycloak, kong, rabbitmq, **kafka, zookeeper** (for erasure event streaming), elasticsearch, logstash, kibana, prometheus, grafana, alertmanager, otel-collector, sentry, mailhog; add any missing
- [ ] T002 [P] Create `Makefile` at repo root with ALL constitution-mandated targets: `local-up`, `local-down`, `local-seed`, `local-logs`, `build-all`, `local-infra` (infra services only), `local-service SERVICE=<name>` (single service with hot-reload), `local-test` (full integration suite)
- [ ] T003 [P] Create `setup/seed-demo-data.sh` — provisions 2 demo tenants and 5 users via REST API calls after all services are healthy

---

## Phase 2: Foundational

- [ ] T004 Create `backend/*/src/main/java/com/saasstarterkit/*/security/SecretsProvider.java` interface (each service) — `getSecret(key)`, `getSecretMap(prefix)` with `VaultSecretsProvider` impl and `EnvSecretsProvider` impl (local dev fallback)
- [ ] T004a [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/cache/CacheProvider.java` interface — `get(key)`, `set(key, value, ttlSeconds)`, `delete(key)`, `exists(key)`; impls: `RedisCacheProvider` (local/cloud) and `InMemoryCacheProvider` (test)
- [ ] T004b [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/queue/QueueProvider.java` interface — `publish(topic, message)`, `subscribe(topic, handler)`; impls: `RabbitMqQueueProvider` (local), `SqsQueueProvider` (aws), `PubSubQueueProvider` (gcp), `ServiceBusQueueProvider` (azure)
- [ ] T004c [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/storage/StorageProvider.java` interface — `upload(bucket, key, bytes)`, `download(bucket, key)`, `delete(bucket, key)`, `presignedUrl(bucket, key, ttl)`; impls: `S3StorageProvider`, `GcsStorageProvider`, `AzureBlobStorageProvider`, `MinioStorageProvider` (local)
- [ ] T004d [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/identity/IdentityProvider.java` interface — `createUser(attrs)`, `updateUser(id, attrs)`, `deleteUser(id)`, `assignRoles(id, roles)`; impl: `KeycloakIdentityProvider`
- [ ] T004e [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/notification/SmsProvider.java` interface — `send(to, body, tenantId)`; impls: `TwilioSmsProvider`, `PlivoSmsProvider`
- [ ] T004f [P] Create `shared-libs/providers/src/main/java/com/saasstarterkit/providers/notification/PushProvider.java` interface — `sendPush(deviceToken, title, body, data)`; impl: `FcmPushProvider`
- [ ] T004g Create `shared-libs/providers/pom.xml` as a multi-module Maven project; add as `<dependency>` in each backend service pom.xml
- [ ] T005 [P] Configure HashiCorp Vault service in `docker-compose.yml` (dev mode) with `vault` policy and `secret/saas/dev/global/` path seeded in `setup/vault-seed.sh`
- [ ] T006 [P] Create `config/providers.yaml` — active cloud provider selection: `provider: local|aws|gcp|azure`
- [ ] T007 [P] Create `config/scm.yaml` — active SCM: `scm: github|gitlab|bitbucket`
- [ ] T008 Create health check wrapper `setup/wait-for-healthy.sh` — polls all service `/actuator/health` endpoints until UP (used by `local-up` Makefile target)

**Checkpoint**: Local stack fully startable and secret injection ready

---

## Phase 3: User Story 1 + 5 — Local Stack & Secret Management (Priority: P1) 🎯 MVP

**Goal**: `make local-up` starts all services; secrets injected from Vault, not environment variables.

**Independent Test**: `docker-compose up -d` → all containers healthy within 120 s. `make local-seed` → demo tenants exist. Any service starting without Vault reaches `EnvSecretsProvider` fallback. No secret appears in any application.yml.

- [ ] T009 [US1] Update `setup/start-all-services.sh` to call `docker-compose up -d` and then `setup/wait-for-healthy.sh`
- [ ] T010 [US1] Add `make local-seed` target to `Makefile` invoking `setup/seed-demo-data.sh`
- [ ] T011 [US5] Update all service `application.yml` files — replace hardcoded secrets with `${saas.secrets.db_password}` etc.; bind to `VaultSecretsProvider.getSecret()`
- [ ] T012 [US5] Create `setup/vault-seed.sh` — initialises Vault dev server, enables kv-v2, writes all required secrets

**Checkpoint**: Local stack and secret management independently testable

---

## Phase 4: User Story 3 — Seven-Environment Parity (Priority: P2)

**Goal**: Kustomize overlays produce correct manifests for all 7 environments from a shared base.

**Independent Test**: `kustomize build k8s/overlays/dev` → Deployment uses 1 replica. `kustomize build k8s/overlays/prod` → Deployment uses 3 replicas, canary strategy, strict resource limits.

- [ ] T013 [P] [US3] Create `k8s/base/` — Deployment, Service, ConfigMap, HPA, PodDisruptionBudget, ServiceMonitor manifests for each service (parameterised image tag)
- [ ] T014 [P] [US3] Create `k8s/overlays/dev/`, `k8s/overlays/staging/`, `k8s/overlays/prod/` kustomization.yaml with correct replica counts, resource limits, and rollout strategies
- [ ] T015 [US3] Create `k8s/overlays/ci/`, `k8s/overlays/canary/`, `k8s/overlays/dr/`, `k8s/overlays/load-test/` kustomization.yaml stubs
- [ ] T016 [US3] Add `cronjob-scale-to-zero.yaml` to dev/sandbox/staging/UAT overlays (overnight scale-down via CronJob)

**Checkpoint**: All 7 Kustomize overlays build without errors

---

## Phase 5: User Story 2 — Cloud-Agnostic Terraform (Priority: P2)

**Goal**: `config/providers.yaml: provider: aws` + `terraform apply` provisions EKS, RDS, ElastiCache; no code changes for GCP or Azure.

**Independent Test**: `terraform plan -var="environment=dev"` completes for aws module. Swap to gcp module, plan completes. No service code modified.

- [ ] T017 [P] [US2] Create `infra/terraform/modules/aws/main.tf` — EKS, RDS PostgreSQL 15, ElastiCache Redis, S3 bucket, IAM roles
- [ ] T018 [P] [US2] Create `infra/terraform/modules/gcp/main.tf` — GKE, Cloud SQL, Memorystore, GCS
- [ ] T019 [P] [US2] Create `infra/terraform/modules/azure/main.tf` — AKS, Azure PostgreSQL, Azure Cache for Redis, Blob Storage
- [ ] T020 [US2] Create `infra/terraform/environments/dev/main.tf` — reads `config/providers.yaml`, calls appropriate module with `environment=dev` vars
- [ ] T021 [US2] Create `infra/terraform/modules/local/main.tf` — docker-compose substitute (for local-only use); outputs same variable names as cloud modules

**Checkpoint**: Cloud-agnostic Terraform modules independently testable with `terraform plan`

---

## Phase 6: User Story 4 — CI/CD Pipeline (Priority: P2)

**Goal**: Dagger pipeline runs build, test, SAST, DAST, container scan on every push. SCM files are thin wrappers.

**Independent Test**: `dagger run pipeline/main.ts --environment=dev --dry-run=true` completes. CRITICAL SAST finding fails the build. Merge to develop triggers deploy to dev.

- [ ] T022 [P] [US4] Create `pipeline/main.ts` Dagger TypeScript pipeline — steps: `buildImage()`, `runUnitTests()`, `runSast()`, `runDast()`, `scanContainer()`, `deployToEnvironment()`
- [ ] T023 [P] [US4] Create `.scm/github/workflows/ci.yml` — thin wrapper calling `dagger run pipeline/main.ts` (SCM-agnostic path per constitution Principle III)
- [ ] T024 [US4] Create `pipeline/steps/sast.ts` — SpotBugs + OWASP Dependency Check; fail pipeline on CRITICAL findings
- [ ] T025 [US4] Add ArgoCD `Application` manifest in `k8s/argocd/` pointing to `k8s/overlays/{env}` for GitOps deployments
- [ ] T026 [US4] Add Unleash feature flag server to `docker-compose.yml` and Kubernetes base manifests

**Checkpoint**: CI/CD pipeline independently triggerable and verifiable

---

## Phase 7: Polish & Cross-Cutting

- [ ] T027 [P] Update `README.md` with full local setup steps, `make` targets, and environment variable table
- [ ] T028 [P] Add `setup/MULTI_TENANCY_IMPLEMENTATION_SUMMARY.md` update with deployment topology diagram
- [ ] T029 Run `quickstart.md` smoke tests (`docker-compose up`, `make local-seed`, `make local-down`)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T008, T004a–T004g): Depends on Phase 1
- **Phase 3** (T009–T012): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T013–T016): Depends on Phase 2
- **Phase 5** (T017–T021): Depends on Phase 2
- **Phase 6** (T022–T026): Depends on Phase 3, Phase 4
- **Phase 7** (T027–T029): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for a fully startable local stack with Vault secrets.
