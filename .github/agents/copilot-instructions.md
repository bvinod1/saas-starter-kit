# saas_starter_kit Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-04

## Active Technologies
- Java 17, Spring Boot 3.x + Spring Data JPA, Spring Web, PostgreSQL JDBC, Liquibase (002-multi-tenancy-tenant-lifecycle)
- PostgreSQL 15 (tenant_management schema + dynamic tenant schemas) (002-multi-tenancy-tenant-lifecycle)
- Java 17, Spring Boot 3.x; TypeScript React 18 (admin UI) + Spring Data JPA, Spring Security, Spring Web, Keycloak Admin Clien (003-user-management)
- PostgreSQL 15 (tenant_{slug}.users, OrgSchema, UserProfileValues as JSONB) (003-user-management)
- Java 17, Spring Boot 3.x + Spring Data JPA, Spring Web, Spring Security (004-organization-management)
- PostgreSQL 15 (tenant_{slug}.organizations, organizational_management schema) (004-organization-management)
- Java 17, Spring Boot 3.x + Spring Security, Keycloak Spring Boot Adapter, OPA (East-West) (005-role-based-access-control)
- PostgreSQL 15 (auth.roles, auth.permissions, auth.user_roles, auth.role_permissions) (005-role-based-access-control)
- Java 17, Spring Boot 3.x; TypeScript React 18 (billing page) + Spring async (CompletableFuture), Spring Data JPA, Zoho Billing HTTP clien (006-subscription-billing-management)
- PostgreSQL 15 (plans, subscriptions, checkout_sessions, usage_tracking, webhook_events) (006-subscription-billing-management)
- Java 17, Spring Boot 3.x + Spring AMQP (RabbitMQ), Spring Web, Mustache, Spring Mail, Firebase Admin SDK (007-notification-service)
- PostgreSQL 15 (notifications, notification_templates, user_notification_preferences) (007-notification-service)
- Java 17 (analytics-service); YAML/config (infrastructure components) + OTel SDK (Java), Micrometer (Prometheus), Logstash, Elasticsearch, Spring Data JPA (008-analytics-observability)
- PostgreSQL (analytics), Elasticsearch (logs), Prometheus TSDB (metrics) (008-analytics-observability)
- Java 17, Spring Boot 3.x + Spring Security, Javax Crypto (AES-256), Spring Data JPA, Kafka (erasure events) (009-security-compliance)
- PostgreSQL (data_consents, data_subject_requests, protected_health_information) (009-security-compliance)
- Kong 3.x (declarative YAML); Bash (deploy scripts) + Kong JWT plugin, Rate Limiting, Request Transformer, CORS, OTel plugin (010-api-gateway)
- Kong DB-less (YAML) or Kong DB (PostgreSQL) (010-api-gateway)
- Java 17, Spring Boot 3.x + Spring Data JPA, Spring Cache (Redis), Javax Crypto (AES-256) (011-tenant-configuration-management)
- PostgreSQL 15 (tenant_configurations), Redis (config cache) (011-tenant-configuration-management)
- TypeScript 5.x, React 18, Node 20 + React Router 6, @tanstack/react-query, Axios, React Hook Form, Tailwind CSS, @heroicons/react, react-hot-toas (012-frontend-application)
- localStorage (session only) (012-frontend-application)
- Terraform 1.x, Kubernetes 1.29, Dagger (TypeScript 5.x), Bash + Terraform AWS/GCP/Azure providers, Kustomize, ArgoCD, HashiCorp Vault, Unleash (013-infrastructure-deployment)
- Terraform state backend (S3/GCS/Azure Blob/local), Vault secrets (013-infrastructure-deployment)

- Java 17, Spring Boot 3.x + Spring Security, Spring Web, Keycloak Admin Client, JJWT, Spring Data JPA (001-authentication-identity-management)

## Project Structure

```text
backend/
frontend/
tests/
```

## Commands

# Add commands for Java 17, Spring Boot 3.x

## Code Style

Java 17, Spring Boot 3.x: Follow standard conventions

## Recent Changes
- 013-infrastructure-deployment: Added Terraform 1.x, Kubernetes 1.29, Dagger (TypeScript 5.x), Bash + Terraform AWS/GCP/Azure providers, Kustomize, ArgoCD, HashiCorp Vault, Unleash
- 012-frontend-application: Added TypeScript 5.x, React 18, Node 20 + React Router 6, @tanstack/react-query, Axios, React Hook Form, Tailwind CSS, @heroicons/react, react-hot-toas
- 011-tenant-configuration-management: Added Java 17, Spring Boot 3.x + Spring Data JPA, Spring Cache (Redis), Javax Crypto (AES-256)


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
