package com.saasstarterkit.notification.service.channel;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;

/**
 * Interface for notification channel providers
 * Each channel (email, SMS, push, etc.) implements this interface
 */
public interface NotificationChannelProvider {
    
    /**
     * Get the channel type this provider handles
     */
    com.saasstarterkit.notification.domain.enums.NotificationChannel getChannelType();
    
    /**
     * Check if this provider can handle the given notification
     */
    boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences);
    
    /**
     * Send a notification through this channel
     * 
     * @param notification The notification to send
     * @param preferences User preferences for this channel
     * @return DeliveryResult containing success/failure information
     */
    DeliveryResult send(Notification notification, UserNotificationPreferences preferences);
    
    /**
     * Check if the provider is available and healthy
     */
    boolean isHealthy();
    
    /**
     * Get provider-specific configuration
     */
    ChannelConfiguration getConfiguration();
    
    /**
     * Validate that the notification can be sent through this channel
     */
    ValidationResult validate(Notification notification, UserNotificationPreferences preferences);
    
    /**
     * Get estimated delivery time for this channel
     */
    long getEstimatedDeliveryTimeMs();
    
    /**
     * Get channel-specific metadata
     */
    ChannelMetadata getMetadata();
    
    // =============================================
    // INNER CLASSES
    // =============================================
    
    /**
     * Result of a notification delivery attempt
     */
    class DeliveryResult {
        private final boolean success;
        private final String messageId;
        private final String errorMessage;
        private final long deliveryTimeMs;
        private final String providerResponse;
        private final ChannelMetadata metadata;
        
        private DeliveryResult(Builder builder) {
            this.success = builder.success;
            this.messageId = builder.messageId;
            this.errorMessage = builder.errorMessage;
            this.deliveryTimeMs = builder.deliveryTimeMs;
            this.providerResponse = builder.providerResponse;
            this.metadata = builder.metadata;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorMessage() { return errorMessage; }
        public long getDeliveryTimeMs() { return deliveryTimeMs; }
        public String getProviderResponse() { return providerResponse; }
        public ChannelMetadata getMetadata() { return metadata; }
        
        public static Builder success() {
            return new Builder().success(true);
        }
        
        public static Builder failure() {
            return new Builder().success(false);
        }
        
        public static class Builder {
            private boolean success;
            private String messageId;
            private String errorMessage;
            private long deliveryTimeMs;
            private String providerResponse;
            private ChannelMetadata metadata;
            
            public Builder success(boolean success) {
                this.success = success;
                return this;
            }
            
            public Builder messageId(String messageId) {
                this.messageId = messageId;
                return this;
            }
            
            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }
            
            public Builder deliveryTimeMs(long deliveryTimeMs) {
                this.deliveryTimeMs = deliveryTimeMs;
                return this;
            }
            
            public Builder providerResponse(String providerResponse) {
                this.providerResponse = providerResponse;
                return this;
            }
            
            public Builder metadata(ChannelMetadata metadata) {
                this.metadata = metadata;
                return this;
            }
            
            public DeliveryResult build() {
                return new DeliveryResult(this);
            }
        }
    }
    
    /**
     * Result of notification validation
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String warningMessage;
        
        private ValidationResult(Builder builder) {
            this.valid = builder.valid;
            this.errorMessage = builder.errorMessage;
            this.warningMessage = builder.warningMessage;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getWarningMessage() { return warningMessage; }
        
        public static Builder valid() {
            return new Builder().valid(true);
        }
        
        public static Builder invalid() {
            return new Builder().valid(false);
        }
        
        public static class Builder {
            private boolean valid;
            private String errorMessage;
            private String warningMessage;
            
            public Builder valid(boolean valid) {
                this.valid = valid;
                return this;
            }
            
            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }
            
            public Builder warningMessage(String warningMessage) {
                this.warningMessage = warningMessage;
                return this;
            }
            
            public ValidationResult build() {
                return new ValidationResult(this);
            }
        }
    }
    
    /**
     * Channel configuration
     */
    class ChannelConfiguration {
        private final String providerName;
        private final String version;
        private final boolean enabled;
        private final int maxRetries;
        private final long retryDelayMs;
        private final int rateLimitPerMinute;
        
        private ChannelConfiguration(Builder builder) {
            this.providerName = builder.providerName;
            this.version = builder.version;
            this.enabled = builder.enabled;
            this.maxRetries = builder.maxRetries;
            this.retryDelayMs = builder.retryDelayMs;
            this.rateLimitPerMinute = builder.rateLimitPerMinute;
        }
        
        public String getProviderName() { return providerName; }
        public String getVersion() { return version; }
        public boolean isEnabled() { return enabled; }
        public int getMaxRetries() { return maxRetries; }
        public long getRetryDelayMs() { return retryDelayMs; }
        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String providerName;
            private String version;
            private boolean enabled = true;
            private int maxRetries = 3;
            private long retryDelayMs = 1000;
            private int rateLimitPerMinute = 60;
            
            public Builder providerName(String providerName) {
                this.providerName = providerName;
                return this;
            }
            
            public Builder version(String version) {
                this.version = version;
                return this;
            }
            
            public Builder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }
            
            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }
            
            public Builder retryDelayMs(long retryDelayMs) {
                this.retryDelayMs = retryDelayMs;
                return this;
            }
            
            public Builder rateLimitPerMinute(int rateLimitPerMinute) {
                this.rateLimitPerMinute = rateLimitPerMinute;
                return this;
            }
            
            public ChannelConfiguration build() {
                return new ChannelConfiguration(this);
            }
        }
    }
    
    /**
     * Channel metadata
     */
    class ChannelMetadata {
        private final String providerName;
        private final String version;
        private final String region;
        private final boolean supportsBulk;
        private final boolean supportsScheduling;
        private final boolean supportsTracking;
        private final long maxMessageSize;
        private final String supportedFormats;
        
        private ChannelMetadata(Builder builder) {
            this.providerName = builder.providerName;
            this.version = builder.version;
            this.region = builder.region;
            this.supportsBulk = builder.supportsBulk;
            this.supportsScheduling = builder.supportsScheduling;
            this.supportsTracking = builder.supportsTracking;
            this.maxMessageSize = builder.maxMessageSize;
            this.supportedFormats = builder.supportedFormats;
        }
        
        public String getProviderName() { return providerName; }
        public String getVersion() { return version; }
        public String getRegion() { return region; }
        public boolean isSupportsBulk() { return supportsBulk; }
        public boolean isSupportsScheduling() { return supportsScheduling; }
        public boolean isSupportsTracking() { return supportsTracking; }
        public long getMaxMessageSize() { return maxMessageSize; }
        public String getSupportedFormats() { return supportedFormats; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String providerName;
            private String version;
            private String region;
            private boolean supportsBulk = false;
            private boolean supportsScheduling = false;
            private boolean supportsTracking = false;
            private long maxMessageSize = 1024;
            private String supportedFormats = "text";
            
            public Builder providerName(String providerName) {
                this.providerName = providerName;
                return this;
            }
            
            public Builder version(String version) {
                this.version = version;
                return this;
            }
            
            public Builder region(String region) {
                this.region = region;
                return this;
            }
            
            public Builder supportsBulk(boolean supportsBulk) {
                this.supportsBulk = supportsBulk;
                return this;
            }
            
            public Builder supportsScheduling(boolean supportsScheduling) {
                this.supportsScheduling = supportsScheduling;
                return this;
            }
            
            public Builder supportsTracking(boolean supportsTracking) {
                this.supportsTracking = supportsTracking;
                return this;
            }
            
            public Builder maxMessageSize(long maxMessageSize) {
                this.maxMessageSize = maxMessageSize;
                return this;
            }
            
            public Builder supportedFormats(String supportedFormats) {
                this.supportedFormats = supportedFormats;
                return this;
            }
            
            public ChannelMetadata build() {
                return new ChannelMetadata(this);
            }
        }
    }
}
