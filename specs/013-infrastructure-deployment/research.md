# Research: Infrastructure & Deployment

## Decision: Terraform Module-per-Provider Architecture

**Decision**: Terraform modules under infrastructure/terraform/modules/ with per-cloud implementations under infrastructure/terraform/providers/{aws,gcp,azure,local}/. Active provider set in config/providers.yaml.
**Rationale**: Constitution II (Cloud Agnostic) requires zero code changes when switching providers. Module abstraction ensures the same Terraform inputs produce equivalent infrastructure on all clouds.

## Decision: Kustomize for Kubernetes Manifests

**Decision**: Kustomize base/ with overlays/ for all 7 environments. base/ contains shared manifests; overlays/ customise resource limits, replica counts, deployment strategies, SLOs.
**Rationale**: Kustomize is native to kubectl; no Helm chart complexity. Overlay pattern handles environment differences cleanly. ArgoCD integrates natively with Kustomize.

## Decision: Dagger Pipelines (TypeScript)

**Decision**: All pipeline logic in TypeScript Dagger modules under pipeline/. SCM-specific files (.github/workflows, .gitlab-ci.yml, etc.) are thin wrappers invoking dagger run.
**Rationale**: Constitution III (SCM Agnostic) is NON-NEGOTIABLE. Pipeline logic must be SCM-independent. Dagger runs locally (developers test pipeline without pushing); SCM wrappers are < 10 lines.

## Decision: HashiCorp Vault for Local Secrets

**Decision**: Vault runs in local Docker Compose stack. Application reads secrets via SecretsProvider interface. Cloud deployments use AWS Secrets Manager / GCP Secret Manager / Azure Key Vault.
**Rationale**: Same code path for all environments. Vault dev mode for local (no persistence). SecretsProvider maps to appropriate backend per cloud.

## Decision: ArgoCD for GitOps Deployments

**Decision**: ArgoCD monitors git repository; applies Kubernetes manifests on commit to environment branches.
**Rationale**: Cloud-agnostic GitOps (runs on any K8s). Declarative desired state. Automatic drift detection and self-healing. Audit trail of all deployments.

## Best Practices Confirmed

- Local docker-compose.yml already provides: PostgreSQL, Redis, Keycloak, Kong, RabbitMQ, ELK, Prometheus, Alertmanager, OTEL
- Makefile targets: local-up, local-infra, local-service, local-test, local-seed, local-down (as per constitution)
- scale-to-zero: dev/sandbox/staging/UAT use KEDA (Kubernetes Event-Driven Autoscaler) with overnight cron schedule
- Database migrations: Liquibase applied automatically on service startup (non-prod); manual approval gate (prod)
- Container scanning: Trivy in CI pipeline; images rejected if CRITICAL CVEs detected
