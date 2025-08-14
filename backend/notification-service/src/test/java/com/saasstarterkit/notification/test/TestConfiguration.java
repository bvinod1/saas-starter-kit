package com.saasstarterkit.notification.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.MimeMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test configuration for notification service tests
 * Provides mock beans and test-specific configurations
 */
@TestConfiguration
@Profile("test")
public class TestConfiguration {
    
    // =============================================
    // MOCK BEANS
    // =============================================
    
    /**
     * Mock JavaMailSender for email tests
     */
    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        JavaMailSender mockSender = mock(JavaMailSender.class);
        MimeMessage mockMessage = mock(MimeMessage.class);
        
        try {
            when(mockSender.createMimeMessage()).thenReturn(mockMessage);
            doNothing().when(mockSender).send(any(MimeMessage.class));
        } catch (Exception e) {
            // Ignore exceptions in test setup
        }
        
        return mockSender;
    }
    
    /**
     * Mock RestTemplate for webhook tests
     */
    @Bean
    @Primary
    public RestTemplate mockRestTemplate() {
        RestTemplate mockTemplate = mock(RestTemplate.class);
        
        // Mock successful response
        // when(mockTemplate.exchange(anyString(), any(), any(), eq(String.class)))
        //     .thenReturn(ResponseEntity.ok("Success"));
        
        return mockTemplate;
    }
    
    // =============================================
    // TEST UTILITIES
    // =============================================
    
    /**
     * Test environment configuration
     */
    @Bean
    public TestEnvironmentConfig testEnvironmentConfig() {
        return new TestEnvironmentConfig();
    }
    
    /**
     * Test data factory
     */
    @Bean
    public TestDataFactory testDataFactory() {
        return new TestDataFactory();
    }
    
    // =============================================
    // INNER CLASSES
    // =============================================
    
    /**
     * Test environment configuration
     */
    public static class TestEnvironmentConfig {
        
        private boolean emailEnabled = true;
        private boolean smsEnabled = true;
        private boolean pushEnabled = true;
        private boolean inAppEnabled = true;
        private boolean webhookEnabled = true;
        
        // Getters and setters
        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
        
        public boolean isSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
        
        public boolean isPushEnabled() { return pushEnabled; }
        public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
        
        public boolean isInAppEnabled() { return inAppEnabled; }
        public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
        
        public boolean isWebhookEnabled() { return webhookEnabled; }
        public void setWebhookEnabled(boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
        
        /**
         * Disable all channels for testing
         */
        public void disableAllChannels() {
            this.emailEnabled = false;
            this.smsEnabled = false;
            this.pushEnabled = false;
            this.inAppEnabled = false;
            this.webhookEnabled = false;
        }
        
        /**
         * Enable all channels for testing
         */
        public void enableAllChannels() {
            this.emailEnabled = true;
            this.smsEnabled = true;
            this.pushEnabled = true;
            this.inAppEnabled = true;
            this.webhookEnabled = true;
        }
        
        /**
         * Enable only specific channels
         */
        public void enableChannels(boolean email, boolean sms, boolean push, boolean inApp, boolean webhook) {
            this.emailEnabled = email;
            this.smsEnabled = sms;
            this.pushEnabled = push;
            this.inAppEnabled = inApp;
            this.webhookEnabled = webhook;
        }
    }
    
    /**
     * Test data factory for creating test data
     */
    public static class TestDataFactory {
        
        /**
         * Create test notification data
         */
        public TestNotificationData createTestNotificationData() {
            return new TestNotificationData();
        }
        
        /**
         * Create test user preferences
         */
        public TestUserPreferences createTestUserPreferences() {
            return new TestUserPreferences();
        }
        
        /**
         * Create test templates
         */
        public TestTemplates createTestTemplates() {
            return new TestTemplates();
        }
    }
    
    /**
     * Test notification data
     */
    public static class TestNotificationData {
        
        private String userId = "test-user-123";
        private String tenantId = "test-tenant-456";
        private String message = "Test notification message";
        private String title = "Test notification title";
        private String subject = "Test subject";
        private String recipient = "test@example.com";
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
    }
    
    /**
     * Test user preferences
     */
    public static class TestUserPreferences {
        
        private boolean emailEnabled = true;
        private boolean smsEnabled = true;
        private boolean pushEnabled = true;
        private boolean inAppEnabled = true;
        private boolean webhookEnabled = true;
        private String timezone = "UTC";
        private String language = "en";
        
        // Getters and setters
        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
        
        public boolean isSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
        
        public boolean isPushEnabled() { return pushEnabled; }
        public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
        
        public boolean isInAppEnabled() { return inAppEnabled; }
        public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
        
        public boolean isWebhookEnabled() { return webhookEnabled; }
        public void setWebhookEnabled(boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        /**
         * Disable all channels
         */
        public void disableAllChannels() {
            this.emailEnabled = false;
            this.smsEnabled = false;
            this.pushEnabled = false;
            this.inAppEnabled = false;
            this.webhookEnabled = false;
        }
        
        /**
         * Enable only email
         */
        public void enableEmailOnly() {
            disableAllChannels();
            this.emailEnabled = true;
        }
        
        /**
         * Enable only SMS
         */
        public void enableSmsOnly() {
            disableAllChannels();
            this.smsEnabled = true;
        }
        
        /**
         * Enable only push
         */
        public void enablePushOnly() {
            disableAllChannels();
            this.pushEnabled = true;
        }
        
        /**
         * Enable only in-app
         */
        public void enableInAppOnly() {
            disableAllChannels();
            this.inAppEnabled = true;
        }
        
        /**
         * Enable only webhook
         */
        public void enableWebhookOnly() {
            disableAllChannels();
            this.webhookEnabled = true;
        }
    }
    
    /**
     * Test templates
     */
    public static class TestTemplates {
        
        private String emailTemplate = "Hello {{name}}, this is a test email.";
        private String smsTemplate = "Hi {{name}}, this is a test SMS.";
        private String pushTemplate = "{{title}}: {{message}}";
        private String inAppTemplate = "{{title}}: {{message}}";
        private String webhookTemplate = "{\"message\": \"{{message}}\", \"timestamp\": \"{{timestamp}}\"}";
        
        // Getters and setters
        public String getEmailTemplate() { return emailTemplate; }
        public void setEmailTemplate(String emailTemplate) { this.emailTemplate = emailTemplate; }
        
        public String getSmsTemplate() { return smsTemplate; }
        public void setSmsTemplate(String smsTemplate) { this.smsTemplate = smsTemplate; }
        
        public String getPushTemplate() { return pushTemplate; }
        public void setPushTemplate(String pushTemplate) { this.pushTemplate = pushTemplate; }
        
        public String getInAppTemplate() { return inAppTemplate; }
        public void setInAppTemplate(String inAppTemplate) { this.inAppTemplate = inAppTemplate; }
        
        public String getWebhookTemplate() { return webhookTemplate; }
        public void setWebhookTemplate(String webhookTemplate) { this.webhookTemplate = webhookTemplate; }
    }
}
