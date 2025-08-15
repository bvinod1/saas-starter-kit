# Kong API Gateway - Multi-Tenant SaaS Platform

This directory contains the Kong API Gateway configuration for a multi-tenant SaaS platform with workspace-based tenant isolation, role-based access control, and tenant-specific rate limiting.

## Overview

The multi-tenant Kong configuration implements:

- **Workspace-based tenant isolation** - Each tenant gets their own isolated workspace
- **Tenant-aware routing** - Routes are scoped to specific tenants via path prefixes
- **Role-Based Access Control (RBAC)** - Different authentication methods per tenant
- **Tiered rate limiting** - Different quotas based on tenant subscription tier
- **Secure tenant data isolation** - Complete separation of tenant configurations

## Architecture

### Tenant Structure

| Tenant | Tier | Auth Method | Rate Limit | Services |
|--------|------|-------------|------------|----------|
| Tenant A | Standard | JWT | 100 req/min | User, Billing, Notifications |
| Tenant B | Standard | OAuth2 | 50 req/min | User, Analytics |
| Tenant C | Enterprise | JWT | 500 req/min | User (Premium) |

### Routing Structure

```
/tenant-a/api/* → Tenant A services (JWT protected)
/tenant-b/api/* → Tenant B services (OAuth2 protected)  
/tenant-c/api/* → Tenant C services (JWT protected)
/api/auth → Shared authentication service
```

## Files

- `kong.yml` - Basic Kong configuration (single-tenant)
- `kong-multi-tenant.yml` - Multi-tenant Kong configuration
- `deploy-multi-tenant.sh` - Automated deployment script
- `README.md` - This documentation

## Prerequisites

1. **Kong Gateway** running and accessible
2. **jq** command-line JSON processor
3. **curl** for HTTP requests
4. **Bash** shell environment

### Install Dependencies

**macOS:**
```bash
brew install jq
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install jq
```

**CentOS/RHEL:**
```bash
sudo yum install jq
```

## Quick Start

### 1. Start Kong Gateway

Ensure Kong is running and accessible:

```bash
# Check Kong status
curl http://localhost:8001/status

# Default ports:
# - Admin API: 8001
# - Proxy: 8000
```

### 2. Deploy Multi-Tenant Configuration

```bash
# Make script executable (if not already)
chmod +x deploy-multi-tenant.sh

# Deploy the multi-tenant configuration
./deploy-multi-tenant.sh deploy
```

### 3. Verify Deployment

```bash
# Check deployment status
./deploy-multi-tenant.sh verify

# Check Kong status
./deploy-multi-tenant.sh status
```

## Deployment Script Usage

The `deploy-multi-tenant.sh` script provides several commands:

```bash
./deploy-multi-tenant.sh [command]

Commands:
  deploy   - Deploy the multi-tenant Kong configuration (default)
  verify   - Verify the current deployment
  status   - Check Kong status
  cleanup  - Remove the multi-tenant configuration (manual cleanup required)
```

### Deployment Process

The script automatically:

1. **Creates Workspaces** - `tenant-a`, `tenant-b`, `tenant-c`
2. **Configures Services** - User, billing, analytics services per tenant
3. **Sets up Routes** - Tenant-specific path routing
4. **Enables Authentication** - JWT for A/C, OAuth2 for B
5. **Applies Rate Limiting** - Tiered limits per tenant
6. **Creates Consumers** - User accounts per tenant
7. **Verifies Deployment** - Confirms all components are working

## Manual Kong CLI Commands

If you prefer to use Kong CLI directly, here are the equivalent commands:

### 1. Create Workspaces

```bash
kong workspace create tenant-a --desc "Tenant A Workspace"
kong workspace create tenant-b --desc "Tenant B Workspace"
kong workspace create tenant-c --desc "Tenant C Enterprise Workspace"
```

### 2. Create Services and Routes

```bash
# Tenant A
kong workspace set tenant-a
kong service create --name tenant-a-user-service --url http://host.docker.internal:8081
kong route create --name tenant-a-user-route --service tenant-a-user-service --paths /tenant-a/api/users

# Tenant B
kong workspace set tenant-b
kong service create --name tenant-b-user-service --url http://host.docker.internal:8081
kong route create --name tenant-b-user-route --service tenant-b-user-service --paths /tenant-b/api/users

# Tenant C
kong workspace set tenant-c
kong service create --name tenant-c-user-service --url http://host.docker.internal:8081
kong route create --name tenant-c-user-route --service tenant-c-user-service --paths /tenant-c/api/users
```

### 3. Enable Authentication Plugins

```bash
# Tenant A - JWT
kong workspace set tenant-a
kong plugin create --name jwt --service tenant-a-user-service \
  --config.secret "tenant-a-jwt-secret-key" \
  --config.key_claim_name "kid" \
  --config.algorithm "HS256"

# Tenant B - OAuth2
kong workspace set tenant-b
kong plugin create --name oauth2 --service tenant-b-user-service \
  --config.scopes "email,profile,read,write" \
  --config.enable_authorization_code "true"

# Tenant C - JWT
kong workspace set tenant-c
kong plugin create --name jwt --service tenant-c-user-service \
  --config.secret "tenant-c-jwt-secret-key" \
  --config.key_claim_name "kid" \
  --config.algorithm "HS256"
```

### 4. Apply Rate Limiting

```bash
# Tenant A - Standard tier
kong workspace set tenant-a
kong plugin create --name rate-limiting --service tenant-a-user-service \
  --config.minute "100" --config.hour "1000"

# Tenant B - Basic tier
kong workspace set tenant-b
kong plugin create --name rate-limiting --service tenant-b-user-service \
  --config.minute "50" --config.hour "500"

# Tenant C - Enterprise tier
kong workspace set tenant-c
kong plugin create --name rate-limiting --service tenant-c-user-service \
  --config.minute "500" --config.hour "5000"
```

### 5. Create Consumers

```bash
# Tenant A
kong workspace set tenant-a
kong consumer create --username user1_tenant_a --custom_id user1_tenant_a

# Tenant B
kong workspace set tenant-b
kong consumer create --username user1_tenant_b --custom_id user1_tenant_b

# Tenant C
kong workspace set tenant-c
kong consumer create --username user1_tenant_c --custom_id user1_tenant_c
```

## Testing the Configuration

### Test Tenant A (JWT)

```bash
# Test endpoint (will require JWT token)
curl -H "X-Tenant-ID: tenant-a" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8000/tenant-a/api/users
```

### Test Tenant B (OAuth2)

```bash
# Test endpoint (will require OAuth2 token)
curl -H "X-Tenant-ID: tenant-b" \
     -H "Authorization: Bearer YOUR_OAUTH2_TOKEN" \
     http://localhost:8000/tenant-b/api/users
```

### Test Tenant C (Enterprise JWT)

```bash
# Test endpoint (will require JWT token)
curl -H "X-Tenant-ID: tenant-c" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8000/tenant-c/api/users
```

## Configuration Details

### Tenant Isolation

Each tenant operates in complete isolation:

- **Separate workspaces** prevent cross-tenant access
- **Unique service names** avoid conflicts
- **Path-based routing** ensures proper tenant identification
- **Header validation** requires `X-Tenant-ID` on all requests

### Authentication Methods

- **JWT (Tenants A & C)**: HS256 algorithm with tenant-specific secrets
- **OAuth2 (Tenant B)**: Full OAuth2 flow with email/profile scopes
- **API Keys**: Available for programmatic access

### Rate Limiting Tiers

- **Basic (Tenant B)**: 50 requests/minute, 500/hour
- **Standard (Tenant A)**: 100 requests/minute, 1000/hour  
- **Enterprise (Tenant C)**: 500 requests/minute, 5000/hour

### Security Features

- **CORS configuration** per tenant
- **Security headers** (X-Frame-Options, XSS Protection)
- **Tenant header validation** on all requests
- **Fault-tolerant rate limiting** prevents service disruption

## Monitoring and Logging

### View Workspaces

```bash
# List all workspaces
curl http://localhost:8001/workspaces

# Switch to specific workspace
curl -X POST http://localhost:8001/workspaces/tenant-a
```

### View Services

```bash
# List services in current workspace
curl http://localhost:8001/services

# View specific service
curl http://localhost:8001/services/tenant-a-user-service
```

### View Plugins

```bash
# List plugins in current workspace
curl http://localhost:8001/plugins

# View specific plugin
curl http://localhost:8001/plugins/PLUGIN_ID
```

### View Consumers

```bash
# List consumers in current workspace
curl http://localhost:8001/consumers

# View specific consumer
curl http://localhost:8001/consumers/CONSUMER_ID
```

## Troubleshooting

### Common Issues

1. **Kong not accessible**
   ```bash
   # Check if Kong is running
   docker ps | grep kong
   
   # Check Kong status
   curl http://localhost:8001/status
   ```

2. **Workspace creation fails**
   ```bash
   # Check existing workspaces
   curl http://localhost:8001/workspaces
   
   # Delete and recreate if needed
   curl -X DELETE http://localhost:8001/workspaces/tenant-a
   ```

3. **Service creation fails**
   ```bash
   # Ensure you're in the correct workspace
   curl -X POST http://localhost:8001/workspaces/tenant-a
   
   # Check service names are unique
   curl http://localhost:8001/services
   ```

4. **Plugin configuration fails**
   ```bash
   # Check plugin configuration
   curl http://localhost:8001/plugins/PLUGIN_ID
   
   # Verify service exists
   curl http://localhost:8001/services/SERVICE_NAME
   ```

### Logs

The deployment script logs all operations to `/tmp/kong-multi-tenant-deployment.log`. Check this file for detailed error information.

## Production Considerations

### Security

- **Change default secrets** for JWT and API keys
- **Use HTTPS** in production
- **Implement proper CORS** policies
- **Add IP whitelisting** if needed

### Performance

- **Use Redis** for rate limiting in production
- **Implement caching** strategies
- **Monitor Kong metrics** for performance tuning

### Scaling

- **Use Kong Enterprise** for advanced features
- **Implement Kong clustering** for high availability
- **Use database backend** instead of declarative config

## Support

For issues with this configuration:

1. Check the troubleshooting section above
2. Review Kong documentation: https://docs.konghq.com/
3. Check deployment logs: `/tmp/kong-multi-tenant-deployment.log`
4. Verify Kong version compatibility

## Version Compatibility

This configuration is tested with:
- Kong Gateway 3.x
- Kong Enterprise 3.x (optional)
- Docker and Docker Compose
- Linux, macOS, and Windows (WSL)
