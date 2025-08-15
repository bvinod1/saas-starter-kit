package com.saasstarterkit.user.service;

// import com.saasstarterkit.user.service.secrets.SecretManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing application configuration with hierarchical loading
 * and integration with secret management systems.
 */
@Service
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private Environment environment;
    
    @Autowired(required = false)
    private SecretManager secretManager;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    // Configuration cache
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();
    
    // Configuration sources priority (lowest to highest)
    private static final String[] CONFIG_SOURCES = {
        "common",           // Common properties file
        "environment",      // Environment-specific properties file
        "common_secrets",   // Common secrets from secret manager
        "env_secrets"       // Environment-specific secrets from secret manager
    };
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing ConfigurationService with profile: {}", activeProfile);
        loadConfiguration();
    }
    
    /**
     * Loads configuration from all sources in priority order.
     */
    private void loadConfiguration() {
        try {
            // Load from property files (already loaded by Spring)
            logger.info("Configuration loaded from property files");
            
            // Load from secret manager if available
            if (secretManager != null && secretManager.isHealthy()) {
                loadSecretsFromSecretManager();
            } else {
                logger.warn("Secret manager not available or unhealthy, skipping secret loading");
            }
            
            logger.info("Configuration loading completed");
            
        } catch (Exception e) {
            logger.error("Error loading configuration", e);
        }
    }
    
    /**
     * Loads secrets from the secret manager.
     */
    private void loadSecretsFromSecretManager() {
        try {
            // Load common secrets
            loadSecretsByNamespace("common");
            
            // Load environment-specific secrets
            loadSecretsByNamespace(activeProfile);
            
            logger.info("Secrets loaded from secret manager successfully");
            
        } catch (Exception e) {
            logger.error("Error loading secrets from secret manager", e);
        }
    }
    
    /**
     * Loads secrets for a specific namespace.
     */
    private void loadSecretsByNamespace(String namespace) {
        try {
            // This is a simplified approach - in practice, you'd use the secret manager's
            // namespace/path functionality to load secrets by category
            String[] secretKeys = secretManager.listSecretKeys();
            
            for (String secretKey : secretKeys) {
                if (secretKey.startsWith(namespace + "/")) {
                    Optional<String> secretValue = secretManager.getSecret(secretKey);
                    if (secretValue.isPresent()) {
                        String configKey = secretKey.substring(namespace.length() + 1);
                        configCache.put(configKey, secretValue.get());
                        logger.debug("Loaded secret: {} -> [REDACTED]", configKey);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error loading secrets for namespace: {}", namespace, e);
        }
    }
    
    /**
     * Gets a configuration value with hierarchical fallback.
     * 
     * @param key Configuration key
     * @return Optional containing the configuration value
     */
    public Optional<String> getConfiguration(String key) {
        // Check cache first
        if (configCache.containsKey(key)) {
            return Optional.of(String.valueOf(configCache.get(key)));
        }
        
        // Check environment properties
        String value = environment.getProperty(key);
        if (value != null) {
            return Optional.of(value);
        }
        
        // Check secret manager if available
        if (secretManager != null && secretManager.isHealthy()) {
            try {
                Optional<String> secretValue = secretManager.getSecret(key);
                if (secretValue.isPresent()) {
                    // Cache the secret value
                    configCache.put(key, secretValue.get());
                    return secretValue;
                }
            } catch (Exception e) {
                logger.warn("Error retrieving secret for key: {}", key, e);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Gets a configuration value with default fallback.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if configuration not found
     * @return Configuration value or default
     */
    public String getConfiguration(String key, String defaultValue) {
        return getConfiguration(key).orElse(defaultValue);
    }
    
    /**
     * Gets a configuration value with type conversion.
     * 
     * @param key Configuration key
     * @param type Target type
     * @param defaultValue Default value if configuration not found
     * @return Configuration value converted to target type
     */
    public <T> T getConfiguration(String key, Class<T> type, T defaultValue) {
        Optional<String> value = getConfiguration(key);
        if (value.isPresent()) {
            try {
                return convertValue(value.get(), type);
            } catch (Exception e) {
                logger.warn("Error converting configuration value for key: {} to type: {}", key, type, e);
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets a boolean configuration value.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if configuration not found
     * @return Boolean configuration value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return getConfiguration(key, Boolean.class, defaultValue);
    }
    
    /**
     * Gets an integer configuration value.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if configuration not found
     * @return Integer configuration value
     */
    public int getInt(String key, int defaultValue) {
        return getConfiguration(key, Integer.class, defaultValue);
    }
    
    /**
     * Gets a long configuration value.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if configuration not found
     * @return Long configuration value
     */
    public long getLong(String key, long defaultValue) {
        return getConfiguration(key, Long.class, defaultValue);
    }
    
    /**
     * Gets a double configuration value.
     * 
     * @param key Configuration key
     * @param defaultValue Default value if configuration not found
     * @return Double configuration value
     */
    public double getDouble(String key, double defaultValue) {
        return getConfiguration(key, Double.class, defaultValue);
    }
    
    /**
     * Gets all configuration values for a specific prefix.
     * 
     * @param prefix Configuration key prefix
     * @return Map of configuration keys to values
     */
    public Map<String, String> getConfigurationsByPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        
        // Get from environment properties
        for (String key : getEnvironmentPropertyKeys()) {
            if (key.startsWith(prefix)) {
                String value = environment.getProperty(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
        }
        
        // Get from cache
        for (Map.Entry<String, Object> entry : configCache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        return result;
    }
    
    /**
     * Refreshes the configuration cache.
     */
    public void refreshCache() {
        configCache.clear();
        logger.info("Configuration cache cleared");
        
        if (secretManager != null && secretManager.isHealthy()) {
            try {
                secretManager.refresh();
                loadSecretsFromSecretManager();
                logger.info("Configuration refreshed from secret manager");
            } catch (Exception e) {
                logger.error("Error refreshing configuration from secret manager", e);
            }
        }
    }
    
    /**
     * Gets the active profile.
     * 
     * @return Active Spring profile
     */
    public String getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Checks if a configuration key exists.
     * 
     * @param key Configuration key
     * @return true if the configuration exists
     */
    public boolean hasConfiguration(String key) {
        return getConfiguration(key).isPresent();
    }
    
    /**
     * Gets configuration statistics.
     * 
     * @return Map containing configuration statistics
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConfigs", configCache.size() + getEnvironmentPropertyKeys().length);
        stats.put("cachedConfigs", configCache.size());
        stats.put("environmentConfigs", getEnvironmentPropertyKeys().length);
        stats.put("activeProfile", activeProfile);
        stats.put("secretManagerAvailable", secretManager != null && secretManager.isHealthy());
        
        return stats;
    }
    
    // Private helper methods
    
    private String[] getEnvironmentPropertyKeys() {
        // This is a simplified approach - in practice, you'd need to implement
        // a way to get all property keys from the Environment
        return new String[0];
    }
    
    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> type) {
        if (type == String.class) {
            return (T) value;
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Float.class) {
            return (T) Float.valueOf(value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
