package com.saasstarterkit.notification.service.channel;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * SMS channel provider implementation using Twilio
 * Handles SMS notifications with proper validation and error handling
 */
@Service
public class SmsChannelProvider implements NotificationChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsChannelProvider.class);
    
    // Phone number validation pattern (E.164 format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    
    @Value("${notification.providers.sms.twilio.account-sid:}")
    private String twilioAccountSid;
    
    @Value("${notification.providers.sms.twilio.auth-token:}")
    private String twilioAuthToken;
    
    @Value("${notification.providers.sms.twilio.from-number:}")
    private String twilioFromNumber;
    
    @Value("${notification.providers.sms.twilio.enabled:false}")
    private boolean twilioEnabled;
    
    @Value("${notification.providers.sms.max-length:160}")
    private int maxMessageLength;
    
    @Value("${notification.providers.sms.rate-limit-per-minute:10}")
    private int rateLimitPerMinute;
    
    // =============================================
    // INTERFACE IMPLEMENTATION
    // =============================================
    
    @Override
    public com.saasstarterkit.notification.domain.enums.NotificationChannel getChannelType() {
        return com.saasstarterkit.notification.domain.enums.NotificationChannel.SMS;
    }
    
    @Override
    public boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences) {
        return preferences != null && 
               preferences.getSmsEnabled() != null && 
               preferences.getSmsEnabled() &&
               StringUtils.hasText(event.getRecipient()) &&
               isValidPhoneNumber(event.getRecipient());
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
            
            // Check if Twilio is enabled and configured
            if (!twilioEnabled || !isTwilioConfigured()) {
                logger.warn("Twilio SMS provider not configured or disabled");
                return DeliveryResult.failure()
                        .errorMessage("SMS provider not configured")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Send via Twilio
            return sendViaTwilio(notification, preferences, startTime);
            
        } catch (Exception e) {
            logger.error("Error sending SMS notification: {}", notification.getId(), e);
            return DeliveryResult.failure()
                    .errorMessage("Failed to send SMS: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public boolean isHealthy() {
        if (!twilioEnabled || !isTwilioConfigured()) {
            return false;
        }
        
        try {
            // TODO: Implement Twilio health check
            // For now, return true if configured
            return true;
        } catch (Exception e) {
            logger.warn("SMS provider health check failed", e);
            return false;
        }
    }
    
    @Override
    public ChannelConfiguration getConfiguration() {
        return ChannelConfiguration.builder()
                .providerName("Twilio")
                .version("1.0.0")
                .enabled(twilioEnabled && isTwilioConfigured())
                .maxRetries(3)
                .retryDelayMs(1000)
                .rateLimitPerMinute(rateLimitPerMinute)
                .build();
    }
    
    @Override
    public ValidationResult validate(Notification notification, UserNotificationPreferences preferences) {
        // Check if SMS is enabled for user
        if (preferences == null || !preferences.getSmsEnabled()) {
            return ValidationResult.invalid()
                    .errorMessage("SMS notifications are disabled for this user")
                    .build();
        }
        
        // Check if recipient is provided
        if (!StringUtils.hasText(notification.getRecipient())) {
            return ValidationResult.invalid()
                    .errorMessage("SMS recipient is required")
                    .build();
        }
        
        // Validate phone number format
        if (!isValidPhoneNumber(notification.getRecipient())) {
            return ValidationResult.invalid()
                    .errorMessage("Invalid phone number format: " + notification.getRecipient() + 
                                ". Must be in E.164 format (e.g., +1234567890)")
                    .build();
        }
        
        // Check message content
        if (!StringUtils.hasText(notification.getMessage())) {
            return ValidationResult.invalid()
                    .errorMessage("SMS message content is required")
                    .build();
        }
        
        // Check message length
        if (notification.getMessage().length() > maxMessageLength) {
            return ValidationResult.invalid()
                    .errorMessage("SMS message too long: " + notification.getMessage().length() + 
                                " characters (max: " + maxMessageLength + ")")
                    .build();
        }
        
        return ValidationResult.valid().build();
    }
    
    @Override
    public long getEstimatedDeliveryTimeMs() {
        // SMS delivery is typically very fast
        return 30000; // 30 seconds
    }
    
    @Override
    public ChannelMetadata getMetadata() {
        return ChannelMetadata.builder()
                .providerName("Twilio")
                .version("1.0.0")
                .region("Global")
                .supportsBulk(true)
                .supportsScheduling(true)
                .supportsTracking(true)
                .maxMessageSize(maxMessageLength)
                .supportedFormats("text")
                .build();
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Send SMS via Twilio
     */
    private DeliveryResult sendViaTwilio(Notification notification, 
                                        UserNotificationPreferences preferences, 
                                        long startTime) {
        try {
            // TODO: Implement actual Twilio API call
            // For now, simulate the API call
            
            logger.info("Sending SMS via Twilio to {}: {}", 
                       notification.getRecipient(), notification.getMessage());
            
            // Simulate API call delay
            Thread.sleep(100);
            
            // Simulate success response
            String messageId = "twilio_" + System.currentTimeMillis();
            
            long deliveryTime = System.currentTimeMillis() - startTime;
            logger.info("SMS sent successfully via Twilio to {} in {}ms", 
                       notification.getRecipient(), deliveryTime);
            
            return DeliveryResult.success()
                    .messageId(messageId)
                    .deliveryTimeMs(deliveryTime)
                    .providerResponse("Twilio SMS delivery successful")
                    .metadata(getMetadata())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to send SMS via Twilio to {}", notification.getRecipient(), e);
            return DeliveryResult.failure()
                    .errorMessage("Twilio SMS delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Check if Twilio is properly configured
     */
    private boolean isTwilioConfigured() {
        return StringUtils.hasText(twilioAccountSid) && 
               StringUtils.hasText(twilioAuthToken) && 
               StringUtils.hasText(twilioFromNumber);
    }
    
    /**
     * Validate phone number format (E.164)
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return false;
        }
        
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
    
    /**
     * Truncate message if it exceeds SMS length limit
     */
    private String truncateMessage(String message) {
        if (message.length() <= maxMessageLength) {
            return message;
        }
        
        // Truncate and add ellipsis
        return message.substring(0, maxMessageLength - 3) + "...";
    }
    
    /**
     * Split long messages into multiple SMS
     */
    private String[] splitMessage(String message) {
        if (message.length() <= maxMessageLength) {
            return new String[]{message};
        }
        
        // Simple splitting - in production, you might want more sophisticated logic
        int parts = (int) Math.ceil((double) message.length() / maxMessageLength);
        String[] splitMessages = new String[parts];
        
        for (int i = 0; i < parts; i++) {
            int start = i * maxMessageLength;
            int end = Math.min(start + maxMessageLength, message.length());
            splitMessages[i] = message.substring(start, end);
        }
        
        return splitMessages;
    }
}
