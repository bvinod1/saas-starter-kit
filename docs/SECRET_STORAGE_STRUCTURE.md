# Secret Storage Structure and Namespace Conventions

## Overview

This document defines the standardized secret storage structure and namespace conventions used across all secret management backends in the SaaS Starter Kit. These conventions ensure consistency, security, and maintainability across different environments and cloud providers.

## 🏗️ **Secret Storage Architecture**

### **Hierarchical Namespace Structure**

```
Root Namespace
├── common/                    # Shared across all environments
│   ├── database/             # Database credentials
│   ├── security/             # Security keys and tokens
│   └── external/             # External service credentials
├── dev/                      # Development environment
├── staging/                  # Staging environment
├── prod/                     # Production environment
└── tenants/                  # Tenant-specific secrets
    ├── {tenant-id-1}/        # Individual tenant secrets
    ├── {tenant-id-2}/
    └── {tenant-id-n}/
```

### **Environment Separation**

- **`common/`**: Secrets shared across all environments (e.g., master encryption keys)
- **`dev/`**: Development-specific secrets (e.g., local database passwords)
- **`staging/`**: Staging environment secrets (e.g., test API keys)
- **`prod/`**: Production environment secrets (e.g., live service credentials)
- **`tenants/`**: Multi-tenant secrets organized by tenant ID

## 📁 **Secret Path Conventions**

### **1. Database Credentials**

```
common/database/
├── master-password           # Master database password
├── encryption-key            # Database encryption key
├── backup-credentials        # Backup service credentials
└── read-replica-password     # Read replica password

tenants/{tenant-id}/database/
├── password                  # Tenant-specific database password
├── username                  # Tenant-specific database username
└── connection-string         # Tenant-specific connection string
```

**Example Usage:**
```java
// Get common database credentials
String masterPassword = secretService.getSecret("common/database/master-password");

// Get tenant-specific credentials
String tenantPassword = secretService.getSecret("tenants/tenant-123/database/password");
```

### **2. Security Secrets**

```
common/security/
├── jwt/
│   ├── signing-key           # JWT signing key
│   ├── refresh-key           # JWT refresh token key
│   └── expiration            # JWT expiration settings
├── encryption/
│   ├── master-key            # Master encryption key
│   ├── data-key              # Data encryption key
│   └── backup-key            # Backup encryption key
└── api-keys/
    ├── email-service         # Email service API key
    ├── sms-service           # SMS service API key
    └── payment-gateway       # Payment gateway API key
```

**Example Usage:**
```java
// Get JWT signing key
String jwtKey = secretService.getSecret("common/security/jwt/signing-key");

// Get email service API key
String emailApiKey = secretService.getSecret("common/security/api-keys/email-service");
```

### **3. External Service Credentials**

```
common/external/
├── email/
│   ├── smtp-password         # SMTP server password
│   ├── api-key               # Email service API key
│   └── webhook-secret        # Email webhook secret
├── sms/
│   ├── api-key               # SMS service API key
│   ├── account-sid           # SMS account SID
│   └── auth-token            # SMS auth token
└── payment/
    ├── stripe-secret-key     # Stripe secret key
    ├── paypal-client-secret  # PayPal client secret
    └── webhook-secret        # Payment webhook secret
```

**Example Usage:**
```java
// Get email service credentials
Map<String, String> emailSecrets = secretService.getSecretsByPath("common/external/email");

// Get specific payment secret
String stripeKey = secretService.getSecret("common/external/payment/stripe-secret-key");
```

### **4. Tenant-Specific Configuration**

```
tenants/{tenant-id}/
├── config/
│   ├── feature-flags         # Tenant-specific feature toggles
│   ├── branding              # Branding configuration
│   └── limits                # Usage limits and quotas
├── features/
│   ├── advanced-logging      # Advanced logging enabled
│   ├── compliance-tracking   # Compliance features enabled
│   └── custom-integrations   # Custom integration settings
└── integrations/
    ├── webhook-urls          # Webhook endpoints
    ├── api-keys              # Tenant-specific API keys
    └── custom-fields         # Custom field mappings
```

**Example Usage:**
```java
// Get tenant configuration
Map<String, String> tenantConfig = secretService.getSecretsByPath("tenants/tenant-123/config");

// Get specific tenant feature
String featureFlags = secretService.getSecret("tenants/tenant-123/features/feature-flags");
```

## 🔐 **Secret Naming Conventions**

### **1. Naming Rules**

- **Use lowercase with hyphens**: `database-password`, `jwt-signing-key`
- **Be descriptive**: `stripe-production-secret-key` not just `stripe-key`
- **Include environment context**: `dev-database-password`, `prod-database-password`
- **Use consistent abbreviations**: `api-key`, `webhook-secret`, `encryption-key`

### **2. Versioning Strategy**

```
common/security/jwt/signing-key-v1     # Version 1 of JWT signing key
common/security/jwt/signing-key-v2     # Version 2 of JWT signing key
common/security/jwt/signing-key        # Current version (latest)
```

### **3. Regional Variations**

```
common/database/us-east-1/master-password    # US East region
common/database/eu-west-1/master-password    # EU West region
common/database/asia-pacific/master-password # Asia Pacific region
```

## 🌍 **Cloud Provider Specific Implementations**

### **1. AWS Secrets Manager**

**Path Structure:**
```
saas-starter-kit/
├── common/
│   ├── database/
│   ├── security/
│   └── external/
├── dev/
├── staging/
├── prod/
└── tenants/
    ├── tenant-123/
    ├── tenant-456/
    └── tenant-789/
```

**Configuration:**
```yaml
aws:
  secrets:
    manager:
      region: us-east-1
      namespace: saas-starter-kit
      cache:
        ttl: 300
        max-size: 1000
```

### **2. Google Cloud Secret Manager**

**Path Structure:**
```
projects/saas-starter-kit/secrets/
├── common-database-master-password
├── common-security-jwt-signing-key
├── dev-database-password
├── prod-database-password
└── tenant-123-database-password
```

**Configuration:**
```yaml
gcp:
  secrets:
    manager:
      project-id: saas-starter-kit
      cache:
        ttl: 300
        max-size: 1000
```

### **3. Azure Key Vault**

**Path Structure:**
```
https://saas-starter-kit.vault.azure.net/secrets/
├── common-database-master-password
├── common-security-jwt-signing-key
├── dev-database-password
├── prod-database-password
└── tenant-123-database-password
```

**Configuration:**
```yaml
azure:
  keyvault:
    url: https://saas-starter-kit.vault.azure.net
    tenant-id: your-tenant-id
    client-id: your-client-id
    client-secret: your-client-secret
```

### **4. HashiCorp Vault (Local/Development)**

**Path Structure:**
```
secret/
├── common/
│   ├── database/
│   ├── security/
│   └── external/
├── dev/
├── staging/
├── prod/
└── tenants/
    ├── tenant-123/
    ├── tenant-456/
    └── tenant-789/
```

**Configuration:**
```yaml
vault:
  url: http://localhost:8200
  token: your-vault-token
  namespace: secret
  cache:
    ttl: 300
    max-size: 1000
```

## 🚀 **Implementation Examples**

### **1. Database Service Integration**

```java
@Service
public class DatabaseService {
    
    @Autowired
    private SecretManagerService secretService;
    
    public void connectToDatabase(String tenantId) {
        try {
            // Get common database credentials
            String masterPassword = secretService.getSecret("common/database/master-password");
            String encryptionKey = secretService.getSecret("common/database/encryption-key");
            
            // Get tenant-specific credentials if available
            String tenantPassword = null;
            try {
                tenantPassword = secretService.getSecret("tenants/" + tenantId + "/database/password");
            } catch (Exception e) {
                logger.debug("No tenant-specific password found, using common credentials");
            }
            
            // Use credentials for database connection
            String password = tenantPassword != null ? tenantPassword : masterPassword;
            // ... database connection logic
            
        } catch (Exception e) {
            logger.error("Failed to retrieve database credentials", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }
}
```

### **2. Security Service Integration**

```java
@Service
public class SecurityService {
    
    @Autowired
    private SecretManagerService secretService;
    
    public String generateJwtToken(String userId, String tenantId) {
        try {
            // Get JWT signing key
            String signingKey = secretService.getSecret("common/security/jwt/signing-key");
            
            // Get tenant-specific JWT settings if available
            String tenantJwtKey = null;
            try {
                tenantJwtKey = secretService.getSecret("tenants/" + tenantId + "/security/jwt-key");
            } catch (Exception e) {
                logger.debug("No tenant-specific JWT key found, using common key");
            }
            
            // Use tenant-specific key if available, otherwise fall back to common key
            String keyToUse = tenantJwtKey != null ? tenantJwtKey : signingKey;
            
            // Generate JWT token
            // ... JWT generation logic
            
        } catch (Exception e) {
            logger.error("Failed to retrieve JWT signing key", e);
            throw new RuntimeException("JWT generation failed", e);
        }
    }
}
```

### **3. Feature Flag Service Integration**

```java
@Service
public class FeatureFlagService {
    
    @Autowired
    private SecretManagerService secretService;
    
    public boolean isFeatureEnabled(String featureName, String tenantId) {
        try {
            // Get tenant-specific feature flags
            Map<String, String> tenantFeatures = secretService.getSecretsByPath("tenants/" + tenantId + "/features");
            
            // Check if feature is enabled for this tenant
            String featureValue = tenantFeatures.get(featureName);
            if (featureValue != null) {
                return Boolean.parseBoolean(featureValue);
            }
            
            // Fall back to common feature flags
            Map<String, String> commonFeatures = secretService.getSecretsByPath("common/features");
            String commonFeatureValue = commonFeatures.get(featureName);
            
            return commonFeatureValue != null ? Boolean.parseBoolean(commonFeatureValue) : false;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve feature flags for tenant: {}", tenantId, e);
            return false; // Default to disabled on error
        }
    }
}
```

## 🔒 **Security Best Practices**

### **1. Access Control**

- **Principle of Least Privilege**: Only grant access to secrets that are actually needed
- **Role-Based Access Control (RBAC)**: Use different roles for different environments
- **Audit Logging**: Log all secret access for compliance and security monitoring

### **2. Secret Rotation**

- **Regular Rotation**: Rotate secrets on a regular schedule (e.g., every 90 days)
- **Automated Rotation**: Use cloud provider tools for automated secret rotation
- **Version Management**: Keep multiple versions of secrets during rotation

### **3. Encryption**

- **At Rest**: Ensure secrets are encrypted at rest in the secret manager
- **In Transit**: Use TLS/HTTPS for all communications with secret managers
- **Application Level**: Consider additional encryption for highly sensitive secrets

## 📊 **Monitoring and Observability**

### **1. Health Checks**

```java
@Component
public class SecretManagerHealthIndicator implements HealthIndicator {
    
    @Autowired
    private SecretManagerService secretService;
    
    @Override
    public Health health() {
        try {
            // Test secret retrieval
            String testSecret = secretService.getSecret("common/health-check/test");
            
            return Health.up()
                    .withDetail("secretManager", "healthy")
                    .withDetail("lastCheck", System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            return Health.down()
                    .withDetail("secretManager", "unhealthy")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

### **2. Metrics Collection**

```java
@Component
public class SecretManagerMetrics {
    
    @Autowired
    private SecretManagerService secretService;
    
    @EventListener
    public void onSecretRetrieved(SecretRetrievedEvent event) {
        // Increment counter for secret retrieval
        Counter.builder("secrets.retrieved")
                .tag("secret.path", event.getSecretPath())
                .tag("tenant.id", event.getTenantId())
                .register(Metrics.globalRegistry)
                .increment();
    }
}
```

## 🚨 **Error Handling and Fallbacks**

### **1. Fallback Strategy**

```java
@Service
public class ResilientSecretService {
    
    @Autowired
    private SecretManagerService secretService;
    
    @Value("${secrets.fallback-to-env:true}")
    private boolean fallbackToEnv;
    
    public String getSecretWithFallback(String secretPath, String envVarName) {
        try {
            // Try to get from secret manager first
            return secretService.getSecret(secretPath);
            
        } catch (Exception e) {
            logger.warn("Failed to retrieve secret from secret manager: {}", secretPath, e);
            
            if (fallbackToEnv) {
                // Fall back to environment variable
                String envValue = System.getenv(envVarName);
                if (envValue != null) {
                    logger.info("Using environment variable as fallback: {}", envVarName);
                    return envValue;
                }
            }
            
            throw new RuntimeException("Secret not available from any source: " + secretPath, e);
        }
    }
}
```

### **2. Circuit Breaker Pattern**

```java
@Service
public class CircuitBreakerSecretService {
    
    @Autowired
    private SecretManagerService secretService;
    
    private final CircuitBreaker circuitBreaker = CircuitBreaker.builder()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .ringBufferSizeInHalfOpenState(2)
            .ringBufferSizeInClosedState(10)
            .build();
    
    public String getSecretWithCircuitBreaker(String secretPath) {
        return circuitBreaker.executeSupplier(() -> secretService.getSecret(secretPath));
    }
}
```

## 📝 **Configuration Examples**

### **1. Environment Variables**

```bash
# AWS Secrets Manager
export AWS_REGION=us-east-1
export SECRETS_BACKEND_TYPE=aws
export SECRETS_MANAGER_ENABLED=true

# Google Cloud Secret Manager
export GCP_PROJECT_ID=saas-starter-kit
export SECRETS_BACKEND_TYPE=gcp
export SECRETS_MANAGER_ENABLED=true

# Azure Key Vault
export AZURE_KEYVAULT_URL=https://saas-starter-kit.vault.azure.net
export AZURE_TENANT_ID=your-tenant-id
export AZURE_CLIENT_ID=your-client-id
export AZURE_CLIENT_SECRET=your-client-secret
export SECRETS_BACKEND_TYPE=azure
export SECRETS_MANAGER_ENABLED=true

# HashiCorp Vault
export VAULT_URL=http://localhost:8200
export VAULT_TOKEN=your-vault-token
export SECRETS_BACKEND_TYPE=vault
export SECRETS_MANAGER_ENABLED=true

# Local Development (No Secret Manager)
export SECRETS_BACKEND_TYPE=local
export SECRETS_MANAGER_ENABLED=false
export SECRETS_FALLBACK_TO_ENV=true
```

### **2. Docker Compose Configuration**

```yaml
version: '3.8'
services:
  vault:
    image: vault:latest
    ports:
      - "8200:8200"
    environment:
      - VAULT_DEV_ROOT_TOKEN_ID=dev-token
      - VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200
    cap_add:
      - IPC_LOCK
    volumes:
      - ./vault-config:/vault/config
      - vault-data:/vault/file

  user-service:
    environment:
      - SECRETS_BACKEND_TYPE=vault
      - SECRETS_MANAGER_ENABLED=true
      - VAULT_URL=http://vault:8200
      - VAULT_TOKEN=dev-token
```

## 🔄 **Migration and Deployment**

### **1. Secret Migration Script**

```bash
#!/bin/bash
# migrate-secrets.sh

SOURCE_BACKEND="local"
TARGET_BACKEND="aws"

echo "Migrating secrets from $SOURCE_BACKEND to $TARGET_BACKEND..."

# Set source backend
export SECRETS_BACKEND_TYPE=$SOURCE_BACKEND
export SECRETS_MANAGER_ENABLED=true

# Set target backend
export TARGET_SECRETS_BACKEND_TYPE=$TARGET_BACKEND
export TARGET_AWS_REGION=us-east-1

# Run migration
java -jar secret-migration-tool.jar \
    --source-backend=$SOURCE_BACKEND \
    --target-backend=$TARGET_BACKEND \
    --dry-run=false
```

### **2. Secret Validation**

```java
@Component
public class SecretValidator {
    
    @Autowired
    private SecretManagerService secretService;
    
    public ValidationResult validateSecrets() {
        ValidationResult result = new ValidationResult();
        
        // Validate required secrets
        String[] requiredSecrets = {
            "common/database/master-password",
            "common/security/jwt/signing-key",
            "common/security/encryption/master-key"
        };
        
        for (String secretPath : requiredSecrets) {
            try {
                String secret = secretService.getSecret(secretPath);
                if (secret != null && !secret.trim().isEmpty()) {
                    result.addValidSecret(secretPath);
                } else {
                    result.addInvalidSecret(secretPath, "Secret is empty");
                }
            } catch (Exception e) {
                result.addInvalidSecret(secretPath, e.getMessage());
            }
        }
        
        return result;
    }
}
```

## 📚 **Conclusion**

This secret storage structure and namespace convention provides a robust, scalable, and secure foundation for managing secrets across different environments and cloud providers. By following these conventions, you ensure:

- **Consistency**: Uniform structure across all environments
- **Security**: Proper separation and access control
- **Maintainability**: Clear organization and naming conventions
- **Scalability**: Support for multi-tenancy and growth
- **Compliance**: Audit trails and security best practices

For additional support or questions, refer to the API documentation or contact the development team.
