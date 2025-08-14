package com.saasstarterkit.notification.service.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory service for creating and configuring notification channel providers
 * Supports dynamic provider creation and configuration updates
 */
@Service
public class ChannelProviderFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ChannelProviderFactory.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Cache for created providers
    private final Map<String, NotificationChannelProvider> providerCache = new ConcurrentHashMap<>();
    
    // Configuration properties
    @Value("${notification.providers.email.smtp.enabled:true}")
    private boolean smtpEnabled;
    
    @Value("${notification.providers.email.sendgrid.enabled:false}")
    private boolean sendgridEnabled;
    
    @Value("${notification.providers.sms.twilio.enabled:false}")
    private boolean twilioEnabled;
    
    @Value("${notification.providers.push.firebase.enabled:false}")
    private boolean firebaseEnabled;
    
    @Value("${notification.providers.in-app.enabled:true}")
    private boolean inAppEnabled;
    
    @Value("${notification.providers.webhook.enabled:false}")
    private boolean webhookEnabled;
    
    // =============================================
    // PUBLIC METHODS
    // =============================================
    
    /**
     * Create a channel provider based on configuration
     */
    public NotificationChannelProvider createProvider(String providerType) {
        String cacheKey = providerType.toLowerCase();
        
        // Check cache first
        if (providerCache.containsKey(cacheKey)) {
            return providerCache.get(cacheKey);
        }
        
        // Create new provider
        NotificationChannelProvider provider = createNewProvider(providerType);
        if (provider != null) {
            providerCache.put(cacheKey, provider);
            logger.info("Created and cached channel provider: {}", providerType);
        }
        
        return provider;
    }
    
    /**
     * Get or create provider for a specific channel
     */
    public NotificationChannelProvider getOrCreateProvider(Notification.NotificationChannel channel) {
        String providerType = getProviderTypeForChannel(channel);
        return createProvider(providerType);
    }
    
    /**
     * Check if a provider type is enabled
     */
    public boolean isProviderEnabled(String providerType) {
        return switch (providerType.toLowerCase()) {
            case "email", "smtp" -> smtpEnabled;
            case "sendgrid" -> sendgridEnabled;
            case "sms", "twilio" -> twilioEnabled;
            case "push", "firebase" -> firebaseEnabled;
            case "in-app", "inapp" -> inAppEnabled;
            case "webhook" -> webhookEnabled;
            default -> false;
        };
    }
    
    /**
     * Get all enabled provider types
     */
    public Map<String, Boolean> getEnabledProviders() {
        return Map.of(
            "email", smtpEnabled,
            "sendgrid", sendgridEnabled,
            "sms", twilioEnabled,
            "push", firebaseEnabled,
            "in-app", inAppEnabled,
            "webhook", webhookEnabled
        );
    }
    
    /**
     * Clear provider cache
     */
    public void clearCache() {
        providerCache.clear();
        logger.info("Channel provider cache cleared");
    }
    
    /**
     * Remove specific provider from cache
     */
    public void removeFromCache(String providerType) {
        String cacheKey = providerType.toLowerCase();
        if (providerCache.remove(cacheKey) != null) {
            logger.info("Removed provider from cache: {}", providerType);
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "size", providerCache.size(),
            "keys", providerCache.keySet(),
            "enabled_providers", getEnabledProviders()
        );
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Create a new provider instance
     */
    private NotificationChannelProvider createNewProvider(String providerType) {
        try {
            return switch (providerType.toLowerCase()) {
                case "email", "smtp" -> createEmailProvider();
                case "sendgrid" -> createSendGridProvider();
                case "sms", "twilio" -> createSmsProvider();
                case "push", "firebase" -> createPushProvider();
                case "in-app", "inapp" -> createInAppProvider();
                case "webhook" -> createWebhookProvider();
                default -> {
                    logger.warn("Unknown provider type: {}", providerType);
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Failed to create provider: {}", providerType, e);
            return null;
        }
    }
    
    /**
     * Create email provider (SMTP)
     */
    private NotificationChannelProvider createEmailProvider() {
        if (!smtpEnabled) {
            logger.warn("SMTP email provider is disabled");
            return null;
        }
        
        try {
            // For now, return null as EmailChannelProvider is created by Spring
            // This method can be used for dynamic configuration in the future
            logger.info("Email provider (SMTP) is enabled and managed by Spring");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create email provider", e);
            return null;
        }
    }
    
    /**
     * Create SendGrid provider
     */
    private NotificationChannelProvider createSendGridProvider() {
        if (!sendgridEnabled) {
            logger.warn("SendGrid provider is disabled");
            return null;
        }
        
        try {
            // TODO: Implement SendGrid provider creation
            logger.info("SendGrid provider is enabled but not yet implemented");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create SendGrid provider", e);
            return null;
        }
    }
    
    /**
     * Create SMS provider (Twilio)
     */
    private NotificationChannelProvider createSmsProvider() {
        if (!twilioEnabled) {
            logger.warn("Twilio SMS provider is disabled");
            return null;
        }
        
        try {
            // For now, return null as SmsChannelProvider is created by Spring
            logger.info("SMS provider (Twilio) is enabled and managed by Spring");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create SMS provider", e);
            return null;
        }
    }
    
    /**
     * Create push provider (Firebase)
     */
    private NotificationChannelProvider createPushProvider() {
        if (!firebaseEnabled) {
            logger.warn("Firebase push provider is disabled");
            return null;
        }
        
        try {
            // For now, return null as PushChannelProvider is created by Spring
            logger.info("Push provider (Firebase) is enabled and managed by Spring");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create push provider", e);
            return null;
        }
    }
    
    /**
     * Create in-app provider
     */
    private NotificationChannelProvider createInAppProvider() {
        if (!inAppEnabled) {
            logger.warn("In-app provider is disabled");
            return null;
        }
        
        try {
            // For now, return null as InAppChannelProvider is created by Spring
            logger.info("In-app provider is enabled and managed by Spring");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create in-app provider", e);
            return null;
        }
    }
    
    /**
     * Create webhook provider
     */
    private NotificationChannelProvider createWebhookProvider() {
        if (!webhookEnabled) {
            logger.warn("Webhook provider is disabled");
            return null;
        }
        
        try {
            // For now, return null as WebhookChannelProvider is created by Spring
            logger.info("Webhook provider is enabled and managed by Spring");
            return null;
        } catch (Exception e) {
            logger.error("Failed to create webhook provider", e);
            return null;
        }
    }
    
    /**
     * Get provider type for a channel
     */
    private String getProviderTypeForChannel(Notification.NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> "email";
            case SMS -> "sms";
            case PUSH -> "push";
            case IN_APP -> "in-app";
            case WEBHOOK -> "webhook";
        };
    }
    
    /**
     * Validate provider configuration
     */
    private boolean validateProviderConfig(String providerType, Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            logger.warn("No configuration provided for provider: {}", providerType);
            return false;
        }
        
        // Add specific validation logic for each provider type
        return switch (providerType.toLowerCase()) {
            case "email", "smtp" -> validateEmailConfig(config);
            case "sendgrid" -> validateSendGridConfig(config);
            case "sms", "twilio" -> validateTwilioConfig(config);
            case "push", "firebase" -> validateFirebaseConfig(config);
            case "webhook" -> validateWebhookConfig(config);
            default -> true; // In-app doesn't need external config
        };
    }
    
    /**
     * Validate email configuration
     */
    private boolean validateEmailConfig(Map<String, Object> config) {
        return config.containsKey("host") && 
               config.containsKey("port") && 
               config.containsKey("username");
    }
    
    /**
     * Validate SendGrid configuration
     */
    private boolean validateSendGridConfig(Map<String, Object> config) {
        return config.containsKey("api-key") && 
               config.containsKey("from-email");
    }
    
    /**
     * Validate Twilio configuration
     */
    private boolean validateTwilioConfig(Map<String, Object> config) {
        return config.containsKey("account-sid") && 
               config.containsKey("auth-token") && 
               config.containsKey("from-number");
    }
    
    /**
     * Validate Firebase configuration
     */
    private boolean validateFirebaseConfig(Map<String, Object> config) {
        return config.containsKey("project-id") && 
               config.containsKey("private-key") && 
               config.containsKey("client-email");
    }
    
    /**
     * Validate webhook configuration
     */
    private boolean validateWebhookConfig(Map<String, Object> config) {
        return config.containsKey("timeout-seconds") && 
               config.containsKey("max-retries");
    }
}
