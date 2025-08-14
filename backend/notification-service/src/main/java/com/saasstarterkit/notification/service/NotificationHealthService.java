package com.saasstarterkit.notification.service;

import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.service.channel.NotificationChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Health check service for notification system
 * Monitors the health of all channel providers and overall system status
 */
@Service
public class NotificationHealthService implements HealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationHealthService.class);
    
    @Autowired
    private ChannelProviderRegistry channelProviderRegistry;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Health check configuration
    private static final int HEALTH_CHECK_TIMEOUT_SECONDS = 10;
    private static final int MAX_FAILED_NOTIFICATIONS = 100;
    private static final int MAX_PENDING_NOTIFICATIONS = 1000;
    
    // =============================================
    // HEALTH INDICATOR IMPLEMENTATION
    // =============================================
    
    @Override
    public Health health() {
        try {
            Map<String, Object> healthDetails = new HashMap<>();
            
            // Check channel providers health
            Map<Notification.NotificationChannel, Boolean> providerHealth = 
                channelProviderRegistry.getProviderHealthStatus();
            healthDetails.put("channel_providers", providerHealth);
            
            // Check notification queue health
            Map<String, Object> queueHealth = checkNotificationQueueHealth();
            healthDetails.put("notification_queue", queueHealth);
            
            // Check database connectivity
            Map<String, Object> databaseHealth = checkDatabaseHealth();
            healthDetails.put("database", databaseHealth);
            
            // Check overall system health
            boolean isHealthy = isSystemHealthy(providerHealth, queueHealth, databaseHealth);
            
            if (isHealthy) {
                return Health.up()
                        .withDetails(healthDetails)
                        .build();
            } else {
                return Health.down()
                        .withDetails(healthDetails)
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
    
    // =============================================
    // PUBLIC METHODS
    // =============================================
    
    /**
     * Get detailed health status
     */
    public Map<String, Object> getDetailedHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Channel providers health
            healthStatus.put("channel_providers", getChannelProvidersHealth());
            
            // Notification queue health
            healthStatus.put("notification_queue", getNotificationQueueHealth());
            
            // Database health
            healthStatus.put("database", getDatabaseHealth());
            
            // System metrics
            healthStatus.put("system_metrics", getSystemMetrics());
            
            // Overall status
            healthStatus.put("overall_status", determineOverallStatus(healthStatus));
            healthStatus.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            logger.error("Failed to get detailed health status", e);
            healthStatus.put("error", e.getMessage());
            healthStatus.put("overall_status", "ERROR");
        }
        
        return healthStatus;
    }
    
    /**
     * Check if the system is healthy
     */
    public boolean isSystemHealthy() {
        try {
            Map<Notification.NotificationChannel, Boolean> providerHealth = 
                channelProviderRegistry.getProviderHealthStatus();
            
            Map<String, Object> queueHealth = checkNotificationQueueHealth();
            Map<String, Object> databaseHealth = checkDatabaseHealth();
            
            return isSystemHealthy(providerHealth, queueHealth, databaseHealth);
            
        } catch (Exception e) {
            logger.error("System health check failed", e);
            return false;
        }
    }
    
    /**
     * Get health status for specific channel
     */
    public Map<String, Object> getChannelHealth(Notification.NotificationChannel channel) {
        Map<String, Object> channelHealth = new HashMap<>();
        
        try {
            NotificationChannelProvider provider = channelProviderRegistry.getProvider(channel);
            
            if (provider == null) {
                channelHealth.put("status", "NOT_AVAILABLE");
                channelHealth.put("message", "Channel provider not found");
                return channelHealth;
            }
            
            boolean isHealthy = provider.isHealthy();
            channelHealth.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");
            channelHealth.put("healthy", isHealthy);
            channelHealth.put("provider_class", provider.getClass().getSimpleName());
            channelHealth.put("configuration", provider.getConfiguration());
            channelHealth.put("metadata", provider.getMetadata());
            channelHealth.put("estimated_delivery_time_ms", provider.getEstimatedDeliveryTimeMs());
            
        } catch (Exception e) {
            logger.error("Channel health check failed for: {}", channel, e);
            channelHealth.put("status", "ERROR");
            channelHealth.put("error", e.getMessage());
        }
        
        return channelHealth;
    }
    
    /**
     * Perform health check for all channels
     */
    public Map<Notification.NotificationChannel, Map<String, Object>> checkAllChannelsHealth() {
        Map<Notification.NotificationChannel, Map<String, Object>> channelsHealth = new HashMap<>();
        
        List<Notification.NotificationChannel> supportedChannels = 
            channelProviderRegistry.getSupportedChannels();
        
        for (Notification.NotificationChannel channel : supportedChannels) {
            channelsHealth.put(channel, getChannelHealth(channel));
        }
        
        return channelsHealth;
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Check notification queue health
     */
    private Map<String, Object> checkNotificationQueueHealth() {
        Map<String, Object> queueHealth = new HashMap<>();
        
        try {
            // Count notifications by status
            long pendingCount = notificationRepository.countByStatus(Notification.NotificationStatus.PENDING);
            long failedCount = notificationRepository.countByStatus(Notification.NotificationStatus.FAILED);
            long sentCount = notificationRepository.countByStatus(Notification.NotificationStatus.SENT);
            
            queueHealth.put("pending_count", pendingCount);
            queueHealth.put("failed_count", failedCount);
            queueHealth.put("sent_count", sentCount);
            
            // Check if counts are within acceptable limits
            boolean isPendingHealthy = pendingCount <= MAX_PENDING_NOTIFICATIONS;
            boolean isFailedHealthy = failedCount <= MAX_FAILED_NOTIFICATIONS;
            
            queueHealth.put("pending_healthy", isPendingHealthy);
            queueHealth.put("failed_healthy", isFailedHealthy);
            queueHealth.put("overall_healthy", isPendingHealthy && isFailedHealthy);
            
            // Add warnings if limits are exceeded
            if (!isPendingHealthy) {
                queueHealth.put("pending_warning", "Too many pending notifications: " + pendingCount);
            }
            if (!isFailedHealthy) {
                queueHealth.put("failed_warning", "Too many failed notifications: " + failedCount);
            }
            
        } catch (Exception e) {
            logger.error("Failed to check notification queue health", e);
            queueHealth.put("error", e.getMessage());
            queueHealth.put("overall_healthy", false);
        }
        
        return queueHealth;
    }
    
    /**
     * Check database health
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> databaseHealth = new HashMap<>();
        
        try {
            // Simple database connectivity test
            long startTime = System.currentTimeMillis();
            long notificationCount = notificationRepository.count();
            long queryTime = System.currentTimeMillis() - startTime;
            
            databaseHealth.put("connected", true);
            databaseHealth.put("total_notifications", notificationCount);
            databaseHealth.put("query_time_ms", queryTime);
            databaseHealth.put("healthy", queryTime < 1000); // Query should complete within 1 second
            
            if (queryTime >= 1000) {
                databaseHealth.put("warning", "Database query is slow: " + queryTime + "ms");
            }
            
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            databaseHealth.put("connected", false);
            databaseHealth.put("error", e.getMessage());
            databaseHealth.put("healthy", false);
        }
        
        return databaseHealth;
    }
    
    /**
     * Check if system is overall healthy
     */
    private boolean isSystemHealthy(Map<Notification.NotificationChannel, Boolean> providerHealth,
                                   Map<String, Object> queueHealth,
                                   Map<String, Object> databaseHealth) {
        
        // Check if at least one provider is healthy
        boolean hasHealthyProviders = providerHealth.values().stream().anyMatch(Boolean::booleanValue);
        
        // Check queue health
        boolean isQueueHealthy = (Boolean) queueHealth.getOrDefault("overall_healthy", false);
        
        // Check database health
        boolean isDatabaseHealthy = (Boolean) databaseHealth.getOrDefault("healthy", false);
        
        return hasHealthyProviders && isQueueHealthy && isDatabaseHealthy;
    }
    
    /**
     * Get channel providers health
     */
    private Map<String, Object> getChannelProvidersHealth() {
        Map<String, Object> providersHealth = new HashMap<>();
        
        try {
            Map<Notification.NotificationChannel, Boolean> healthStatus = 
                channelProviderRegistry.getProviderHealthStatus();
            
            providersHealth.put("status", healthStatus);
            
            long healthyCount = healthStatus.values().stream().filter(Boolean::booleanValue).count();
            long totalCount = healthStatus.size();
            
            providersHealth.put("healthy_count", healthyCount);
            providersHealth.put("total_count", totalCount);
            providersHealth.put("overall_healthy", healthyCount > 0);
            
            if (healthyCount == 0) {
                providersHealth.put("warning", "No healthy channel providers available");
            }
            
        } catch (Exception e) {
            logger.error("Failed to get channel providers health", e);
            providersHealth.put("error", e.getMessage());
            providersHealth.put("overall_healthy", false);
        }
        
        return providersHealth;
    }
    
    /**
     * Get notification queue health
     */
    private Map<String, Object> getNotificationQueueHealth() {
        return checkNotificationQueueHealth();
    }
    
    /**
     * Get database health
     */
    private Map<String, Object> getDatabaseHealth() {
        return checkDatabaseHealth();
    }
    
    /**
     * Get system metrics
     */
    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get provider configurations
            Map<Notification.NotificationChannel, NotificationChannelProvider.ChannelConfiguration> configs = 
                channelProviderRegistry.getAllConfigurations();
            
            metrics.put("total_providers", configs.size());
            metrics.put("enabled_providers", configs.values().stream()
                    .filter(NotificationChannelProvider.ChannelConfiguration::isEnabled)
                    .count());
            
            // Get provider metadata
            Map<Notification.NotificationChannel, NotificationChannelProvider.ChannelMetadata> metadata = 
                channelProviderRegistry.getAllMetadata();
            
            metrics.put("supported_formats", metadata.values().stream()
                    .map(NotificationChannelProvider.ChannelMetadata::getSupportedFormats)
                    .distinct()
                    .toList());
            
            metrics.put("total_regions", metadata.values().stream()
                    .map(NotificationChannelProvider.ChannelMetadata::getRegion)
                    .distinct()
                    .count());
            
        } catch (Exception e) {
            logger.error("Failed to get system metrics", e);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Determine overall system status
     */
    private String determineOverallStatus(Map<String, Object> healthStatus) {
        try {
            // Check channel providers
            Map<String, Object> providersHealth = (Map<String, Object>) healthStatus.get("channel_providers");
            boolean providersHealthy = (Boolean) providersHealth.getOrDefault("overall_healthy", false);
            
            // Check queue
            Map<String, Object> queueHealth = (Map<String, Object>) healthStatus.get("notification_queue");
            boolean queueHealthy = (Boolean) queueHealth.getOrDefault("overall_healthy", false);
            
            // Check database
            Map<String, Object> databaseHealth = (Map<String, Object>) healthStatus.get("database");
            boolean databaseHealthy = (Boolean) databaseHealth.getOrDefault("healthy", false);
            
            if (providersHealthy && queueHealthy && databaseHealthy) {
                return "HEALTHY";
            } else if (providersHealthy || queueHealthy || databaseHealthy) {
                return "DEGRADED";
            } else {
                return "UNHEALTHY";
            }
            
        } catch (Exception e) {
            logger.error("Failed to determine overall status", e);
            return "ERROR";
        }
    }
}
