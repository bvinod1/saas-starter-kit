# Property and Secret Management Best Practices for Multi-Environment Applications

## Overview

This document describes the comprehensive property and secret management system implemented for the SaaS Starter Kit, providing secure, hierarchical configuration loading with integration to external secret management systems.

## Architecture

### 1. **Property File Organization & Management**

The system follows a hierarchical configuration approach with clear separation of concerns:

```
Common Properties (common.yml)
    ↓
Environment-Specific Properties (application-{profile}.yml)
    ↓
Secrets from Secret Manager
    ↓
Environment Variables (Runtime Overrides)
```

#### **Common Properties (`common.yml`)**
- **Purpose**: Store non-sensitive, common configurations shared across all environments
- **Content**: Application information, logging patterns, feature flags, security policies
- **Status**: ✅ **Committed to version control**
- **Usage**: Base configuration that applies to all environments

#### **Environment-Specific Properties**
- **Development**: `application-dev.yml` - Debug settings, local endpoints, verbose logging
- **Production**: `application-prod.yml` - Performance tuning, security hardening, external URLs
- **Status**: ✅ **Committed to version control**
- **Content**: Environment-dependent but non-sensitive values (service endpoints, logging levels)

#### **Secrets Management**
- **Status**: ❌ **Never committed to source control**
- **Storage**: External secret management systems (AWS Secrets Manager, HashiCorp Vault, etc.)
- **Organization**: 
  - Common secrets under `common/` namespace
  - Environment-specific secrets under `{environment}/` namespace

### 2. **Configuration Loading & Override Order**

Configuration values are resolved with this precedence (lowest to highest priority):

1. **Common property file** (`common.yml`) - Base defaults
2. **Environment-specific property file** (`application-{profile}.yml`) - Environment overrides
3. **Common secrets** from secret manager (`common/*`) - Shared sensitive data
4. **Environment-specific secrets** from secret manager (`{environment}/*`) - Environment secrets
5. **Environment variables** - Runtime overrides for deployment-specific values

**Example Override Chain:**
```yaml
# common.yml
security:
  jwt:
    expiration: 3600  # Base: 1 hour

# application-prod.yml  
security:
  jwt:
    expiration: 1800  # Override: 30 minutes

# Secret Manager: prod/security.jwt.expiration
# Value: 900 (15 minutes) - Highest priority
```

### 3. **Secret Manager Interface Design**

The system provides a generic interface for secret manager adapters:

#### **Core Interface: `SecretManager`**
```java
public interface SecretManager {
    Optional<String> getSecret(String secretKey);
    CompletableFuture<Optional<String>> getSecretAsync(String secretKey);
    boolean putSecret(String secretKey, String secretValue);
    boolean deleteSecret(String secretKey);
    boolean secretExists(String secretKey);
    String[] listSecretKeys();
    SecretManagerHealth getHealth();
    // ... additional methods
}
```

#### **Supporting Classes**
- **`SecretMetadata`**: Information about secrets (creation date, type, encryption status)
- **`SecretManagerHealth`**: Health status and operational metrics
- **`ConfigurationService`**: Integration layer between properties and secrets

### 4. **Configuration Service Integration**

The `ConfigurationService` provides a unified interface for accessing configuration:

```java
@Service
public class ConfigurationService {
    
    // Get configuration with fallback
    public Optional<String> getConfiguration(String key);
    
    // Get configuration with default value
    public String getConfiguration(String key, String defaultValue);
    
    // Get typed configuration
    public <T> T getConfiguration(String key, Class<T> type, T defaultValue);
    
    // Convenience methods
    public boolean getBoolean(String key, boolean defaultValue);
    public int getInt(String key, int defaultValue);
    public long getLong(String key, long defaultValue);
    
    // Refresh configuration cache
    public void refreshCache();
}
```

## Implementation Details

### 1. **Property File Structure**

#### **Common Properties (`common.yml`)**
```yaml
# Application Information
app:
  name: "SaaS Starter Kit - User Service"
  version: "1.0.0"
  description: "Multi-tenant user management service"

# Feature Flags
features:
  multi_tenancy: true
  advanced_logging: true
  opentelemetry: true
  configuration_management: true
  compliance_tracking: true

# Security Configuration
security:
  jwt:
    expiration: 3600
    refresh_expiration: 86400
  password:
    min_length: 8
    require_special_chars: true
    require_numbers: true
    require_uppercase: true
```

#### **Development Properties (`application-dev.yml`)**
```yaml
spring:
  profiles:
    active: dev
  application:
    name: "SaaS Starter Kit - User Service (DEV)"

# Development-specific overrides
app:
  environment: "development"
  debug: true

# Logging Configuration (Development)
logging:
  level:
    root: DEBUG
    com.saasstarterkit: TRACE
    org.springframework.security: DEBUG

# Database Configuration (Development)
spring:
  datasource:
    url: "jdbc:postgresql://localhost:5432/saas_starter_dev"
    username: "${DB_USERNAME:dev_user}"
    password: "${DB_PASSWORD:dev_password}"
```

#### **Production Properties (`application-prod.yml`)**
```yaml
spring:
  profiles:
    active: prod
  application:
    name: "SaaS Starter Kit - User Service (PROD)"

# Production-specific overrides
app:
  environment: "production"
  debug: false

# Logging Configuration (Production)
logging:
  level:
    root: WARN
    com.saasstarterkit: INFO

# Database Configuration (Production)
spring:
  datasource:
    url: "${DATABASE_URL}"
    username: "${DATABASE_USERNAME}"
    password: "${DATABASE_PASSWORD}"
```

### 2. **Secret Management Integration**

#### **Secret Namespace Structure**
```
common/
├── database/
│   ├── master_password
│   └── encryption_key
├── security/
│   ├── jwt_secret
│   └── admin_token
└── external/
    ├── email_api_key
    └── sms_api_key

dev/
├── database/
│   └── dev_password
└── external/
    └── dev_api_key

prod/
├── database/
│   └── prod_password
├── security/
│   └── prod_jwt_secret
└── external/
    ├── prod_email_api_key
    └── prod_sms_api_key
```

#### **Secret Manager Adapters**
The system supports multiple secret management backends:

- **AWS Secrets Manager**: Enterprise-grade secret management
- **HashiCorp Vault**: Open-source secret management
- **Azure Key Vault**: Microsoft cloud secret management
- **Local Development**: File-based secrets for development

### 3. **Environment Variable Support**

All configuration values can be overridden using environment variables:

```bash
# Database configuration
export DATABASE_URL="jdbc:postgresql://prod-db:5432/saas_prod"
export DATABASE_USERNAME="saas_user"
export DATABASE_PASSWORD="secure_password"

# Security configuration
export JWT_SECRET="your-super-secure-jwt-secret"
export JWT_EXPIRATION="1800"

# Feature flags
export FEATURE_DEBUG_MODE="false"
export FEATURE_MOCK_EXTERNAL_SERVICES="false"

# External services
export AUTH_SERVICE_URL="https://auth.prod.example.com"
export NOTIFICATION_SERVICE_URL="https://notifications.prod.example.com"
```

## Security Considerations

### 1. **Secret Protection**
- **Never commit secrets to version control**
- **Use environment variables for deployment-specific secrets**
- **Encrypt secrets at rest and in transit**
- **Implement secret rotation policies**

### 2. **Access Control**
- **Role-based access control (RBAC) for configuration management**
- **Audit logging for all configuration changes**
- **Principle of least privilege for secret access**

### 3. **Compliance**
- **GDPR compliance for data retention policies**
- **HIPAA compliance for healthcare data**
- **SOC 2 compliance for security controls**
- **Regular security audits and penetration testing**

## Best Practices

### 1. **Configuration Naming**
```
feature.{feature_name}           # Feature flags
security.{setting_name}          # Security settings
logging.{component}.{level}      # Logging configuration
database.{setting_name}          # Database configuration
external.{service}.{setting}     # External service settings
```

### 2. **Environment Management**
- **Use `default` for global defaults**
- **Use `dev`, `test`, `staging`, `prod` for environments**
- **Keep environment-specific files minimal**
- **Use environment variables for deployment secrets**

### 3. **Secret Management**
- **Use descriptive secret names with namespace prefixes**
- **Implement secret versioning and rotation**
- **Monitor secret access and usage**
- **Regular backup and disaster recovery testing**

### 4. **Configuration Validation**
- **Validate configuration at startup**
- **Use configuration schemas for validation**
- **Implement configuration health checks**
- **Monitor configuration drift**

## Usage Examples

### 1. **Accessing Configuration in Code**

```java
@Service
public class UserService {
    
    @Autowired
    private ConfigurationService configService;
    
    public void processUser(User user) {
        // Get configuration values
        boolean debugMode = configService.getBoolean("features.debug_mode", false);
        int sessionTimeout = configService.getInt("security.session.timeout", 3600);
        String dbUrl = configService.getConfiguration("spring.datasource.url", "default-url");
        
        if (debugMode) {
            logger.debug("Processing user: {}", user);
        }
        
        // Use configuration values
        if (sessionTimeout > 0) {
            // Apply session timeout
        }
    }
}
```

### 2. **Secret Manager Integration**

```java
@Service
public class DatabaseService {
    
    @Autowired
    private SecretManager secretManager;
    
    public void connectToDatabase() {
        // Get database credentials from secret manager
        Optional<String> password = secretManager.getSecret("common/database/master_password");
        
        if (password.isPresent()) {
            // Use password for database connection
            String dbPassword = password.get();
            // ... database connection logic
        } else {
            throw new RuntimeException("Database password not found in secret manager");
        }
    }
}
```

### 3. **Configuration Refresh**

```java
@RestController
@RequestMapping("/api/config")
public class ConfigurationController {
    
    @Autowired
    private ConfigurationService configService;
    
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<String> refreshConfiguration() {
        configService.refreshCache();
        return ResponseEntity.ok("Configuration refreshed successfully");
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConfigurationStats() {
        return ResponseEntity.ok(configService.getConfigurationStats());
    }
}
```

## Monitoring and Alerting

### 1. **Configuration Health Checks**
- **Secret manager connectivity**
- **Configuration cache status**
- **Configuration validation results**
- **Secret expiration monitoring**

### 2. **Metrics and Observability**
- **Configuration access patterns**
- **Secret retrieval latency**
- **Configuration change frequency**
- **Error rates and failure modes**

### 3. **Alerting Rules**
- **Secret manager unavailable**
- **Configuration validation failures**
- **Secret expiration warnings**
- **Configuration drift detection**

## Troubleshooting

### 1. **Common Issues**

#### **Configuration Not Found**
```bash
# Check if configuration exists
curl "http://localhost:8081/actuator/env" | grep "your.config.key"

# Check configuration service logs
tail -f logs/application.log | grep "ConfigurationService"
```

#### **Secret Manager Issues**
```bash
# Check secret manager health
curl "http://localhost:8081/actuator/health" | grep "secretManager"

# Check secret manager connectivity
# (Implementation specific to your secret manager)
```

#### **Environment Variable Issues**
```bash
# Verify environment variables are set
env | grep "YOUR_CONFIG_KEY"

# Check Spring profile activation
echo $SPRING_PROFILES_ACTIVE
```

### 2. **Debug Endpoints**
```bash
# Configuration statistics
curl "http://localhost:8081/api/config/stats"

# Environment information
curl "http://localhost:8081/actuator/env"

# Configuration refresh
curl -X POST "http://localhost:8081/api/config/refresh"
```

## Future Enhancements

### 1. **Advanced Features**
- **Configuration templates and inheritance**
- **Dynamic configuration updates**
- **Configuration versioning and rollback**
- **Configuration dependency management**

### 2. **Integration Improvements**
- **Configuration change notifications**
- **Configuration drift detection**
- **Automated configuration validation**
- **Configuration compliance reporting**

### 3. **Security Enhancements**
- **Configuration encryption at rest**
- **Configuration access auditing**
- **Configuration change approval workflows**
- **Configuration backup and recovery**

## Conclusion

This property and secret management system provides a robust, secure, and scalable solution for managing multi-environment application configurations. It follows industry best practices for security, compliance, and operational excellence while maintaining flexibility for different deployment scenarios.

The hierarchical configuration approach ensures that common settings are shared across environments while allowing for environment-specific customizations. The integration with external secret management systems provides enterprise-grade security for sensitive configuration data.

For additional support or questions, refer to the API documentation or contact the development team.
