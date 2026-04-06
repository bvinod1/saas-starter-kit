# Data Model: Infrastructure & Deployment

> Infrastructure is code; this data model documents the Terraform
> resource graph, Kubernetes manifest hierarchy, and pipeline inputs.

## Terraform Resource Graph

### Module: `infra/terraform/modules/networking/`
| Resource | Provider | Key Inputs |
|----------|----------|-----------|
| VPC / VNet | aws/gcp/azure | cidr_block, environment |
| Subnets (public/private) | provider | az_count |
| Security Groups / NSG | provider | ingress_rules |
| Load Balancer | provider | internal, tls_cert_arn |

### Module: `infra/terraform/modules/database/`
| Resource | Provider | Key Inputs |
|----------|----------|-----------|
| PostgreSQL instance | aws RDS / gcp CloudSQL / azure DB | version=15, multi_az |
| Redis cluster | ElastiCache / Memorystore / Azure Cache | node_type, replicas |

### Module: `infra/terraform/modules/kubernetes/`
| Resource | Notes |
|----------|-------|
| EKS / GKE / AKS cluster | managed k8s |
| Node groups | on-demand + spot/preemptible |
| OIDC provider | For IRSA / Workload Identity |

## Kustomize Overlay Structure
```
k8s/
  base/                   # shared manifests
  overlays/
    dev/
    ci/
    staging/
    canary/
    prod/
    dr/
    load-test/
```

## Kubernetes Manifest Schema (per service)
| Manifest | Key Fields |
|----------|-----------|
| Deployment | replicas, image tag, resources, env from ConfigMap/Secret |
| ConfigMap | app config, non-sensitive env |
| ExternalSecret | Vault → k8s Secret via ESO |
| HPA | minReplicas, maxReplicas, CPU/Memory targets |
| PodDisruptionBudget | minAvailable |
| ServiceMonitor | Prometheus scrape config |

## Pipeline Input Schema (Dagger TypeScript)
```typescript
interface PipelineInputs {
  imageTag: string;          // git SHA or semver
  environment: Environment;  // dev | ci | staging | canary | prod | dr | load-test
  tenantIds?: string[];       // for tenant-scoped deploys
  dryRun: boolean;
  skipTests?: boolean;        // only allowed in dev
}
```

## Secrets Hierarchy (Vault)
```
secret/saas/
  {env}/
    global/           # shared across all tenants
      db_password
      jwt_secret
    tenant/
      {tenant_id}/    # tenant-specific secrets
        db_password
        api_keys/
          stripe
          sendgrid
```

## Environment Parity Rules
All 7 environments MUST use identical Helm chart versions and base manifests.
Differences are ONLY in:
- replica counts
- resource requests/limits
- feature flag overrides
- Vault secret paths

## Files
- `docker-compose.yml` – local dev stack
- `infrastructure/` – monitoring/logging configs
- `application-iam.yml` – IAM roles
