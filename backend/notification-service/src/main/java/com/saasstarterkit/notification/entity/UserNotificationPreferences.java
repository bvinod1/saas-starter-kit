package com.saasstarterkit.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;
import java.util.Map;

/**
 * User notification preferences entity
 * Stores user preferences for notification channels, timing, and language
 */
@Entity
@Table(name = "user_notification_preferences", schema = "shared")
@EntityListeners(AuditingEntityListener.class)
public class UserNotificationPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @Column(name = "organization_id")
    private String organizationId;
    
    // Channel preferences
    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;
    
    @Column(name = "sms_enabled")
    private Boolean smsEnabled = true;
    
    @Column(name = "push_enabled")
    private Boolean pushEnabled = true;
    
    @Column(name = "in_app_enabled")
    private Boolean inAppEnabled = true;
    
    @Column(name = "webhook_enabled")
    private Boolean webhookEnabled = false;
    
    // Timing preferences
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;
    
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;
    
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";
    
    // Language preferences
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";
    
    @Column(name = "fallback_language", length = 10)
    private String fallbackLanguage = "en";
    
    // Category preferences
    @Column(name = "category_preferences", columnDefinition = "JSONB")
    private String categoryPreferences; // JSON string of category-specific settings
    
    // Frequency preferences
    @Column(name = "digest_frequency")
    @Enumerated(EnumType.STRING)
    private DigestFrequency digestFrequency = DigestFrequency.DAILY;
    
    @Column(name = "max_notifications_per_day")
    private Integer maxNotificationsPerDay = 50;
    
    @Column(name = "urgent_notifications_always")
    private Boolean urgentNotificationsAlways = true;
    
    // Channel-specific preferences
    @Column(name = "email_preferences", columnDefinition = "JSONB")
    private String emailPreferences; // JSON string of email-specific settings
    
    @Column(name = "sms_preferences", columnDefinition = "JSONB")
    private String smsPreferences; // JSON string of SMS-specific settings
    
    @Column(name = "push_preferences", columnDefinition = "JSONB")
    private String pushPreferences; // JSON string of push-specific settings
    
    @Column(name = "in_app_preferences", columnDefinition = "JSONB")
    private String inAppPreferences; // JSON string of in-app specific settings
    
    // Device preferences
    @Column(name = "device_tokens", columnDefinition = "JSONB")
    private String deviceTokens; // JSON array of device tokens for push notifications
    
    @Column(name = "webhook_endpoints", columnDefinition = "JSONB")
    private String webhookEndpoints; // JSON array of webhook URLs
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public UserNotificationPreferences() {}
    
    public UserNotificationPreferences(String userId) {
        this.userId = userId;
    }
    
    // =============================================
    // GETTERS AND SETTERS
    // =============================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    
    public Boolean getSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    
    public Boolean getPushEnabled() { return pushEnabled; }
    public void setPushEnabled(Boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    
    public Boolean getInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
    
    public Boolean getWebhookEnabled() { return webhookEnabled; }
    public void setWebhookEnabled(Boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
    
    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }
    
    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    
    public String getFallbackLanguage() { return fallbackLanguage; }
    public void setFallbackLanguage(String fallbackLanguage) { this.fallbackLanguage = fallbackLanguage; }
    
    public String getCategoryPreferences() { return categoryPreferences; }
    public void setCategoryPreferences(String categoryPreferences) { this.categoryPreferences = categoryPreferences; }
    
    public DigestFrequency getDigestFrequency() { return digestFrequency; }
    public void setDigestFrequency(DigestFrequency digestFrequency) { this.digestFrequency = digestFrequency; }
    
    public Integer getMaxNotificationsPerDay() { return maxNotificationsPerDay; }
    public void setMaxNotificationsPerDay(Integer maxNotificationsPerDay) { this.maxNotificationsPerDay = maxNotificationsPerDay; }
    
    public Boolean getUrgentNotificationsAlways() { return urgentNotificationsAlways; }
    public void setUrgentNotificationsAlways(Boolean urgentNotificationsAlways) { this.urgentNotificationsAlways = urgentNotificationsAlways; }
    
    public String getEmailPreferences() { return emailPreferences; }
    public void setEmailPreferences(String emailPreferences) { this.emailPreferences = emailPreferences; }
    
    public String getSmsPreferences() { return smsPreferences; }
    public void setSmsPreferences(String smsPreferences) { this.smsPreferences = smsPreferences; }
    
    public String getPushPreferences() { return pushPreferences; }
    public void setPushPreferences(String pushPreferences) { this.pushPreferences = pushPreferences; }
    
    public String getInAppPreferences() { return inAppPreferences; }
    public void setInAppPreferences(String inAppPreferences) { this.inAppPreferences = inAppPreferences; }
    
    public String getDeviceTokens() { return deviceTokens; }
    public void setDeviceTokens(String deviceTokens) { this.deviceTokens = deviceTokens; }
    
    public String getWebhookEndpoints() { return webhookEndpoints; }
    public void setWebhookEndpoints(String webhookEndpoints) { this.webhookEndpoints = webhookEndpoints; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // =============================================
    // ENUMS
    // =============================================
    
    public enum DigestFrequency {
        IMMEDIATE, HOURLY, DAILY, WEEKLY, NEVER
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    public boolean isChannelEnabled(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return emailEnabled != null && emailEnabled;
            case SMS:
                return smsEnabled != null && smsEnabled;
            case PUSH:
                return pushEnabled != null && pushEnabled;
            case IN_APP:
                return inAppEnabled != null && inAppEnabled;
            case WEBHOOK:
                return webhookEnabled != null && webhookEnabled;
            default:
                return false;
        }
    }
    
    public boolean isInQuietHours(java.time.LocalTime currentTime) {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Same day (e.g., 22:00 to 06:00)
            return currentTime.isAfter(quietHoursStart) && currentTime.isBefore(quietHoursEnd);
        } else {
            // Overnight (e.g., 22:00 to 06:00)
            return currentTime.isAfter(quietHoursStart) || currentTime.isBefore(quietHoursEnd);
        }
    }
    
    public boolean shouldSendNotification(NotificationPriority priority) {
        if (priority == NotificationPriority.URGENT && urgentNotificationsAlways) {
            return true;
        }
        
        return !isInQuietHours(java.time.LocalTime.now());
    }
    
    public boolean isActive() {
        return isActive != null && isActive;
    }
    
    @Override
    public String toString() {
        return "UserNotificationPreferences{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", emailEnabled=" + emailEnabled +
                ", smsEnabled=" + smsEnabled +
                ", pushEnabled=" + pushEnabled +
                ", inAppEnabled=" + inAppEnabled +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                '}';
    }
}
