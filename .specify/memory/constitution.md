<!--
SYNC IMPACT REPORT
==================
Version change: N/A → 1.0.0 (initial ratification)

Added sections:
  - Core Principles (8 principles covering all cross-cutting concerns)
  - Infrastructure Standards
  - Application Cross-Cutting Standards
  - Governance

Modified principles: N/A (initial version)
Removed sections: N/A (initial version)

Templates reviewed:
  - .specify/templates/plan-template.md          ⚠ pending — review Constitution Check alignment
  - .specify/templates/spec-template.md          ⚠ pending — verify mandatory sections match principles
  - .specify/templates/tasks-template.md         ⚠ pending — verify task categories match principles
  - .specify/templates/constitution-template.md  ✅ used as basis

Follow-up TODOs:
  - TODO(RATIFICATION_APPROVERS): List of stakeholders who formally ratified this constitution
  - TODO(COMPLIANCE_REVIEW_CADENCE): Define how frequently compliance reviews occur (suggested: quarterly)
-->

# SaaS Starter Kit Constitution

## Core Principles

### I. Interface-First Architecture (NON-NEGOTIABLE)

Every external dependency — cloud storage, cache, queue, secrets, email, identity, DNS, CDN,
service mesh, and SCM webhooks — MUST be accessed exclusively through a declared abstract
interface. Concrete provider implementations are injected via configuration, never referenced
directly in application or service code. No service or shared library MAY import a cloud-SDK
type directly; it MUST go through the adapter layer in `shared-libs/providers/`.

**Rationale**: This is the foundational constraint that makes the kit cloud-agnostic and
locally runnable. Violating it creates hard coupling that cannot be reversed cheaply.

Required interface files (one per concern):
- `storage.interface.ts`, `cache.interface.ts`, `queue.interface.ts`
- `secrets.interface.ts`, `email.interface.ts`, `identity.interface.ts`
- `scm-webhook.interface.ts`

Adapters MUST exist for: `aws`, `gcp`, `azure`, `local`.
Provider selection MUST be driven solely by `config/providers.yaml`.

### II. Cloud Agnosticism

The kit MUST deploy without code changes to AWS, GCP, and Azure, and MUST run fully
offline using the local Docker Compose stack. Switching cloud targets MUST require only
a change to `config/providers.yaml` and the active Terraform provider module.

Canonical provider mappings:

| Concern               | AWS                  | GCP                   | Azure                  | Local             |
|-----------------------|----------------------|-----------------------|------------------------|-------------------|
| Kubernetes            | EKS                  | GKE                   | AKS                    | kind / minikube   |
| API Gateway           | Kong / AWS GW        | Kong / Cloud Endpoints| Kong / APIM            | Kong (Docker)     |
| Object Storage        | S3                   | Cloud Storage         | Azure Blob             | MinIO             |
| Relational DB         | RDS PostgreSQL       | Cloud SQL             | Azure PostgreSQL        | PostgreSQL        |
| Cache                 | ElastiCache (Redis)  | Memorystore (Redis)   | Azure Cache for Redis  | Redis             |
| Queue                 | SQS                  | Cloud Pub/Sub         | Azure Service Bus      | RabbitMQ          |
| Event Streaming       | MSK (Kafka)          | Kafka on GKE          | Azure Event Hubs       | Kafka             |
| Secrets               | Secrets Manager      | Secret Manager        | Azure Key Vault        | HashiCorp Vault   |
| Email                 | SES                  | SendGrid / Postmark   | SendGrid / Postmark    | MailHog           |
| Identity              | Cognito              | Firebase Auth         | Azure AD B2C           | Keycloak          |
| Tracing               | X-Ray                | Cloud Trace           | Azure Monitor          | Jaeger            |
| Metrics               | CloudWatch           | Cloud Monitoring      | Azure Monitor          | Prometheus        |
| Logging               | CloudWatch Logs      | Cloud Logging         | Azure Monitor          | ELK Stack         |
| Service Mesh          | Istio on EKS         | Istio on GKE          | Istio on AKS           | Istio on kind     |
| IaC                   | Terraform + AWS      | Terraform + GCP       | Terraform + Azure      | Docker Compose    |

Infrastructure-as-Code MUST use Terraform modules under `infrastructure/terraform/modules/`
with per-cloud implementations under `infrastructure/terraform/providers/{aws,gcp,azure,local}/`.
Kubernetes manifests MUST use Kustomize base + overlays under
`infrastructure/kubernetes/{base,overlays/{local,dev,sandbox,staging,uat,pre-prod,prod}/}`.

### III. SCM & CI/CD Agnosticism

Pipeline logic — what to build, test, scan, and deploy — MUST be expressed once in
Dagger pipelines under `pipeline/` (TypeScript). SCM-specific files under `.scm/` are
thin wrappers that do nothing except invoke the Dagger pipeline. No business logic
MAY live in SCM workflow files.

Supported SCM systems:

| SCM           | Hosted           | Self-Hosted              | Wrapper path                              |
|---------------|------------------|--------------------------|-------------------------------------------|
| GitHub        | github.com       | GitHub Enterprise        | `.scm/github/workflows/*.yml`             |
| GitLab        | gitlab.com       | GitLab Self-Managed      | `.scm/gitlab/.gitlab-ci.yml`              |
| Bitbucket     | bitbucket.org    | Bitbucket Data Center    | `.scm/bitbucket/bitbucket-pipelines.yml`  |
| Azure DevOps  | dev.azure.com    | ADO Server               | `.scm/azure-devops/azure-pipelines.yml`   |
| Jenkins       | N/A              | Self-hosted              | `.scm/jenkins/Jenkinsfile`                |
| Gitea         | gitea.com        | Self-hosted              | `.scm/gitea/workflows/ci.yml`             |

Active SCM provider and branching strategy MUST be set in `config/scm.yaml`.
GitOps deployments MUST use ArgoCD (cloud-agnostic, runs on any Kubernetes).

Canonical branch-to-environment mapping:

| Branch       | Environment | Trigger                   |
|--------------|-------------|---------------------------|
| `feature/*`  | Local / Dev | On push (PR preview)      |
| `develop`    | Dev         | On merge                  |
| `sandbox`    | Sandbox     | On merge / manual         |
| `staging`    | Staging     | On merge                  |
| `uat`        | UAT         | On merge / manual         |
| `pre-prod`   | Pre-Prod    | On release candidate tag  |
| `main`       | Production  | On release approval       |

### IV. Seven-Environment Parity

The kit MUST support exactly seven environments. Every environment below production MUST
be runnable with the same configuration surface — differing only in scale, data class,
and security posture, not in architecture.

| Environment | Purpose                                        | Data Class            | Audience                        |
|-------------|------------------------------------------------|-----------------------|---------------------------------|
| local       | Developer inner loop, hot reload               | Seeded fake           | Individual developer            |
| dev         | Integration testing, PR validation             | Seeded fake           | Dev team                        |
| sandbox     | External / partner API exploration             | Anonymized subset     | Third-party developers, partners|
| staging     | Regression testing, QA sign-off                | Anonymized prod-like  | QA team                         |
| uat         | Business acceptance testing                    | Anonymized prod-like  | Business stakeholders           |
| pre-prod    | Performance, load, chaos testing; release gate | Anonymized prod clone | DevOps, SRE, perf team          |
| production  | Live system                                    | Real / PII            | End users                       |

Deployment strategies per environment:

| Environment | Strategy                         | Rollback              |
|-------------|----------------------------------|-----------------------|
| local       | Direct / hot reload              | Instant               |
| dev         | Rolling update                   | Auto on failure       |
| sandbox     | Rolling update                   | Manual                |
| staging     | Blue/Green                       | Auto on failure       |
| uat         | Blue/Green                       | Manual                |
| pre-prod    | Canary (10% → 50% → 100%)        | Auto on failure       |
| production  | Canary + Blue/Green              | Auto + manual         |

Cost control: `dev`, `sandbox`, `staging`, and `uat` MUST support scale-to-zero overnight.
`pre-prod` MUST be on-demand only (not always on).

### V. Traffic-Aware API & Service Design (NON-NEGOTIABLE)

Every API and inter-service interaction MUST be classified as either North-South or
East-West. Design, security, and observability decisions MUST reflect that classification.

**North-South** (external → internal): traffic entering via API Gateway from browsers,
mobile apps, or third parties.
- Protocol: REST or GraphQL with OpenAPI / Swagger contracts
- Auth: JWT / OAuth2 / API Keys validated at the gateway
- Security: TLS termination, WAF, CORS, rate limiting, DDoS protection per environment
- Tracing: trace initiation (span root created at gateway)
- Documentation: OpenAPI / Swagger; consumer mocking MUST be provided

**East-West** (internal → internal): service-to-service traffic inside the cluster.
- Protocol: gRPC (synchronous) or Kafka / RabbitMQ (async / event-driven)
- Contracts: Protobuf (gRPC) or Avro / AsyncAPI (events)
- Auth: mTLS via Istio; workload identity via SPIFFE/SPIRE
- Security: Kubernetes network policies, OPA authorization, zero-trust posture
- Tracing: W3C Trace Context headers propagated on every call
- Resilience: circuit breakers, retry with exponential backoff, bulkhead, timeout per call

Shared libraries for tracing propagation and auth middleware MUST handle both traffic
classes. New services MUST declare which traffic class(es) they participate in.

### VI. Tenant-Centric & User-Centric Design

**Multi-tenancy**: Every data record, every service call, and every event payload MUST
carry tenant context. Tenant isolation strategy (DB-per-tenant / schema-per-tenant /
shared) is configurable per deployment but MUST be enforced uniformly. Tenant context
MUST be injected at the API Gateway (North-South) and propagated via headers
(East-West) without exception.

**User Management**: The user-service MUST implement the full user lifecycle:
onboarding, suspension, deactivation, reactivation, offboarding (GDPR erasure).
User hierarchy — Organization → Teams → Users → Roles — MUST be first-class.
Supported roles at minimum: Owner, Admin, Member, Viewer, Guest.

**Dynamic User Profile Schemas (NON-NEGOTIABLE)**: Each organization MAY define
custom user attributes. The schema management subsystem MUST enforce:
- Immutable versioning: every schema change produces a new version; existing versions
  are never mutated.
- Version lifecycle: `draft → published → deprecated`.
- Users are tagged with the schema version they completed; historical data is preserved.
- Dynamic form rendering: the frontend MUST fetch the active schema version and render
  fields dynamically; field types, validations, conditional visibility, and groupings
  are driven entirely by schema definition — no hard-coded form fields.
- Data storage: profile values MUST be stored as JSONB (or equivalent flexible store),
  never as rigid schema columns.
- Admin tooling: schema builder, version diff view, deprecation management, and bulk
  backfill for existing users on new required fields are MANDATORY.

Schema data model MUST conform to the canonical `OrgSchema` / `UserProfileValues`
structures defined in the User Management architectural document.

### VII. Observability by Default (NON-NEGOTIABLE)

OpenTelemetry (OTel) is the single observability standard. Every service MUST instrument
traces, metrics, and structured logs using OTel SDKs. Backend exporters are configured
per environment via `config/observability.yaml`; switching backends requires no code change.

Mandatory observability per service:
- Structured JSON logging (no unstructured log lines)
- Distributed traces with W3C Trace Context propagation (East-West)
- Trace initiation at API Gateway (North-South)
- Liveness and readiness probes
- RED metrics (Rate, Errors, Duration) per endpoint

Per-environment sampling and alerting:

| Environment | Trace Sampling | Alerting                 | SLOs        |
|-------------|---------------|--------------------------|-------------|
| local       | 100%          | None                     | None        |
| dev         | 100%          | Slack only               | None        |
| sandbox     | 50%           | Slack only               | None        |
| staging     | 50%           | Slack + PagerDuty        | Defined     |
| uat         | 50%           | Slack                    | Defined     |
| pre-prod    | 100%          | Slack + PagerDuty        | Enforced    |
| production  | 5% (sampled)  | PagerDuty 24/7           | Enforced (strict) |

Error tracking: Sentry MUST be used across all environments (cloud-agnostic).
Dashboards: Grafana MUST be used across all environments.

### VIII. Security at Every Layer

Security controls MUST be applied at both the North-South boundary and the East-West
layer. Controls MUST be enforced — not optional — per the environment security posture
matrix below.

North-South mandatory controls:
- TLS termination at API Gateway (self-signed local, Let's Encrypt dev–UAT, full TLS pre-prod+)
- WAF rules (basic dev–UAT, full production rules pre-prod+)
- OAuth2 / OIDC / JWT token validation at gateway; tokens MUST NOT be re-validated downstream
- Rate limiting and DDoS protection (basic sandbox–UAT, full pre-prod+)
- CORS policy enforcement
- API key management for third-party consumers

East-West mandatory controls:
- mTLS enforced on all service-to-service communication (enabled from dev upward)
- SPIFFE/SPIRE workload identity for service authentication
- Kubernetes NetworkPolicy: deny-all default, explicit allow per service pair
- OPA authorization policies for service-level RBAC
- Zero-trust posture: no implicit trust between services

Common controls (all environments):
- Secrets MUST never be hardcoded or committed; MUST be injected via `SecretsProvider`
  interface (Vault locally, cloud-native secrets manager in cloud environments)
- Automated secrets rotation
- SAST, DAST, and dependency scanning MUST run in CI before every merge to `develop`
- Audit logging enabled from dev upward; immutable in production
- Penetration testing: allowed in sandbox/staging/UAT, required in pre-prod, scheduled in production

---

## Infrastructure Standards

### Networking
- Public subnets host North-South components (API Gateway, Load Balancers) only.
- Private subnets host all East-West services, databases, and caches.
- Network segmentation MUST be enforced via cloud-native VPC/VNet controls AND
  Kubernetes NetworkPolicy.

### Local Development Stack
The following Docker Compose services constitute the mandatory local stack. Every
component MUST be runnable with `make local-up` without a cloud account:

```
Kong (API Gateway) · Keycloak (Auth) · PostgreSQL · MongoDB · Redis · MinIO
Kafka · RabbitMQ · HashiCorp Vault · MailHog · Jaeger · Prometheus · Grafana
Elasticsearch · Kibana · Unleash (Feature Flags) · Nginx (CDN)
```

Standard developer commands MUST be available via `Makefile` / `Taskfile.yml`:
- `make local-up` — start full local stack
- `make local-infra` — start infrastructure services only
- `make local-service SERVICE=<name>` — start one service with hot reload
- `make local-test` — run full integration suite locally
- `make local-seed` — seed fake data
- `make local-down` — teardown

### Resiliency Patterns (East-West)
Circuit breakers, retry with exponential backoff and jitter, bulkhead isolation, and
per-call timeouts MUST be implemented for all synchronous East-West calls.
Chaos engineering hooks MUST be available from pre-prod upward.

### Feature Flags
Unleash (self-hosted) is the standard feature flag system across all environments.
Flag state in staging and UAT MUST mirror production. Pre-prod MUST have zero overrides.

### Billing & Subscription
Stripe / Paddle are the standard billing providers (cloud-agnostic). Usage metering
MUST be accessed via `MeteringProvider` interface.

---

## Application Cross-Cutting Standards

### API Contracts
- North-South: OpenAPI 3.x contracts under `contracts/openapi/`. Code generation MUST
  be used; hand-written client code against North-South APIs is prohibited.
- East-West sync: Protobuf schemas under `contracts/protobuf/`. gRPC code generation
  is mandatory.
- East-West async: AsyncAPI schemas under `contracts/asyncapi/`. Event producer and
  consumer stubs MUST be generated.

### Data Management
- Database migrations MUST be versioned and applied automatically on service startup
  in non-production environments and via controlled rollout in production.
- Multi-tenant data isolation strategy MUST be declared per service and respected by
  all queries — no cross-tenant data leakage is acceptable.
- GDPR right to erasure: a `UserDataErasureEvent` broadcast via Kafka MUST trigger
  purge handlers in every service that holds user data.
- PII MUST be masked in all environments below production.

### Communication
- All outbound email MUST go through `EmailProvider` interface; direct SMTP calls are prohibited.
- Webhook delivery to third parties is North-South outbound and MUST be retried with
  exponential backoff with at-least-once delivery guarantee.

---

## Governance

This constitution is the highest-authority document for the SaaS Starter Kit. It
supersedes all other practices, ADRs, and team conventions. In case of conflict,
this document prevails.

**Amendment procedure**:
1. Raise a proposal document describing the change, rationale, and migration plan.
2. Obtain approval from TODO(RATIFICATION_APPROVERS) designated stakeholders.
3. Increment the version number per the versioning policy below.
4. Update this file and run the consistency propagation checklist against all templates.
5. Merge via the standard PR process with a signed-off commit message:
   `docs: amend constitution to vX.Y.Z (<summary>)`

**Versioning policy**:
- MAJOR: backward-incompatible governance change — principle removal, redefinition, or
  architectural constraint reversal.
- MINOR: new principle, new mandatory section, or materially expanded guidance.
- PATCH: clarifications, wording fixes, typo corrections, non-semantic refinements.

**Compliance review**: TODO(COMPLIANCE_REVIEW_CADENCE) — all pull requests MUST include
a Constitution Check confirming no principle is violated. New services MUST be reviewed
against Principles I (Interface-First), V (Traffic-Aware Design), VII (Observability),
and VIII (Security) before merging to `develop`.

**Runtime guidance**: Refer to `docs/architecture/` for north-south, east-west,
cloud-agnostic, and local-development runbooks. Refer to `docs/environments/` for
per-environment operating procedures.

---

**Version**: 1.0.0 | **Ratified**: 2026-03-13 | **Last Amended**: 2026-03-13
