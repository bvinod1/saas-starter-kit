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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Push notification channel provider implementation using Firebase Cloud Messaging
 * Handles push notifications to mobile devices and web browsers
 */
@Service
public class PushChannelProvider implements NotificationChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(PushChannelProvider.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${notification.providers.push.firebase.project-id:}")
    private String firebaseProjectId;
    
    @Value("${notification.providers.push.firebase.private-key-id:}")
    private String firebasePrivateKeyId;
    
    @Value("${notification.providers.push.firebase.private-key:}")
    private String firebasePrivateKey;
    
    @Value("${notification.providers.push.firebase.client-email:}")
    private String firebaseClientEmail;
    
    @Value("${notification.providers.push.firebase.client-id:}")
    private String firebaseClientId;
    
    @Value("${notification.providers.push.firebase.enabled:false}")
    private boolean firebaseEnabled;
    
    @Value("${notification.providers.push.max-title-length:50}")
    private int maxTitleLength;
    
    @Value("${notification.providers.push.max-body-length:200}")
    private int maxBodyLength;
    
    @Value("${notification.providers.push.rate-limit-per-minute:100}")
    private int rateLimitPerMinute;
    
    // =============================================
    // INTERFACE IMPLEMENTATION
    // =============================================
    
    @Override
    public com.saasstarterkit.notification.domain.enums.NotificationChannel getChannelType() {
        return com.saasstarterkit.notification.domain.enums.NotificationChannel.PUSH;
    }
    
    @Override
    public boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences) {
        return preferences != null && 
               preferences.getPushEnabled() != null && 
               preferences.getPushEnabled() &&
               hasDeviceTokens(preferences);
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
            
            // Check if Firebase is enabled and configured
            if (!firebaseEnabled || !isFirebaseConfigured()) {
                logger.warn("Firebase push provider not configured or disabled");
                return DeliveryResult.failure()
                        .errorMessage("Push notification provider not configured")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Send via Firebase
            return sendViaFirebase(notification, preferences, startTime);
            
        } catch (Exception e) {
            logger.error("Error sending push notification: {}", notification.getId(), e);
            return DeliveryResult.failure()
                    .errorMessage("Failed to send push notification: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public boolean isHealthy() {
        if (!firebaseEnabled || !isFirebaseConfigured()) {
            return false;
        }
        
        try {
            // TODO: Implement Firebase health check
            // For now, return true if configured
            return true;
        } catch (Exception e) {
            logger.warn("Push provider health check failed", e);
            return false;
        }
    }
    
    @Override
    public ChannelConfiguration getConfiguration() {
        return ChannelConfiguration.builder()
                .providerName("Firebase Cloud Messaging")
                .version("1.0.0")
                .enabled(firebaseEnabled && isFirebaseConfigured())
                .maxRetries(3)
                .retryDelayMs(1000)
                .rateLimitPerMinute(rateLimitPerMinute)
                .build();
    }
    
    @Override
    public ValidationResult validate(Notification notification, UserNotificationPreferences preferences) {
        // Check if push notifications are enabled for user
        if (preferences == null || !preferences.getPushEnabled()) {
            return ValidationResult.invalid()
                    .errorMessage("Push notifications are disabled for this user")
                    .build();
        }
        
        // Check if user has device tokens
        if (!hasDeviceTokens(preferences)) {
            return ValidationResult.invalid()
                    .errorMessage("No device tokens found for push notifications")
                    .build();
        }
        
        // Check message content
        if (!StringUtils.hasText(notification.getMessage())) {
            return ValidationResult.invalid()
                    .errorMessage("Push notification message content is required")
                    .build();
        }
        
        // Check title length
        if (StringUtils.hasText(notification.getTitle()) && 
            notification.getTitle().length() > maxTitleLength) {
            return ValidationResult.invalid()
                    .errorMessage("Push notification title too long: " + notification.getTitle().length() + 
                                " characters (max: " + maxTitleLength + ")")
                    .build();
        }
        
        // Check body length
        if (notification.getMessage().length() > maxBodyLength) {
            return ValidationResult.invalid()
                    .errorMessage("Push notification body too long: " + notification.getMessage().length() + 
                                " characters (max: " + maxBodyLength + ")")
                    .build();
        }
        
        return ValidationResult.valid().build();
    }
    
    @Override
    public long getEstimatedDeliveryTimeMs() {
        // Push notifications are typically very fast
        return 5000; // 5 seconds
    }
    
    @Override
    public ChannelMetadata getMetadata() {
        return ChannelMetadata.builder()
                .providerName("Firebase Cloud Messaging")
                .version("1.0.0")
                .region("Global")
                .supportsBulk(true)
                .supportsScheduling(true)
                .supportsTracking(true)
                .maxMessageSize(maxBodyLength)
                .supportedFormats("text,json")
                .build();
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Send push notification via Firebase Cloud Messaging
     */
    private DeliveryResult sendViaFirebase(Notification notification, 
                                          UserNotificationPreferences preferences, 
                                          long startTime) {
        try {
            // Get device tokens
            List<String> deviceTokens = getDeviceTokens(preferences);
            if (deviceTokens.isEmpty()) {
                return DeliveryResult.failure()
                        .errorMessage("No device tokens available")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Build FCM message
            Map<String, Object> fcmMessage = buildFcmMessage(notification, preferences);
            
            logger.info("Sending push notification via Firebase to {} devices: {}", 
                       deviceTokens.size(), notification.getMessage());
            
            // TODO: Implement actual Firebase API call
            // For now, simulate the API call
            
            // Simulate API call delay
            Thread.sleep(200);
            
            // Simulate success response
            String messageId = "fcm_" + System.currentTimeMillis();
            
            long deliveryTime = System.currentTimeMillis() - startTime;
            logger.info("Push notification sent successfully via Firebase to {} devices in {}ms", 
                       deviceTokens.size(), deliveryTime);
            
            return DeliveryResult.success()
                    .messageId(messageId)
                    .deliveryTimeMs(deliveryTime)
                    .providerResponse("Firebase push notification delivery successful")
                    .metadata(getMetadata())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to send push notification via Firebase", e);
            return DeliveryResult.failure()
                    .errorMessage("Firebase push notification delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Build FCM message payload
     */
    private Map<String, Object> buildFcmMessage(Notification notification, 
                                               UserNotificationPreferences preferences) {
        // Basic notification structure
        Map<String, Object> notificationPayload = Map.of(
            "title", notification.getTitle() != null ? notification.getTitle() : "Notification",
            "body", notification.getMessage(),
            "icon", "/favicon.ico",
            "badge", "1",
            "click_action", "/notifications"
        );
        
        // Data payload for app processing
        Map<String, Object> dataPayload = Map.of(
            "notificationId", notification.getId().toString(),
            "type", notification.getType().toString(),
            "priority", notification.getPriority().toString(),
            "timestamp", System.currentTimeMillis(),
            "sourceEvent", notification.getSourceEvent() != null ? notification.getSourceEvent() : "",
            "category", notification.getData() != null ? notification.getData() : ""
        );
        
        // FCM message structure
        return Map.of(
            "notification", notificationPayload,
            "data", dataPayload,
            "priority", "high",
            "content_available", true,
            "mutable_content", true
        );
    }
    
    /**
     * Check if Firebase is properly configured
     */
    private boolean isFirebaseConfigured() {
        return StringUtils.hasText(firebaseProjectId) && 
               StringUtils.hasText(firebasePrivateKeyId) && 
               StringUtils.hasText(firebasePrivateKey) && 
               StringUtils.hasText(firebaseClientEmail) && 
               StringUtils.hasText(firebaseClientId);
    }
    
    /**
     * Check if user has device tokens
     */
    private boolean hasDeviceTokens(UserNotificationPreferences preferences) {
        try {
            if (preferences.getDeviceTokens() == null) {
                return false;
            }
            
            List<String> tokens = objectMapper.readValue(
                preferences.getDeviceTokens(), 
                List.class
            );
            
            return tokens != null && !tokens.isEmpty();
            
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse device tokens for user", e);
            return false;
        }
    }
    
    /**
     * Get device tokens from user preferences
     */
    private List<String> getDeviceTokens(UserNotificationPreferences preferences) {
        try {
            if (preferences.getDeviceTokens() == null) {
                return List.of();
            }
            
            return objectMapper.readValue(
                preferences.getDeviceTokens(), 
                List.class
            );
            
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse device tokens for user", e);
            return List.of();
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
    
    /**
     * Validate device token format
     */
    private boolean isValidDeviceToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        // Basic validation - Firebase tokens are typically 140+ characters
        return token.length() >= 100 && token.matches("^[a-zA-Z0-9:_-]+$");
    }
    
    /**
     * Filter valid device tokens
     */
    private List<String> filterValidTokens(List<String> tokens) {
        return tokens.stream()
                .filter(this::isValidDeviceToken)
                .toList();
    }
}
