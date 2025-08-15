# Multi-Tenant Configuration Management

## Overview

This document describes the comprehensive configuration management system implemented for the SaaS Starter Kit, providing tenant-aware configuration storage, dynamic loading, secure access, and audit trails.

## Architecture

### 1. **Tenant-Aware Configuration Storage**

The system uses a hierarchical configuration approach:

```
Global Defaults (environment: default)
    ↓
Environment-Specific (environment: dev/test/prod)
    ↓
Tenant-Specific Overrides (tenant_id: specific)
```

**Storage Structure:**
- **Global Configs**: `tenant_id = NULL`, accessible by all tenants
- **Environment Configs**: Environment-specific overrides (dev, test, prod)
- **Tenant Configs**: Tenant-specific customizations and overrides

### 2. **Configuration Types**

| Type | Description | Example |
|------|-------------|---------|
| `STRING` | Text values | `"SaaS Starter Kit"` |
| `INTEGER` | Numeric values | `3600` |
| `BOOLEAN` | True/false values | `true` |
| `JSON` | Complex data structures | `{"key": "value"}` |
| `ENCRYPTED` | Sensitive encrypted data | `encrypted_value` |
| `FEATURE_FLAG` | Feature toggle flags | `true` |

### 3. **Security & Access Control**

#### RBAC Implementation
```java
@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
public ResponseEntity<TenantConfiguration> setConfiguration(...)
```

**Roles:**
- **TENANT_ADMIN**: Can manage configurations for their own tenant
- **PLATFORM_ADMIN**: Can manage all configurations across tenants

#### Data Encryption
- Sensitive configurations are automatically encrypted using AES encryption
- Encryption key is configurable and should be stored securely in production
- Supports both encrypted and plain-text configurations

### 4. **API Endpoints**

#### Configuration Management
```
GET    /api/tenant-config/{tenantId}/{configKey}     # Get specific config
GET    /api/tenant-config/{tenantId}                 # Get all tenant configs
POST   /api/tenant-config/{tenantId}/{configKey}     # Set config value
POST   /api/tenant-config/{tenantId}/bulk            # Bulk update configs
DELETE /api/tenant-config/{tenantId}/{configKey}     # Delete config
```

#### Feature Flags
```
GET    /api/tenant-config/{tenantId}/feature/{featureKey}  # Get feature flag
```

#### Audit & History
```
GET    /api/tenant-config/{tenantId}/{configKey}/history   # Get config history
GET    /api/tenant-config/{tenantId}/stats                 # Get config statistics
```

#### Cache Management
```
POST   /api/tenant-config/cache/refresh                    # Refresh config cache
```

### 5. **Usage Examples**

#### Setting a Configuration
```bash
curl -X POST "http://localhost:8081/api/tenant-config/tenant-123/feature.new_ui" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "value": "true",
    "type": "BOOLEAN",
    "userId": "user-456",
    "metadata": {
      "description": "Enable new UI for tenant",
      "category": "features"
    }
  }'
```

#### Getting a Configuration
```bash
curl "http://localhost:8081/api/tenant-config/tenant-123/feature.new_ui?environment=prod"
```

#### Bulk Update
```bash
curl -X POST "http://localhost:8081/api/tenant-config/tenant-123/bulk" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": "user-456",
    "configs": {
      "feature.new_ui": true,
      "feature.advanced_logging": false,
      "billing.currency": "EUR"
    }
  }'
```

### 6. **Configuration Hierarchy Resolution**

The system follows this priority order when resolving configurations:

1. **Tenant-Specific + Environment**: Highest priority
2. **Environment-Specific Global**: Medium priority  
3. **Global Default**: Lowest priority

**Example:**
```yaml
# Global default
app.name: "SaaS Starter Kit"

# Environment override
app.name: "SaaS Starter Kit - DEV"  # environment: dev

# Tenant override
app.name: "Acme Corp Platform"      # tenant: acme-corp, environment: dev
```

### 7. **Caching Strategy**

- **In-Memory Cache**: Frequently accessed configurations
- **Cache Keys**: `{tenantId}_{configKey}_{environment}`
- **Cache Invalidation**: Automatic on configuration changes
- **Manual Refresh**: Available via API endpoint

### 8. **Versioning & Rollback**

#### Version Management
- Each configuration change creates a new version
- Previous versions are marked as inactive
- Full audit trail maintained for compliance

#### Rollback Process
```bash
curl -X POST "http://localhost:8081/api/tenant-config/tenant-123/feature.new_ui/rollback" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId": "user-456"}'
```

### 9. **Monitoring & Alerting**

#### Metrics Available
- Total configurations per tenant
- Feature flag counts
- Encrypted configuration counts
- Configuration change frequency

#### Audit Trail
- All configuration changes logged
- User attribution for changes
- Timestamp tracking
- Metadata for change context

### 10. **Best Practices**

#### Configuration Naming
```
feature.{feature_name}           # Feature flags
security.{setting_name}          # Security settings
logging.{component}.{level}      # Logging configuration
billing.{setting_name}           # Billing configuration
notification.{channel}.{setting} # Notification settings
```

#### Environment Management
- Use `default` for global defaults
- Use `dev`, `test`, `staging`, `prod` for environments
- Tenant-specific overrides inherit environment context

#### Security Considerations
- Encrypt sensitive configurations (API keys, passwords)
- Use RBAC for access control
- Audit all configuration changes
- Regular security reviews of configuration access

### 11. **Integration with Other Services**

#### Frontend Integration
```typescript
// Get configuration value
const configValue = await api.get(`/api/tenant-config/${tenantId}/${configKey}`);

// Check feature flag
const isEnabled = await api.get(`/api/tenant-config/${tenantId}/feature/${featureKey}`);
```

#### Backend Integration
```java
@Autowired
private TenantConfigurationService configService;

// Get configuration
String value = configService.getConfiguration(tenantId, "feature.new_ui", environment);

// Check feature flag
boolean enabled = configService.getFeatureFlag(tenantId, "new_ui", environment);
```

### 12. **Database Schema**

#### Table: `tenant_configurations`
```sql
CREATE TABLE tenant_configurations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    environment VARCHAR(100) NOT NULL DEFAULT 'default',
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_encrypted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    metadata JSONB
);
```

#### Views
- `active_tenant_configs`: Current active configurations
- `config_audit_trail`: Complete configuration history

### 13. **Troubleshooting**

#### Common Issues

1. **Configuration Not Found**
   - Check tenant ID and environment
   - Verify configuration key exists
   - Check RBAC permissions

2. **Cache Issues**
   - Use cache refresh endpoint
   - Check configuration changes
   - Verify cache keys

3. **Encryption Errors**
   - Verify encryption key configuration
   - Check configuration type
   - Validate encrypted values

#### Debug Endpoints
```bash
# Get configuration statistics
curl "http://localhost:8081/api/tenant-config/tenant-123/stats"

# Get configuration history
curl "http://localhost:8081/api/tenant-config/tenant-123/feature.new_ui/history"
```

### 14. **Future Enhancements**

- **Configuration Templates**: Predefined configuration sets
- **Scheduled Rollouts**: Time-based configuration changes
- **A/B Testing**: Configuration-based feature testing
- **Configuration Validation**: Schema validation for configurations
- **Webhook Notifications**: Real-time configuration change notifications
- **Configuration Import/Export**: Bulk configuration management

## Conclusion

This configuration management system provides a robust, secure, and scalable solution for managing multi-tenant SaaS configurations. It supports hierarchical configuration loading, secure access control, comprehensive audit trails, and easy integration with both frontend and backend services.

For additional support or questions, refer to the API documentation or contact the development team.
