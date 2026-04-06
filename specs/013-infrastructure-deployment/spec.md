# Feature Specification: Infrastructure & Deployment

**Feature Branch**: `013-infrastructure-deployment`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Local full-stack startup (Priority: P1)

A developer runs `make local-up` and the entire stack starts: PostgreSQL, Redis, Keycloak,
Kong, RabbitMQ, ELK, Prometheus, Alertmanager, OTEL Collector, MailHog.

**Acceptance Scenarios**:
1. **Given** docker compose file, **When** `docker-compose up`, **Then** all services healthy within 120 s.
2. **Given** fresh checkout, **When** `make local-seed`, **Then** demo tenants and users provisioned.
3. **Given** running stack, **When** `make local-down`, **Then** all containers stopped and volumes cleaned.

---

### User Story 2 — Cloud-agnostic deployment (Priority: P2)

Switching from local to AWS, GCP, or Azure requires only a change to `config/providers.yaml`
and the active Terraform provider module — no code changes.

**Acceptance Scenarios**:
1. **Given** providers.yaml set to AWS, **When** Terraform applied, **Then** EKS, RDS, ElastiCache, S3 provisioned.
2. **Given** providers.yaml set to GCP, **When** Terraform applied, **Then** GKE, Cloud SQL, Memorystore, GCS provisioned.
3. **Given** providers.yaml set to Azure, **When** Terraform applied, **Then** AKS, Azure PostgreSQL, Redis, Blob provisioned.

---

### User Story 3 — Seven-environment parity (Priority: P2)

The same Kubernetes config deploys to local, dev, sandbox, staging, UAT, pre-prod, and production
using Kustomize overlays — differing only in scale, data class, and security posture.

**Acceptance Scenarios**:
1. **Given** Kustomize base, **When** overlay for staging applied, **Then** blue/green strategy configured.
2. **Given** production overlay, **When** applied, **Then** canary deployment strategy and strict SLOs enforced.
3. **Given** dev/sandbox/staging/UAT, **When** overnight scheduled, **Then** scale-to-zero triggered.

---

### User Story 4 — CI/CD pipeline (Priority: P2)

Pipeline logic is expressed in Dagger (TypeScript). SCM-specific files are thin wrappers.
Every push to a feature branch triggers build, test, SAST, DAST, and container scanning.

**Acceptance Scenarios**:
1. **Given** push to feature branch, **When** CI triggers, **Then** build + unit test + SAST completed.
2. **Given** merge to develop, **When** CI triggers, **Then** integration tests + DAST + deploy to dev.
3. **Given** SAST finding CRITICAL, **When** pipeline runs, **Then** build FAILS and merge blocked.

---

### User Story 5 — Secret management (Priority: P1)

Secrets are never hardcoded. They are injected at runtime via SecretsProvider (Vault locally,
cloud-native secrets manager in cloud).

**Acceptance Scenarios**:
1. **Given** local stack, **When** service starts, **Then** secrets fetched from HashiCorp Vault.
2. **Given** AWS deployment, **When** service starts, **Then** secrets fetched from AWS Secrets Manager.
3. **Given** secret rotation triggered, **When** new secret applied, **Then** services reload without restart.

---

### Edge Cases
- What happens if Vault is unreachable during startup?
- How are database migrations run in production without downtime?
- How is a rollback triggered automatically on deployment failure?

## Requirements

### Functional Requirements
- **FR-001**: Local stack MUST be startable with single command (docker-compose up or make local-up).
- **FR-002**: Infrastructure MUST be defined as code using Terraform modules per cloud provider.
- **FR-003**: Kubernetes manifests MUST use Kustomize base + overlays for all 7 environments.
- **FR-004**: Pipeline logic MUST live in Dagger (TypeScript) under `pipeline/`.
- **FR-005**: SCM wrappers (.github, .gitlab, .bitbucket) MUST only invoke Dagger pipeline.
- **FR-006**: Secrets MUST be accessed only via SecretsProvider interface.
- **FR-007**: Database migrations MUST be applied automatically on service startup (non-prod) or controlled rollout (prod).
- **FR-008**: Feature flags MUST use Unleash (self-hosted) across all environments.
- **FR-009**: GitOps deployments MUST use ArgoCD.
- **FR-010**: dev/sandbox/staging/UAT MUST support scale-to-zero scheduling.
- **FR-011**: Active SCM provider MUST be configurable via `config/scm.yaml`.
- **FR-012**: Cloud provider selection MUST be driven by `config/providers.yaml`.
- **FR-013**: All external dependency abstractions (cache, queue, storage, identity, secrets, email, SMS, push) MUST be declared as interfaces in `shared-libs/providers/` and injected via configuration; no service code MAY import cloud SDK types directly.

### Key Entities
- **TerraformModule**: per-cloud module (aws, gcp, azure, local)
- **KustomizeOverlay**: environment-specific config (scale, strategy, resource limits, SLOs)
- **DaggerPipeline**: typed workflow steps (build, test, scan, deploy)
- **SecretProviderBinding**: maps secret name → vault/cloud-native path

### Non-Functional Requirements
- Local stack cold start MUST be < 3 minutes
- Cloud deployment MUST require zero code changes when switching providers
- CI pipeline MUST complete in < 10 minutes for feature branches
