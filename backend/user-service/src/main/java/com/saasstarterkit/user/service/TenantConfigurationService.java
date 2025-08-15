package com.saasstarterkit.user.service;

import com.saasstarterkit.user.entity.TenantConfiguration;
import com.saasstarterkit.user.repository.TenantConfigurationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class TenantConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantConfigurationService.class);
    
    @Autowired
    private TenantConfigurationRepository configRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // In-memory cache for frequently accessed configs
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();
    
    // Encryption key (in production, this should be stored securely)
    private static final String ENCRYPTION_KEY = "your-secure-encryption-key-32-chars";
    
    /**
     * Get configuration value with hierarchical fallback:
     * 1. Tenant-specific config
     * 2. Environment-specific global config
     * 3. Global default config
     */
    @Cacheable(value = "tenantConfigs", key = "#tenantId + '_' + #configKey + '_' + #environment")
    public String getConfiguration(String tenantId, String configKey, String environment) {
        try {
            // Try tenant-specific config first
            Optional<TenantConfiguration> tenantConfig = configRepository
                .findByTenantIdAndConfigKeyAndEnvironmentAndIsActiveTrue(tenantId, configKey, environment);
            
            if (tenantConfig.isPresent()) {
                return decryptIfNeeded(tenantConfig.get());
            }
            
            // Fallback to environment-specific global config
            Optional<TenantConfiguration> envGlobalConfig = configRepository
                .findByConfigKeyAndEnvironmentAndIsActiveTrue(configKey, environment);
            
            if (envGlobalConfig.isPresent()) {
                return decryptIfNeeded(envGlobalConfig.get());
            }
            
            // Final fallback to global default config
            Optional<TenantConfiguration> globalConfig = configRepository
                .findByConfigKeyAndEnvironmentAndIsActiveTrue(configKey, "default");
            
            if (globalConfig.isPresent()) {
                return decryptIfNeeded(globalConfig.get());
            }
            
            logger.warn("Configuration not found for key: {} in tenant: {} environment: {}", 
                       configKey, tenantId, environment);
            return null;
            
        } catch (Exception e) {
            logger.error("Error retrieving configuration for key: {} in tenant: {}", configKey, tenantId, e);
            return null;
        }
    }
    
    /**
     * Get configuration with type conversion
     */
    public <T> T getConfiguration(String tenantId, String configKey, String environment, Class<T> type) {
        String value = getConfiguration(tenantId, configKey, environment);
        if (value == null) return null;
        
        try {
            if (type == String.class) {
                return type.cast(value);
            } else if (type == Integer.class) {
                return type.cast(Integer.parseInt(value));
            } else if (type == Boolean.class) {
                return type.cast(Boolean.parseBoolean(value));
            } else if (type == Long.class) {
                return type.cast(Long.parseLong(value));
            } else if (type == Double.class) {
                return type.cast(Double.parseDouble(value));
            } else {
                // Try JSON deserialization
                return objectMapper.readValue(value, type);
            }
        } catch (Exception e) {
            logger.error("Error converting configuration value: {} to type: {}", value, type, e);
            return null;
        }
        return null;
    }
    
    /**
     * Get boolean configuration (feature flags)
     */
    public boolean getFeatureFlag(String tenantId, String featureKey, String environment) {
        Boolean value = getConfiguration(tenantId, featureKey, environment, Boolean.class);
        return value != null && value;
    }
    
    /**
     * Set configuration value with versioning and audit
     */
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public TenantConfiguration setConfiguration(String tenantId, String configKey, String configValue, 
                                            TenantConfiguration.ConfigType configType, String environment, 
                                            String userId, Map<String, Object> metadata) {
        
        try {
            // Check if config already exists
            Optional<TenantConfiguration> existingConfig = configRepository
                .findByTenantIdAndConfigKeyAndEnvironmentAndIsActiveTrue(tenantId, configKey, environment);
            
            TenantConfiguration config;
            if (existingConfig.isPresent()) {
                // Deactivate current version
                TenantConfiguration current = existingConfig.get();
                current.setIsActive(false);
                configRepository.save(current);
                
                // Create new version
                config = new TenantConfiguration(tenantId, configKey, configValue, configType);
                config.setVersion(current.getVersion() + 1);
                config.setEnvironment(environment);
                config.setCreatedBy(userId);
                config.setMetadata(metadata != null ? objectMapper.writeValueAsString(metadata) : null);
            } else {
                // Create new configuration
                config = new TenantConfiguration(tenantId, configKey, configValue, configType);
                config.setEnvironment(environment);
                config.setCreatedBy(userId);
                config.setMetadata(metadata != null ? objectMapper.writeValueAsString(metadata) : null);
            }
            
            // Encrypt if needed
            if (configType == TenantConfiguration.ConfigType.ENCRYPTED) {
                config.setConfigValue(encrypt(configValue));
                config.setIsEncrypted(true);
            }
            
            // Clear cache
            configCache.remove(tenantId + "_" + configKey + "_" + environment);
            
            TenantConfiguration saved = configRepository.save(config);
            
            logger.info("Configuration updated for tenant: {} key: {} by user: {}", 
                       tenantId, configKey, userId);
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error setting configuration for tenant: {} key: {}", tenantId, configKey, e);
            throw new RuntimeException("Failed to set configuration", e);
        }
    }
    
    /**
     * Bulk update configurations
     */
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public List<TenantConfiguration> bulkUpdateConfigurations(String tenantId, 
                                                            Map<String, Object> configs, 
                                                            String environment, 
                                                            String userId) {
        List<TenantConfiguration> results = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            TenantConfiguration.ConfigType configType = determineConfigType(value);
            String stringValue = value != null ? value.toString() : null;
            
            TenantConfiguration config = setConfiguration(tenantId, key, stringValue, 
                                                       configType, environment, userId, null);
            results.add(config);
        }
        
        return results;
    }
    
    /**
     * Rollback configuration to previous version
     */
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public TenantConfiguration rollbackConfiguration(String tenantId, String configKey, 
                                                  String environment, String userId) {
        List<TenantConfiguration> versions = configRepository
            .findAllVersions(tenantId, configKey, environment);
        
        if (versions.size() < 2) {
            throw new RuntimeException("No previous version available for rollback");
        }
        
        // Get previous version (index 1 since 0 is current)
        TenantConfiguration previousVersion = versions.get(1);
        
        // Create new version with previous value
        TenantConfiguration rollback = new TenantConfiguration(tenantId, configKey, 
                                                            previousVersion.getConfigValue(), 
                                                            previousVersion.getConfigType());
        rollback.setEnvironment(environment);
        rollback.setVersion(versions.get(0).getVersion() + 1);
        rollback.setCreatedBy(userId);
        rollback.setMetadata("{\"action\": \"rollback\", \"fromVersion\": " + 
                           versions.get(0).getVersion() + "}");
        
        // Deactivate current version
        TenantConfiguration current = versions.get(0);
        current.setIsActive(false);
        configRepository.save(current);
        
        // Clear cache
        configCache.remove(tenantId + "_" + configKey + "_" + environment);
        
        TenantConfiguration saved = configRepository.save(rollback);
        
        logger.info("Configuration rolled back for tenant: {} key: {} to version: {} by user: {}", 
                   tenantId, configKey, previousVersion.getVersion(), userId);
        
        return saved;
    }
    
    /**
     * Get all configurations for a tenant
     */
    public Map<String, Object> getAllConfigurations(String tenantId, String environment) {
        List<TenantConfiguration> configs = configRepository
            .findByTenantIdAndEnvironmentAndIsActiveTrue(tenantId, environment);
        
        Map<String, Object> result = new HashMap<>();
        for (TenantConfiguration config : configs) {
            result.put(config.getConfigKey(), decryptIfNeeded(config));
        }
        
        return result;
    }
    
    /**
     * Get configuration audit trail
     */
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public List<TenantConfiguration> getConfigurationHistory(String tenantId, String configKey, String environment) {
        return configRepository.findAllVersions(tenantId, configKey, environment);
    }
    
    /**
     * Delete configuration (soft delete)
     */
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public void deleteConfiguration(String tenantId, String configKey, String environment, String userId) {
        Optional<TenantConfiguration> config = configRepository
            .findByTenantIdAndConfigKeyAndEnvironmentAndIsActiveTrue(tenantId, configKey, environment);
        
        if (config.isPresent()) {
            TenantConfiguration toDelete = config.get();
            toDelete.setIsActive(false);
            toDelete.setUpdatedBy(userId);
            toDelete.setUpdatedAt(LocalDateTime.now());
            
            configRepository.save(toDelete);
            
            // Clear cache
            configCache.remove(tenantId + "_" + configKey + "_" + environment);
            
            logger.info("Configuration deleted for tenant: {} key: {} by user: {}", 
                       tenantId, configKey, userId);
        }
    }
    
    /**
     * Refresh configuration cache
     */
    public void refreshCache() {
        configCache.clear();
        logger.info("Configuration cache refreshed");
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStats(String tenantId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConfigs", configRepository.countByTenantIdAndIsActiveTrue(tenantId));
        stats.put("featureFlags", configRepository
            .findByTenantIdAndConfigTypeAndIsActiveTrue(tenantId, TenantConfiguration.ConfigType.FEATURE_FLAG).size());
        stats.put("encryptedConfigs", configRepository
            .findByTenantIdAndConfigTypeAndIsActiveTrue(tenantId, TenantConfiguration.ConfigType.ENCRYPTED).size());
        
        return stats;
    }
    
    // Private helper methods
    
    private String decryptIfNeeded(TenantConfiguration config) {
        if (config.getIsEncrypted() && config.getConfigValue() != null) {
            try {
                return decrypt(config.getConfigValue());
            } catch (Exception e) {
                logger.error("Error decrypting configuration value", e);
                return null;
            }
        }
        return config.getConfigValue();
    }
    
    private String encrypt(String value) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    private String decrypt(String encryptedValue) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decrypted);
    }
    
    private TenantConfiguration.ConfigType determineConfigType(Object value) {
        if (value == null) return TenantConfiguration.ConfigType.STRING;
        
        if (value instanceof Boolean) return TenantConfiguration.ConfigType.BOOLEAN;
        if (value instanceof Integer || value instanceof Long) return TenantConfiguration.ConfigType.INTEGER;
        if (value instanceof Double || value instanceof Float) return TenantConfiguration.ConfigType.STRING;
        if (value instanceof Map || value instanceof List) return TenantConfiguration.ConfigType.JSON;
        
        return TenantConfiguration.ConfigType.STRING;
    }
}
