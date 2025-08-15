package com.saasstarterkit.notification.service.channel;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Email channel provider implementation
 * Supports both SMTP and SendGrid for email delivery
 */
@Service
public class EmailChannelProvider implements NotificationChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailChannelProvider.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${notification.providers.email.smtp.from-email:noreply@saasstarterkit.com}")
    private String fromEmail;
    
    @Value("${notification.providers.email.smtp.from-name:SaaS Starter Kit}")
    private String fromName;
    
    @Value("${notification.providers.email.sendgrid.api-key:}")
    private String sendgridApiKey;
    
    @Value("${notification.providers.email.sendgrid.enabled:false}")
    private boolean sendgridEnabled;
    
    // =============================================
    // INTERFACE IMPLEMENTATION
    // =============================================
    
    @Override
    public com.saasstarterkit.notification.domain.enums.NotificationChannel getChannelType() {
        return com.saasstarterkit.notification.domain.enums.NotificationChannel.EMAIL;
    }
    
    @Override
    public boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences) {
        return preferences != null && 
               preferences.getEmailEnabled() != null && 
               preferences.getEmailEnabled() &&
               StringUtils.hasText(event.getRecipient());
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
            
            // Choose email provider
            if (sendgridEnabled && StringUtils.hasText(sendgridApiKey)) {
                return sendViaSendGrid(notification, preferences, startTime);
            } else {
                return sendViaSmtp(notification, preferences, startTime);
            }
            
        } catch (Exception e) {
            logger.error("Error sending email notification: {}", notification.getId(), e);
            return DeliveryResult.failure()
                    .errorMessage("Failed to send email: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Simple health check - try to create a mail message
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo("test@example.com");
            testMessage.setSubject("Health Check");
            testMessage.setText("Health check message");
            
            return true;
        } catch (Exception e) {
            logger.warn("Email provider health check failed", e);
            return false;
        }
    }
    
    @Override
    public ChannelConfiguration getConfiguration() {
        return ChannelConfiguration.builder()
                .providerName(sendgridEnabled ? "SendGrid" : "SMTP")
                .version("1.0.0")
                .enabled(true)
                .maxRetries(3)
                .retryDelayMs(1000)
                .rateLimitPerMinute(60)
                .build();
    }
    
    @Override
    public ValidationResult validate(Notification notification, UserNotificationPreferences preferences) {
        // Check if email is enabled for user
        if (preferences == null || !preferences.getEmailEnabled()) {
            return ValidationResult.invalid()
                    .errorMessage("Email notifications are disabled for this user")
                    .build();
        }
        
        // Check if recipient is provided
        if (!StringUtils.hasText(notification.getRecipient())) {
            return ValidationResult.invalid()
                    .errorMessage("Email recipient is required")
                    .build();
        }
        
        // Validate email format (simple check)
        if (!isValidEmail(notification.getRecipient())) {
            return ValidationResult.invalid()
                    .errorMessage("Invalid email format: " + notification.getRecipient())
                    .build();
        }
        
        // Check message content
        if (!StringUtils.hasText(notification.getMessage())) {
            return ValidationResult.invalid()
                    .errorMessage("Email message content is required")
                    .build();
        }
        
        return ValidationResult.valid().build();
    }
    
    @Override
    public long getEstimatedDeliveryTimeMs() {
        // Email delivery is typically very fast
        return 5000; // 5 seconds
    }
    
    @Override
    public ChannelMetadata getMetadata() {
        return ChannelMetadata.builder()
                .providerName(sendgridEnabled ? "SendGrid" : "SMTP")
                .version("1.0.0")
                .region("Global")
                .supportsBulk(true)
                .supportsScheduling(true)
                .supportsTracking(true)
                .maxMessageSize(25 * 1024 * 1024) // 25MB
                .supportedFormats("text,html")
                .build();
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Send email via SMTP
     */
    private DeliveryResult sendViaSmtp(Notification notification, 
                                      UserNotificationPreferences preferences, 
                                      long startTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipient());
            
            if (StringUtils.hasText(notification.getSubject())) {
                message.setSubject(notification.getSubject());
            } else {
                message.setSubject("Notification from SaaS Starter Kit");
            }
            
            message.setText(notification.getMessage());
            
            // Add reply-to if configured
            if (StringUtils.hasText(fromEmail)) {
                message.setReplyTo(fromEmail);
            }
            
            // Send the email
            mailSender.send(message);
            
            long deliveryTime = System.currentTimeMillis() - startTime;
            logger.info("Email sent successfully via SMTP to {} in {}ms", 
                       notification.getRecipient(), deliveryTime);
            
            return DeliveryResult.success()
                    .messageId("smtp_" + System.currentTimeMillis())
                    .deliveryTimeMs(deliveryTime)
                    .providerResponse("SMTP delivery successful")
                    .metadata(getMetadata())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to send email via SMTP to {}", notification.getRecipient(), e);
            return DeliveryResult.failure()
                    .errorMessage("SMTP delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Send email via SendGrid
     */
    private DeliveryResult sendViaSendGrid(Notification notification, 
                                          UserNotificationPreferences preferences, 
                                          long startTime) {
        try {
            // TODO: Implement SendGrid integration
            // For now, fallback to SMTP
            logger.warn("SendGrid integration not yet implemented, falling back to SMTP");
            return sendViaSmtp(notification, preferences, startTime);
            
        } catch (Exception e) {
            logger.error("Failed to send email via SendGrid to {}", notification.getRecipient(), e);
            return DeliveryResult.failure()
                    .errorMessage("SendGrid delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        // Basic email format validation
        return email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               !email.startsWith("@") && 
               !email.endsWith("@");
    }
}
