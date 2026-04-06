# Quickstart: Infrastructure & Deployment

## Prerequisites
- Docker 24+ and Docker Compose v2 (local dev)
- Terraform 1.6+ (cloud provisioning)
- kubectl 1.28+ and kustomize 5+
- Dagger CLI (pipeline execution)

## Local development (full stack)
```bash
cd /Users/vinodbalasubramanian/Documents/saas_starter_kit
docker compose up -d
# All services start; check with:
docker compose ps
```

## Key environment variables
| Variable | Description |
|----------|-------------|
| `TF_VAR_environment` | Target environment (dev/staging/prod/…) |
| `TF_VAR_aws_region` | AWS region (or gcp_region / azure_location) |
| `KUBECONFIG` | Path to kubeconfig |
| `VAULT_ADDR` | Vault server address |
| `VAULT_TOKEN` | Vault access token |

## Provision cloud infrastructure
```bash
cd infra/terraform/environments/dev
terraform init
terraform plan -var="environment=dev"
terraform apply -auto-approve  # dev only
```

## Deploy to Kubernetes
```bash
# Apply base + dev overlay
kubectl apply -k k8s/overlays/dev

# Verify rollout
kubectl rollout status deployment/auth-service -n saas-dev
```

## Run CI pipeline locally with Dagger
```bash
dagger run .dagger/pipeline.ts --image-tag=$(git rev-parse --short HEAD) \
  --environment=dev --dry-run=false
```

## Health check all services
```bash
bash setup/start-all-services.sh
```

## Stop all services
```bash
bash setup/stop-all-services.sh
```

## Start with structured logging
```bash
bash setup/start-with-logging.sh
```

## Monitoring stack
```bash
docker compose up -d prometheus grafana otel-collector elasticsearch kibana
# Grafana: http://localhost:3000
# Prometheus: http://localhost:9090
```
