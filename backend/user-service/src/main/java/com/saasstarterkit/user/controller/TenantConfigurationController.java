package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.entity.TenantConfiguration;
import com.saasstarterkit.user.service.TenantConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant-config")
@CrossOrigin(origins = "*")
public class TenantConfigurationController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantConfigurationController.class);
    
    @Autowired
    private TenantConfigurationService configService;
    
    /**
     * Get configuration value for a specific key
     */
    @GetMapping("/{tenantId}/{configKey}")
    public ResponseEntity<Object> getConfiguration(
            @PathVariable String tenantId,
            @PathVariable String configKey,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            String value = configService.getConfiguration(tenantId, configKey, environment);
            if (value != null) {
                return ResponseEntity.ok(Map.of("value", value, "key", configKey, "tenantId", tenantId));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving configuration for tenant: {} key: {}", tenantId, configKey, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to retrieve configuration"));
        }
    }
    
    /**
     * Get all configurations for a tenant
     */
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllConfigurations(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            Map<String, Object> configs = configService.getAllConfigurations(tenantId, environment);
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            logger.error("Error retrieving all configurations for tenant: {}", tenantId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to retrieve configurations"));
        }
    }
    
    /**
     * Set configuration value
     */
    @PostMapping("/{tenantId}/{configKey}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<TenantConfiguration> setConfiguration(
            @PathVariable String tenantId,
            @PathVariable String configKey,
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            String configValue = (String) request.get("value");
            String configType = (String) request.get("type");
            String userId = (String) request.get("userId");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
            
            if (configValue == null || userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            TenantConfiguration.ConfigType type = TenantConfiguration.ConfigType.STRING;
            if (configType != null) {
                try {
                    type = TenantConfiguration.ConfigType.valueOf(configType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid config type: {}, using STRING", configType);
                }
            }
            
            TenantConfiguration config = configService.setConfiguration(
                tenantId, configKey, configValue, type, environment, userId, metadata);
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Error setting configuration for tenant: {} key: {}", tenantId, configKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Bulk update configurations
     */
    @PostMapping("/{tenantId}/bulk")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<TenantConfiguration>> bulkUpdateConfigurations(
            @PathVariable String tenantId,
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configs = (Map<String, Object>) request.get("configs");
            String userId = (String) request.get("userId");
            
            if (configs == null || userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<TenantConfiguration> results = configService.bulkUpdateConfigurations(
                tenantId, configs, environment, userId);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error bulk updating configurations for tenant: {}", tenantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Rollback configuration to previous version
     */
    @PostMapping("/{tenantId}/{configKey}/rollback")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<TenantConfiguration> rollbackConfiguration(
            @PathVariable String tenantId,
            @PathVariable String configKey,
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            String userId = (String) request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            TenantConfiguration config = configService.rollbackConfiguration(
                tenantId, configKey, environment, userId);
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Error rolling back configuration for tenant: {} key: {}", tenantId, configKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get configuration history/audit trail
     */
    @GetMapping("/{tenantId}/{configKey}/history")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<TenantConfiguration>> getConfigurationHistory(
            @PathVariable String tenantId,
            @PathVariable String configKey,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            List<TenantConfiguration> history = configService.getConfigurationHistory(
                tenantId, configKey, environment);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving configuration history for tenant: {} key: {}", tenantId, configKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete configuration
     */
    @DeleteMapping("/{tenantId}/{configKey}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Void> deleteConfiguration(
            @PathVariable String tenantId,
            @PathVariable String configKey,
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            String userId = (String) request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            configService.deleteConfiguration(tenantId, configKey, environment, userId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error deleting configuration for tenant: {} key: {}", tenantId, configKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get configuration statistics
     */
    @GetMapping("/{tenantId}/stats")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfigurationStats(@PathVariable String tenantId) {
        try {
            Map<String, Object> stats = configService.getConfigurationStats(tenantId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving configuration stats for tenant: {}", tenantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Refresh configuration cache
     */
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> refreshCache() {
        try {
            configService.refreshCache();
            return ResponseEntity.ok(Map.of("message", "Configuration cache refreshed successfully"));
        } catch (Exception e) {
            logger.error("Error refreshing configuration cache", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to refresh cache"));
        }
    }
    
    /**
     * Get feature flag status
     */
    @GetMapping("/{tenantId}/feature/{featureKey}")
    public ResponseEntity<Map<String, Object>> getFeatureFlag(
            @PathVariable String tenantId,
            @PathVariable String featureKey,
            @RequestParam(defaultValue = "default") String environment) {
        
        try {
            boolean enabled = configService.getFeatureFlag(tenantId, featureKey, environment);
            return ResponseEntity.ok(Map.of(
                "feature", featureKey,
                "enabled", enabled,
                "tenantId", tenantId,
                "environment", environment
            ));
        } catch (Exception e) {
            logger.error("Error retrieving feature flag for tenant: {} feature: {}", tenantId, featureKey, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to retrieve feature flag"));
        }
    }
}
