# Multi-Tenancy Implementation Guide

This document describes the implementation of multi-tenancy with **one schema per tenant** using Keycloak for authentication and authorization.

## Architecture Overview

### Multi-Tenancy Model: Schema per Tenant

Our SaaS starter kit implements a **schema-per-tenant** multi-tenancy model where:

- Each tenant gets its own database schema (e.g., `tenant_acme`, `tenant_xyz`)
- Shared data is stored in common schemas (`shared`, `tenant_management`)
- Complete data isolation between tenants
- Tenant-specific customization capabilities

### Key Components

1. **Keycloak Integration**: Tenant-aware authentication and authorization
2. **Database Schema Management**: Dynamic schema creation and management
3. **Tenant Context**: Thread-local tenant context management
4. **API Gateway**: Tenant-aware routing and filtering
5. **Frontend**: Tenant selector and context management

## Database Architecture

### Schema Structure

```
saas_starter_kit/
├── tenant_management/     # Tenant registry and management
│   ├── tenants           # Master tenant table
│   ├── tenant_schemas    # Schema registry
│   └── tenant_users      # Cross-tenant user registry
├── shared/               # System-wide shared data
│   ├── subscription_plans
│   └── notification_templates
├── tenant_acme/          # Tenant-specific schema
│   ├── users
│   ├── organizations
│   ├── subscriptions
│   └── ...
└── tenant_xyz/           # Another tenant schema
    ├── users
    ├── organizations
    ├── subscriptions
    └── ...
```

### Key Database Functions

- `create_tenant_schema(tenant_slug)`: Creates a new tenant schema
- `create_tenant(name, slug, keycloak_realm_id, keycloak_client_id)`: Creates tenant and schema
- `get_tenant_schema(tenant_slug)`: Returns schema name for tenant
- `tenant_exists(tenant_slug)`: Checks if tenant exists

## Backend Implementation

### 1. Tenant Context Management

```java
@Component
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantSchema = new ThreadLocal<>();
    
    public static void setCurrentTenant(String tenantSlug) {
        currentTenant.set(tenantSlug);
        currentTenantSchema.set("tenant_" + tenantSlug);
    }
}
```

### 2. Tenant Resolution

The `TenantResolver` extracts tenant information from JWT tokens:

```java
@Component
public class TenantResolver implements HandlerInterceptor {
    private String extractTenantFromJwt(Jwt jwt) {
        String tenantSlug = jwt.getClaimAsString("tenant_slug");
        if (tenantSlug == null) {
            String tenantId = jwt.getClaimAsString("tenant_id");
            tenantSlug = tenantId; // Fallback
        }
        return tenantSlug;
    }
}
```

### 3. Schema Interceptor

Sets the database search path to include the current tenant's schema:

```java
@Component
public class SchemaInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantSchema = TenantContext.getCurrentTenantSchema();
        if (tenantSchema != null) {
            // Set search_path to include tenant schema
            connection.execute("SET search_path TO " + tenantSchema + ", shared, tenant_management, public");
        }
        return true;
    }
}
```

### 4. Multi-Tenant Data Source

Dynamic data source routing based on current tenant:

```java
@Configuration
public class MultiTenantDataSourceConfig {
    @Bean
    @Primary
    public DataSource multiTenantDataSource() {
        AbstractRoutingDataSource multiTenantDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TenantContext.getCurrentTenantSchema();
            }
        };
        // Configure target data sources
        return multiTenantDataSource;
    }
}
```

## Keycloak Configuration

### 1. Protocol Mappers

Add tenant-specific claims to JWT tokens:

```json
{
  "name": "tenantId",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-usermodel-property-mapper",
  "config": {
    "userinfo.token.claim": "true",
    "user.attribute": "tenantId",
    "id.token.claim": "true",
    "access.token.claim": "true",
    "claim.name": "tenant_id",
    "jsonType.label": "String"
  }
}
```

### 2. Tenant Groups

Organize users by tenant groups:

```json
{
  "name": "Tenants",
  "subGroups": [
    {
      "name": "tenant-admin",
      "attributes": {
        "tenant-role": ["admin"]
      }
    },
    {
      "name": "tenant-user",
      "attributes": {
        "tenant-role": ["user"]
      }
    }
  ]
}
```

## Frontend Implementation

### 1. Tenant Context

Extended AuthContext to include tenant information:

```typescript
interface AuthContextType {
  user: User | null;
  token: string | null;
  currentTenant: string | null;
  setCurrentTenant: (tenantSlug: string) => void;
  // ... other properties
}
```

### 2. Tenant Selector Component

Allows users to switch between tenants:

```typescript
const TenantSelector: React.FC = () => {
  const { currentTenant, setCurrentTenant, user } = useAuth();
  const [tenants, setTenants] = useState<Tenant[]>([]);
  
  const handleTenantChange = (tenantSlug: string) => {
    setCurrentTenant(tenantSlug);
    window.location.reload(); // Reload to update context
  };
  
  // Render tenant dropdown
};
```

### 3. API Integration

Include tenant context in API calls:

```typescript
// Automatically include tenant in headers
axios.interceptors.request.use((config) => {
  const tenant = localStorage.getItem('currentTenant');
  if (tenant) {
    config.headers['X-Tenant'] = tenant;
  }
  return config;
});
```

## API Gateway Configuration

### Kong Configuration

Add tenant-aware routing:

```yaml
services:
  - name: user-service
    url: http://host.docker.internal:8081
    routes:
      - name: user-routes
        paths:
          - /api/users
        strip_path: true
      - name: tenant-routes
        paths:
          - /api/tenants
        strip_path: true
```

## Tenant Management API

### Endpoints

- `GET /api/tenants` - List all tenants (super admin only)
- `GET /api/tenants/{slug}` - Get tenant by slug
- `POST /api/tenants` - Create new tenant (super admin only)
- `DELETE /api/tenants/{id}` - Delete tenant (super admin only)
- `GET /api/tenants/check/{slug}` - Check if tenant exists

### Tenant Creation Process

1. **Validate Input**: Check tenant name and slug
2. **Create Tenant Record**: Insert into `tenant_management.tenants`
3. **Create Schema**: Execute `create_tenant_schema(tenant_slug)`
4. **Setup Keycloak**: Create realm and client (if needed)
5. **Initialize Data**: Create default organization and settings

## Security Considerations

### 1. Tenant Isolation

- **Database Level**: Complete schema separation
- **Application Level**: Tenant context validation
- **API Level**: Tenant-aware authorization

### 2. Data Access Control

```java
@PreAuthorize("hasRole('TENANT_ADMIN') and @tenantSecurityService.isCurrentUserInTenant(#tenantId)")
public ResponseEntity<?> updateTenantData(@PathVariable UUID tenantId) {
    // Only allow access to current tenant's data
}
```

### 3. Cross-Tenant Protection

- Validate tenant context on every request
- Prevent cross-tenant data access
- Audit all tenant operations

## Deployment Considerations

### 1. Database Migration

```sql
-- Run multi-tenant schema setup
\i database/init/02-multi-tenant-schema.sql
```

### 2. Keycloak Setup

1. Import updated realm configuration
2. Create tenant-specific realms (optional)
3. Configure protocol mappers
4. Set up tenant groups

### 3. Environment Variables

```bash
# Tenant management
TENANT_DEFAULT_SCHEMA=public
TENANT_SCHEMA_PREFIX=tenant_

# Keycloak tenant configuration
KEYCLOAK_TENANT_ATTRIBUTE=tenant_slug
KEYCLOAK_TENANT_ID_ATTRIBUTE=tenant_id
```

## Monitoring and Observability

### 1. Tenant Metrics

- Tenant creation/deletion rates
- Schema performance metrics
- Cross-tenant access attempts

### 2. Logging

```java
@Slf4j
public class TenantService {
    public Tenant createTenant(String name, String slug) {
        log.info("Creating tenant: {} with slug: {}", name, slug);
        // ... implementation
        log.info("Tenant created successfully: {}", tenant.getId());
    }
}
```

### 3. Health Checks

- Tenant schema availability
- Keycloak tenant connectivity
- Cross-tenant data isolation

## Best Practices

### 1. Tenant Naming

- Use lowercase, alphanumeric slugs
- Avoid reserved database keywords
- Implement slug validation

### 2. Schema Management

- Regular schema health checks
- Backup tenant schemas individually
- Monitor schema growth

### 3. Performance

- Index tenant-specific tables
- Use connection pooling per tenant
- Implement tenant-aware caching

### 4. Scalability

- Consider separate databases for large tenants
- Implement tenant data archiving
- Plan for tenant migration strategies

## Troubleshooting

### Common Issues

1. **Tenant Context Not Set**
   - Check JWT token claims
   - Verify tenant resolver configuration
   - Ensure proper interceptor registration

2. **Schema Not Found**
   - Verify tenant exists in registry
   - Check schema creation process
   - Validate search path configuration

3. **Cross-Tenant Data Access**
   - Review security configurations
   - Check tenant context validation
   - Audit authorization rules

### Debug Commands

```sql
-- Check tenant schemas
SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%';

-- Verify tenant data isolation
SELECT current_setting('search_path');

-- Check tenant registry
SELECT * FROM tenant_management.tenants WHERE status = 'active';
```

## Future Enhancements

1. **Tenant Customization**: Custom fields and workflows per tenant
2. **Tenant Migration**: Tools for moving tenants between environments
3. **Multi-Region**: Geographic tenant distribution
4. **Tenant Analytics**: Usage and performance metrics per tenant
5. **API Versioning**: Tenant-specific API versions

## Conclusion

This multi-tenancy implementation provides:

- **Complete Data Isolation**: Schema-per-tenant architecture
- **Scalable Architecture**: Support for unlimited tenants
- **Security**: Tenant-aware authentication and authorization
- **Flexibility**: Tenant-specific customization capabilities
- **Observability**: Comprehensive monitoring and logging

The implementation follows industry best practices and provides a solid foundation for building a multi-tenant SaaS application.
