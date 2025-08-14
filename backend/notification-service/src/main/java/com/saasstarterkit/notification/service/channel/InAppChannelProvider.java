package com.saasstarterkit.notification.service.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * In-App notification channel provider
 * Handles real-time web notifications using WebSocket/STOMP
 */
@Service
public class InAppChannelProvider implements NotificationChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(InAppChannelProvider.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${notification.providers.in-app.enabled:true}")
    private boolean inAppEnabled;
    
    @Value("${notification.providers.in-app.max-title-length:100}")
    private int maxTitleLength;
    
    @Value("${notification.providers.in-app.max-body-length:500}")
    private int maxBodyLength;
    
    @Value("${notification.providers.in-app.rate-limit-per-minute:200}")
    private int rateLimitPerMinute;
    
    @Value("${notification.providers.in-app.websocket.destination-prefix:/user}")
    private String websocketDestinationPrefix;
    
    // =============================================
    // INTERFACE IMPLEMENTATION
    // =============================================
    
    @Override
    public Notification.NotificationChannel getChannelType() {
        return Notification.NotificationChannel.IN_APP;
    }
    
    @Override
    public boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences) {
        return preferences != null && 
               preferences.getInAppEnabled() != null && 
               preferences.getInAppEnabled();
    }
    
    @Override
    public DeliveryResult send(Notification notification, UserNotificationPreferences preferences) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate the notification
            ValidationResult validation = validate(notification, preferences);
            if (!validation.isValid()) {
                return DeliveryResult.failure()
                        .errorMessage(validation.getErrorMessage())
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Check if in-app notifications are enabled
            if (!inAppEnabled) {
                logger.warn("In-app notification provider is disabled");
                return DeliveryResult.failure()
                        .errorMessage("In-app notifications are disabled")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Send in-app notification
            return sendInAppNotification(notification, preferences, startTime);
            
        } catch (Exception e) {
            logger.error("Error sending in-app notification: {}", notification.getId(), e);
            return DeliveryResult.failure()
                    .errorMessage("Failed to send in-app notification: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Check if WebSocket messaging template is available
            return messagingTemplate != null && inAppEnabled;
        } catch (Exception e) {
            logger.warn("In-app provider health check failed", e);
            return false;
        }
    }
    
    @Override
    public ChannelConfiguration getConfiguration() {
        return ChannelConfiguration.builder()
                .providerName("In-App WebSocket")
                .version("1.0.0")
                .enabled(inAppEnabled)
                .maxRetries(1) // In-app notifications are not retried
                .retryDelayMs(0)
                .rateLimitPerMinute(rateLimitPerMinute)
                .build();
    }
    
    @Override
    public ValidationResult validate(Notification notification, UserNotificationPreferences preferences) {
        // Check if in-app notifications are enabled for user
        if (preferences == null || !preferences.getInAppEnabled()) {
            return ValidationResult.invalid()
                    .errorMessage("In-app notifications are disabled for this user")
                    .build();
        }
        
        // Check message content
        if (!StringUtils.hasText(notification.getMessage())) {
            return ValidationResult.invalid()
                    .errorMessage("In-app notification message content is required")
                    .build();
        }
        
        // Check title length
        if (StringUtils.hasText(notification.getTitle()) && 
            notification.getTitle().length() > maxTitleLength) {
            return ValidationResult.invalid()
                    .errorMessage("In-app notification title too long: " + notification.getTitle().length() + 
                                " characters (max: " + maxTitleLength + ")")
                    .build();
        }
        
        // Check body length
        if (notification.getMessage().length() > maxBodyLength) {
            return ValidationResult.invalid()
                    .errorMessage("In-app notification body too long: " + notification.getMessage().length() + 
                                " characters (max: " + maxBodyLength + ")")
                    .build();
        }
        
        return ValidationResult.valid().build();
    }
    
    @Override
    public long getEstimatedDeliveryTimeMs() {
        // In-app notifications are instant
        return 100; // 100ms
    }
    
    @Override
    public ChannelMetadata getMetadata() {
        return ChannelMetadata.builder()
                .providerName("In-App WebSocket")
                .version("1.0.0")
                .region("Local")
                .supportsBulk(false)
                .supportsScheduling(false)
                .supportsTracking(true)
                .maxMessageSize(maxBodyLength)
                .supportedFormats("text,json")
                .build();
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Send in-app notification via WebSocket
     */
    private DeliveryResult sendInAppNotification(Notification notification, 
                                               UserNotificationPreferences preferences, 
                                               long startTime) {
        try {
            // Build notification payload
            Map<String, Object> notificationPayload = buildNotificationPayload(notification, preferences);
            
            // Determine destination
            String destination = buildWebSocketDestination(notification);
            
            logger.info("Sending in-app notification to {}: {}", 
                       destination, notification.getMessage());
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                notification.getUserId(),
                destination,
                notificationPayload
            );
            
            long deliveryTime = System.currentTimeMillis() - startTime;
            logger.info("In-app notification sent successfully to {} in {}ms", 
                       destination, deliveryTime);
            
            return DeliveryResult.success()
                    .messageId("inapp_" + System.currentTimeMillis())
                    .deliveryTimeMs(deliveryTime)
                    .providerResponse("In-app notification delivered via WebSocket")
                    .metadata(getMetadata())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to send in-app notification", e);
            return DeliveryResult.failure()
                    .errorMessage("In-app notification delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Build notification payload for WebSocket
     */
    private Map<String, Object> buildNotificationPayload(Notification notification, 
                                                        UserNotificationPreferences preferences) {
        return Map.of(
            "id", notification.getId(),
            "type", "IN_APP",
            "title", notification.getTitle() != null ? notification.getTitle() : "Notification",
            "message", notification.getMessage(),
            "priority", notification.getPriority().toString(),
            "category", notification.getSourceEvent() != null ? notification.getSourceEvent() : "general",
            "timestamp", LocalDateTime.now().toString(),
            "read", false,
            "data", notification.getData() != null ? notification.getData() : "",
            "userId", notification.getUserId(),
            "tenantId", notification.getTenantId()
        );
    }
    
    /**
     * Build WebSocket destination path
     */
    private String buildWebSocketDestination(Notification notification) {
        StringBuilder destination = new StringBuilder("/notifications");
        
        // Add category if available
        if (StringUtils.hasText(notification.getSourceEvent())) {
            destination.append("/").append(notification.getSourceEvent());
        }
        
        // Add priority if high or urgent
        if (notification.getPriority() == Notification.NotificationPriority.HIGH || 
            notification.getPriority() == Notification.NotificationPriority.URGENT) {
            destination.append("/priority");
        }
        
        return destination.toString();
    }
    
    /**
     * Send notification to specific user
     */
    public void sendToUser(String userId, Map<String, Object> payload) {
        try {
            String destination = "/notifications";
            messagingTemplate.convertAndSendToUser(userId, destination, payload);
            logger.debug("In-app notification sent to user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send in-app notification to user: {}", userId, e);
        }
    }
    
    /**
     * Send notification to all users in a tenant
     */
    public void sendToTenant(String tenantId, Map<String, Object> payload) {
        try {
            String destination = "/tenant/" + tenantId + "/notifications";
            messagingTemplate.convertAndSend(destination, payload);
            logger.debug("In-app notification sent to tenant: {}", tenantId);
        } catch (Exception e) {
            logger.error("Failed to send in-app notification to tenant: {}", tenantId, e);
        }
    }
    
    /**
     * Send notification to all users in an organization
     */
    public void sendToOrganization(String organizationId, Map<String, Object> payload) {
        try {
            String destination = "/organization/" + organizationId + "/notifications";
            messagingTemplate.convertAndSend(destination, payload);
            logger.debug("In-app notification sent to organization: {}", organizationId);
        } catch (Exception e) {
            logger.error("Failed to send in-app notification to organization: {}", organizationId, e);
        }
    }
    
    /**
     * Broadcast notification to all connected users
     */
    public void broadcastToAll(Map<String, Object> payload) {
        try {
            String destination = "/broadcast/notifications";
            messagingTemplate.convertAndSend(destination, payload);
            logger.debug("In-app notification broadcasted to all users");
        } catch (Exception e) {
            logger.error("Failed to broadcast in-app notification", e);
        }
    }
    
    /**
     * Truncate title if it exceeds length limit
     */
    private String truncateTitle(String title) {
        if (title == null || title.length() <= maxTitleLength) {
            return title;
        }
        
        return title.substring(0, maxTitleLength - 3) + "...";
    }
    
    /**
     * Truncate body if it exceeds length limit
     */
    private String truncateBody(String body) {
        if (body == null || body.length() <= maxBodyLength) {
            return body;
        }
        
        return body.substring(0, maxBodyLength - 3) + "...";
    }
}
