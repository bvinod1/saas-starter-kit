#!/usr/bin/env python3
import os, textwrap

SPECS_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "specs")

QUICKSTARTS = {
    "001-authentication-identity-management": textwrap.dedent("""\
        # Quickstart: Authentication & Identity Management

        ## Prerequisites
        - Docker 24+ and Docker Compose v2
        - Java 17 (Temurin recommended)
        - Maven 3.9+

        ## Start the auth stack locally
        ```bash
        docker compose up -d postgres keycloak
        # Wait for Keycloak to be healthy (~30 s)
        docker compose up -d auth-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `KEYCLOAK_URL` | `http://localhost:8080` | Keycloak base URL |
        | `KEYCLOAK_REALM` | `saas-starter` | Default realm |
        | `KEYCLOAK_CLIENT_ID` | `saas-backend` | Service client |
        | `KEYCLOAK_CLIENT_SECRET` | – | Required |
        | `JWT_SECRET` | – | Signing secret (HS256) |
        | `DB_URL` | `jdbc:postgresql://localhost:5432/authdb` | |

        ## Obtain a token
        ```bash
        curl -X POST http://localhost:8081/api/v1/auth/login \\
          -H "Content-Type: application/json" \\
          -d '{"email":"admin@example.com","password":"admin","tenantSubdomain":"demo"}'
        ```

        ## Test endpoints
        ```bash
        cd backend/auth-service
        mvn test
        ```

        ## Useful logs
        ```bash
        docker compose logs -f auth-service
        ```
    """),

    "002-multi-tenancy-tenant-lifecycle": textwrap.dedent("""\
        # Quickstart: Multi-Tenancy & Tenant Lifecycle

        ## Prerequisites
        - Docker 24+ and Docker Compose v2
        - Java 17, Maven 3.9+
        - Valid auth token (see feature 001)

        ## Start services
        ```bash
        docker compose up -d postgres keycloak tenant-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
        | `KEYCLOAK_URL` | `http://localhost:8080` | Admin API base |
        | `KEYCLOAK_ADMIN_USER` | `admin` | |
        | `KEYCLOAK_ADMIN_PASSWORD` | – | Required |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | |

        ## Create a tenant
        ```bash
        curl -X POST http://localhost:8082/api/v1/tenants \\
          -H "Authorization: Bearer $ADMIN_TOKEN" \\
          -H "Content-Type: application/json" \\
          -d '{"name":"Acme Corp","subdomain":"acme","planId":"starter"}'
        ```

        ## Monitor provisioning
        ```bash
        curl http://localhost:8082/api/v1/tenants/{tenantId}/status \\
          -H "Authorization: Bearer $ADMIN_TOKEN"
        ```

        ## Run tests
        ```bash
        cd backend/tenant-service && mvn test
        ```
    """),

    "003-user-management": textwrap.dedent("""\
        # Quickstart: User Management

        ## Prerequisites
        - Auth and tenant services running (features 001, 002)
        - Java 17, Maven 3.9+

        ## Start service
        ```bash
        docker compose up -d user-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/userdb` | |
        | `KEYCLOAK_URL` | `http://localhost:8080` | |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | For async events |
        | `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | Tracing |

        ## Create a user
        ```bash
        curl -X POST http://localhost:8083/api/v1/users \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"email":"alice@example.com","firstName":"Alice","lastName":"Smith","roles":["user"]}'
        ```

        ## List users
        ```bash
        curl http://localhost:8083/api/v1/users \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Run tests
        ```bash
        cd backend/user-service && mvn test
        ```
    """),

    "004-organization-management": textwrap.dedent("""\
        # Quickstart: Organization Management

        ## Prerequisites
        - User service running (feature 003)
        - Java 17, Maven 3.9+

        ## Start service
        ```bash
        docker compose up -d organisation-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/orgdb` | |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | |
        | `USER_SERVICE_URL` | `http://localhost:8083` | |

        ## Create an organization
        ```bash
        curl -X POST http://localhost:8084/api/v1/organizations \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"name":"Engineering","type":"DEPARTMENT"}'
        ```

        ## Add a member
        ```bash
        curl -X POST http://localhost:8084/api/v1/organizations/{orgId}/members \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"userId":"$USER_ID","roleInOrg":"MEMBER"}'
        ```

        ## Run tests
        ```bash
        cd backend/organisation-service && mvn test
        ```
    """),

    "005-role-based-access-control": textwrap.dedent("""\
        # Quickstart: Role-Based Access Control

        ## Prerequisites
        - Auth, user, and org services running
        - Java 17, Maven 3.9+
        - Redis (for permission cache)

        ## Start dependencies
        ```bash
        docker compose up -d redis
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
        | `REDIS_URL` | `redis://localhost:6379` | Permission cache |
        | `KEYCLOAK_URL` | `http://localhost:8080` | Role sync |
        | `CACHE_TTL_SECONDS` | `300` | |

        ## Assign a role
        ```bash
        curl -X POST http://localhost:8083/api/v1/users/$USER_ID/roles \\
          -H "Authorization: Bearer $ADMIN_TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"roleId":"billing-admin"}'
        ```

        ## Check permissions
        ```bash
        curl http://localhost:8083/api/v1/users/$USER_ID/permissions \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Run tests
        ```bash
        cd backend/user-service && mvn test -Dtest="*RbacTest"
        ```
    """),

    "006-subscription-billing-management": textwrap.dedent("""\
        # Quickstart: Subscription & Billing Management

        ## Prerequisites
        - Tenant service running (feature 002)
        - Java 17, Maven 3.9+
        - Stripe test API key (or mock)

        ## Start service
        ```bash
        docker compose up -d billing-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/billingdb` | |
        | `STRIPE_SECRET_KEY` | `sk_test_...` | Stripe test key |
        | `STRIPE_WEBHOOK_SECRET` | `whsec_...` | Webhook signature |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | |

        ## Create a subscription
        ```bash
        curl -X POST http://localhost:8085/api/v1/subscriptions \\
          -H "Authorization: Bearer $ADMIN_TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"planId":"pro","billingCycle":"MONTHLY"}'
        ```

        ## Test Stripe webhooks locally
        ```bash
        stripe listen --forward-to http://localhost:8085/api/v1/billing/webhook
        ```

        ## Run tests
        ```bash
        cd backend/billing-service && mvn test
        ```
    """),

    "007-notification-service": textwrap.dedent("""\
        # Quickstart: Notification Service

        ## Prerequisites
        - RabbitMQ running
        - Java 17, Maven 3.9+
        - SendGrid / Twilio test credentials (or mock)

        ## Start service
        ```bash
        docker compose up -d rabbitmq notification-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/notifdb` | |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | |
        | `SENDGRID_API_KEY` | `SG...` | Email provider |
        | `TWILIO_ACCOUNT_SID` | – | SMS provider |
        | `TWILIO_AUTH_TOKEN` | – | |
        | `MOCK_PROVIDERS` | `true` | Skip real sends in dev |

        ## Send a test notification
        ```bash
        # Publish directly to RabbitMQ exchange
        rabbitmqadmin publish exchange=notifications \\
          routing_key="$TENANT_ID.user.registered.email" \\
          payload='{"userId":"$USER_ID","email":"test@example.com","firstName":"Test"}'
        ```

        ## View notification log
        ```bash
        curl http://localhost:8086/api/v1/notifications \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Run tests
        ```bash
        cd backend/notification-service && mvn test
        ```
    """),

    "008-analytics-observability": textwrap.dedent("""\
        # Quickstart: Analytics & Observability

        ## Prerequisites
        - Docker Compose with monitoring stack
        - Java 17, Maven 3.9+

        ## Start observability stack
        ```bash
        docker compose up -d prometheus grafana otel-collector kibana elasticsearch logstash
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `OTEL_SERVICE_NAME` | `analytics-service` | |
        | `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://otel-collector:4317` | |
        | `OTEL_RESOURCE_ATTRIBUTES` | `tenant.id=system` | |
        | `MANAGEMENT_PROMETHEUS_ENABLED` | `true` | Actuator metrics |

        ## Access dashboards
        | Dashboard | URL |
        |-----------|-----|
        | Grafana | http://localhost:3000 (admin/admin) |
        | Prometheus | http://localhost:9090 |
        | Kibana | http://localhost:5601 |

        ## Verify metrics endpoint
        ```bash
        curl http://localhost:8087/actuator/prometheus | grep saas_
        ```

        ## Check health
        ```bash
        curl http://localhost:8087/actuator/health
        ```

        ## Run tests
        ```bash
        cd backend/analytics-service && mvn test
        ```
    """),

    "009-security-compliance": textwrap.dedent("""\
        # Quickstart: Security & Compliance

        ## Prerequisites
        - All core services running
        - Java 17, Maven 3.9+
        - Vault (for PHI key management)

        ## Start Vault (dev mode)
        ```bash
        docker compose up -d vault
        vault operator init -key-shares=1 -key-threshold=1
        vault unseal $UNSEAL_KEY
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `VAULT_ADDR` | `http://localhost:8200` | |
        | `VAULT_TOKEN` | `root` (dev mode) | |
        | `PHI_ENCRYPTION_KEY_PATH` | `secret/saas/dev/global/phi_key` | |
        | `GDPR_ERASURE_TIMEOUT_DAYS` | `30` | Legal deadline |
        | `RABBITMQ_URL` | `amqp://localhost:5672` | For erasure events |

        ## Submit a data erasure request
        ```bash
        curl -X POST http://localhost:8083/api/v1/users/$USER_ID/erasure \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Check erasure status
        ```bash
        curl http://localhost:8083/api/v1/users/$USER_ID/erasure/status \\
          -H "Authorization: Bearer $ADMIN_TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Update consent
        ```bash
        curl -X PUT http://localhost:8083/api/v1/users/$USER_ID/consent \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"consentType":"MARKETING","consented":false}'
        ```
    """),

    "010-api-gateway": textwrap.dedent("""\
        # Quickstart: API Gateway

        ## Prerequisites
        - Docker 24+ and Docker Compose v2
        - All backend services running

        ## Start Kong
        ```bash
        docker compose up -d kong
        # Verify admin API
        curl http://localhost:8001/status
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `KONG_DATABASE` | `off` | DB-less mode |
        | `KONG_DECLARATIVE_CONFIG` | `/kong/kong.yml` | Config file path |
        | `KONG_PROXY_LISTEN` | `0.0.0.0:8000` | Proxy port |
        | `KONG_ADMIN_LISTEN` | `127.0.0.1:8001` | Admin API |

        ## Reload config
        ```bash
        docker compose exec kong kong reload
        ```

        ## Test routing
        ```bash
        # Auth route
        curl -X POST http://localhost:8000/api/v1/auth/login \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"email":"test@example.com","password":"test"}'
        ```

        ## Test rate limiting (expect 429 after limit)
        ```bash
        for i in $(seq 1 20); do
          curl -s -o /dev/null -w "%{http_code}\\n" http://localhost:8000/api/v1/users \\
            -H "X-Tenant-ID: $TENANT_ID" -H "Authorization: Bearer $TOKEN"
        done
        ```

        ## Validate config
        ```bash
        docker compose exec kong kong check /kong/kong.yml
        ```
    """),

    "011-tenant-configuration-management": textwrap.dedent("""\
        # Quickstart: Tenant Configuration Management

        ## Prerequisites
        - Tenant service running (feature 002)
        - Redis running
        - Java 17, Maven 3.9+

        ## Start dependencies
        ```bash
        docker compose up -d redis tenant-service
        ```

        ## Key environment variables
        | Variable | Default | Description |
        |----------|---------|-------------|
        | `DB_URL` | `jdbc:postgresql://localhost:5432/saasdb` | |
        | `REDIS_URL` | `redis://localhost:6379` | Config cache |
        | `CONFIG_ENCRYPTION_KEY` | – | AES-256 base64 key |
        | `VAULT_ADDR` | `http://localhost:8200` | For secret configs |

        ## Set a tenant config value
        ```bash
        curl -X PUT http://localhost:8082/api/v1/tenants/$TENANT_ID/config \\
          -H "Authorization: Bearer $ADMIN_TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID" \\
          -H "Content-Type: application/json" \\
          -d '{"key":"feature.mfa.required","value":"true"}'
        ```

        ## Get resolved config (hierarchy applied)
        ```bash
        curl "http://localhost:8082/api/v1/tenants/$TENANT_ID/config/feature.mfa.required" \\
          -H "Authorization: Bearer $TOKEN" \\
          -H "X-Tenant-ID: $TENANT_ID"
        ```

        ## Invalidate cache
        ```bash
        curl -X DELETE "http://localhost:8082/api/v1/tenants/$TENANT_ID/config/cache" \\
          -H "Authorization: Bearer $ADMIN_TOKEN"
        ```
    """),

    "012-frontend-application": textwrap.dedent("""\
        # Quickstart: Frontend Application

        ## Prerequisites
        - Node.js 18+ and npm 9+
        - Backend services running (or use mock API)

        ## Install dependencies
        ```bash
        cd frontend
        npm install
        ```

        ## Key environment variables
        Create `frontend/.env.local`:
        ```
        REACT_APP_API_BASE_URL=http://localhost:8000
        REACT_APP_KEYCLOAK_URL=http://localhost:8080
        REACT_APP_KEYCLOAK_REALM=saas-starter
        REACT_APP_KEYCLOAK_CLIENT_ID=saas-frontend
        ```

        ## Start development server
        ```bash
        cd frontend
        npm start
        # Opens http://localhost:3000
        ```

        ## Run tests
        ```bash
        cd frontend
        npm test
        # Or in CI mode:
        npm test -- --watchAll=false --coverage
        ```

        ## Build for production
        ```bash
        cd frontend
        npm run build
        # Output in frontend/build/
        ```

        ## Lint and type-check
        ```bash
        cd frontend
        npx tsc --noEmit   # type check
        npx eslint src/    # lint
        ```

        ## Key pages
        | Route | Component |
        |-------|-----------|
        | `/login` | `src/pages/LoginPage.tsx` |
        | `/register` | `src/pages/RegisterPage.tsx` |
        | `/dashboard` | `src/pages/Dashboard.tsx` |
        | `/admin/*` | `src/pages/admin/` |
        | `/billing` | `src/pages/BillingPage.tsx` |
    """),

    "013-infrastructure-deployment": textwrap.dedent("""\
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
        dagger run .dagger/pipeline.ts --image-tag=$(git rev-parse --short HEAD) \\
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
    """),
}


def main():
    written = 0
    for feature_dir, content in QUICKSTARTS.items():
        target = os.path.join(SPECS_DIR, feature_dir, "quickstart.md")
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w") as f:
            f.write(content)
        print(f"OK: {feature_dir}/quickstart.md")
        written += 1
    print(f"\nQuickstart files written: {written}")


if __name__ == "__main__":
    main()
