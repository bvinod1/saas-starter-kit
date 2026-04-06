# Implementation Plan: Infrastructure & Deployment

**Branch**: `013-infrastructure-deployment` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Cloud-agnostic IaC using Terraform modules per provider (AWS/GCP/Azure/local). Kubernetes
manifests use Kustomize base + 7 environment overlays. Pipeline logic in Dagger (TypeScript);
SCM wrappers are thin shells that only invoke Dagger. Secrets via SecretsProvider interface
(Vault locally, cloud-native in cloud). GitOps via ArgoCD. Scale-to-zero for dev/sandbox/staging/UAT.

## Technical Context

**Language/Version**: Terraform 1.x, Kubernetes 1.29, Dagger (TypeScript 5.x), Bash
**Primary Dependencies**: Terraform AWS/GCP/Azure providers, Kustomize, ArgoCD, HashiCorp Vault, Unleash
**Storage**: Terraform state backend (S3/GCS/Azure Blob/local), Vault secrets
**Testing**: Terratest, Dagger pipeline unit tests, k8s conformance
**Target Platform**: AWS / GCP / Azure / local (kind/minikube)
**Project Type**: Infrastructure as Code + CI/CD pipeline
**Performance Goals**: Local cold start < 3 min; CI pipeline < 10 min; cloud deploy < 15 min
**Constraints**: Zero code changes to switch provider; secrets never hardcoded or committed
**Scale/Scope**: 7 environments; 13 microservices; multi-cloud

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | SecretsProvider interface; Terraform provider modules abstract cloud APIs |
| II. Cloud Agnostic | PASS | This IS the cloud-agnostic infrastructure feature |
| III. SCM/CI Agnostic | PASS | Dagger pipeline; SCM wrappers in .scm/ contain only Dagger invocation |
| IV. Seven-Environment Parity | PASS | Kustomize overlays for all 7 environments |
| V. Traffic-Aware | PASS | Public subnets: Kong/LB only; private subnets: all services/DBs |
| VII. Observability | PASS | OTel collector, Prometheus, Grafana, ELK in local and all cloud stacks |
| VIII. Security | PASS | mTLS from dev upward; Vault for secrets; SAST/DAST in CI pipeline |

## Project Structure

### Documentation (this feature)

```
specs/013-infrastructure-deployment/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
└── quickstart.md
```

### Source Code (repository root)

```
docker-compose.yml                            # Local full stack (existing)
infrastructure/
├── iam/realm-export.json
├── prometheus/prometheus.yml
├── alertmanager/alertmanager.yml
├── otel-collector/otel-collector-config.yaml
├── kibana/tenant-rbac-config.yml
├── logstash/
├── terraform/                                  [TO BE CREATED]
│   ├── modules/
│   └── providers/{aws,gcp,azure,local}/
└── kubernetes/                                 [TO BE CREATED]
    ├── base/
    └── overlays/{local,dev,sandbox,staging,uat,pre-prod,prod}/

pipeline/                                       [TO BE CREATED]
└── src/                # Dagger TypeScript pipeline

.scm/                                           [TO BE CREATED]
├── github/workflows/
├── gitlab/.gitlab-ci.yml
└── ...

config/
├── providers.yaml      # Active cloud provider
├── scm.yaml            # Active SCM
└── observability.yaml  # OTel backend per env

Makefile                                        [TO BE CREATED]
```

**Structure Decision**: IaC in infrastructure/; pipeline in pipeline/; provider config in config/.

## Complexity Tracking

> No constitution violations.
