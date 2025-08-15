package com.saasstarterkit.notification.service;

import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.repository.NotificationRepository;
import com.saasstarterkit.notification.repository.NotificationTemplateRepository;
import com.saasstarterkit.notification.repository.UserNotificationPreferencesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check service for notification system
 * Monitors the health of notification repositories and overall system status
 */
@Service
public class NotificationHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationHealthService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    @Autowired
    private UserNotificationPreferencesRepository preferencesRepository;
    
    // Health check configuration
    private static final int MAX_FAILED_NOTIFICATIONS = 100;
    private static final int MAX_PENDING_NOTIFICATIONS = 1000;
    
    // =============================================
    // PUBLIC METHODS
    // =============================================
    
    /**
     * Get detailed health status
     */
    public Map<String, Object> getDetailedHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Repository health
            healthStatus.put("repositories", getRepositoriesHealth());
            
            // Notification queue health
            healthStatus.put("notification_queue", getNotificationQueueHealth());
            
            // Database health
            healthStatus.put("database", getDatabaseHealth());
            
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
            Map<String, Object> repositoriesHealth = getRepositoriesHealth();
            Map<String, Object> queueHealth = getNotificationQueueHealth();
            Map<String, Object> databaseHealth = getDatabaseHealth();
            
            return isSystemHealthy(repositoriesHealth, queueHealth, databaseHealth);
            
        } catch (Exception e) {
            logger.error("System health check failed", e);
            return false;
        }
    }
    
    /**
     * Get simple health status
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean healthy = isSystemHealthy();
            health.put("status", healthy ? "UP" : "DOWN");
            health.put("healthy", healthy);
            health.put("timestamp", LocalDateTime.now().toString());
            
            if (healthy) {
                health.put("message", "Notification service is healthy");
            } else {
                health.put("message", "Notification service has issues");
            }
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("healthy", false);
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now().toString());
        }
        
        return health;
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Check repositories health
     */
    private Map<String, Object> getRepositoriesHealth() {
        Map<String, Object> repositoriesHealth = new HashMap<>();
        
        try {
            // Check notification repository
            long notificationCount = notificationRepository.count();
            repositoriesHealth.put("notification_repository", true);
            repositoriesHealth.put("notification_count", notificationCount);
            
            // Check template repository
            long templateCount = templateRepository.count();
            repositoriesHealth.put("template_repository", true);
            repositoriesHealth.put("template_count", templateCount);
            
            // Check preferences repository
            long preferencesCount = preferencesRepository.count();
            repositoriesHealth.put("preferences_repository", true);
            repositoriesHealth.put("preferences_count", preferencesCount);
            
            repositoriesHealth.put("overall_healthy", true);
            
        } catch (Exception e) {
            logger.error("Repository health check failed", e);
            repositoriesHealth.put("overall_healthy", false);
            repositoriesHealth.put("error", e.getMessage());
        }
        
        return repositoriesHealth;
    }
    
    /**
     * Check notification queue health
     */
    private Map<String, Object> getNotificationQueueHealth() {
        Map<String, Object> queueHealth = new HashMap<>();
        
        try {
            // Count notifications by status
            long pendingCount = notificationRepository.findByStatus("PENDING").size();
            long failedCount = notificationRepository.findByStatus("FAILED").size();
            long sentCount = notificationRepository.findByStatus("SENT").size();
            
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
    private Map<String, Object> getDatabaseHealth() {
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
    private boolean isSystemHealthy(Map<String, Object> repositoriesHealth,
                                   Map<String, Object> queueHealth,
                                   Map<String, Object> databaseHealth) {
        
        // Check repositories health
        boolean isRepositoriesHealthy = (Boolean) repositoriesHealth.getOrDefault("overall_healthy", false);
        
        // Check queue health
        boolean isQueueHealthy = (Boolean) queueHealth.getOrDefault("overall_healthy", false);
        
        // Check database health
        boolean isDatabaseHealthy = (Boolean) databaseHealth.getOrDefault("healthy", false);
        
        return isRepositoriesHealthy && isQueueHealthy && isDatabaseHealthy;
    }
    
    /**
     * Determine overall system status
     */
    private String determineOverallStatus(Map<String, Object> healthStatus) {
        try {
            // Check repositories
            Map<String, Object> repositoriesHealth = (Map<String, Object>) healthStatus.get("repositories");
            boolean repositoriesHealthy = (Boolean) repositoriesHealth.getOrDefault("overall_healthy", false);
            
            // Check queue
            Map<String, Object> queueHealth = (Map<String, Object>) healthStatus.get("notification_queue");
            boolean queueHealthy = (Boolean) queueHealth.getOrDefault("overall_healthy", false);
            
            // Check database
            Map<String, Object> databaseHealth = (Map<String, Object>) healthStatus.get("database");
            boolean databaseHealthy = (Boolean) databaseHealth.getOrDefault("healthy", false);
            
            if (repositoriesHealthy && queueHealthy && databaseHealthy) {
                return "HEALTHY";
            } else if (repositoriesHealthy || queueHealthy || databaseHealthy) {
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
