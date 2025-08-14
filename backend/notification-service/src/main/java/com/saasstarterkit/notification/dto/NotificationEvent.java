package com.saasstarterkit.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.saasstarterkit.notification.entity.Notification.NotificationChannel;
import com.saasstarterkit.notification.entity.Notification.NotificationPriority;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification events published to RabbitMQ
 * This is the core event structure for the event-driven notification system
 */
public class NotificationEvent {
    
    private String eventId;
    private String eventType;
    private String correlationId;
    
    // User and tenant information
    private String userId;
    private String tenantId;
    private String organizationId;
    
    // Notification content
    private String templateName;
    private NotificationChannel channel;
    private String title;
    private String message;
    private String subject;
    private String recipient;
    
    // Metadata
    private NotificationPriority priority;
    private String category;
    private Map<String, Object> variables;
    private Map<String, Object> metadata;
    
    // Timing
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    // Source information
    private String sourceService;
    private String sourceEvent;
    private String sourceUserId;
    
    // Processing flags
    private Boolean immediate = false;
    private Boolean retryOnFailure = true;
    private Integer maxRetries = 3;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public NotificationEvent() {}
    
    public NotificationEvent(String eventType, String userId, NotificationChannel channel, String message) {
        this.eventType = eventType;
        this.userId = userId;
        this.channel = channel;
        this.message = message;
        this.eventId = java.util.UUID.randomUUID().toString();
        this.correlationId = java.util.UUID.randomUUID().toString();
    }
    
    // =============================================
    // GETTERS AND SETTERS
    // =============================================
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    
    public String getSourceEvent() { return sourceEvent; }
    public void setSourceEvent(String sourceEvent) { this.sourceEvent = sourceEvent; }
    
    public String getSourceUserId() { return sourceUserId; }
    public void setSourceUserId(String sourceUserId) { this.sourceUserId = sourceUserId; }
    
    public Boolean getImmediate() { return immediate; }
    public void setImmediate(Boolean immediate) { this.immediate = immediate; }
    
    public Boolean getRetryOnFailure() { return retryOnFailure; }
    public void setRetryOnFailure(Boolean retryOnFailure) { this.retryOnFailure = retryOnFailure; }
    
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    
    // =============================================
    // BUILDER METHODS
    // =============================================
    
    public NotificationEvent withTemplate(String templateName) {
        this.templateName = templateName;
        return this;
    }
    
    public NotificationEvent withPriority(NotificationPriority priority) {
        this.priority = priority;
        return this;
    }
    
    public NotificationEvent withCategory(String category) {
        this.category = category;
        return this;
    }
    
    public NotificationEvent withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }
    
    public NotificationEvent withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }
    
    public NotificationEvent withSubject(String subject) {
        this.subject = subject;
        return this;
    }
    
    public NotificationEvent withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public NotificationEvent withTenant(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
    
    public NotificationEvent withOrganization(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }
    
    public NotificationEvent withScheduling(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
        return this;
    }
    
    public NotificationEvent withExpiry(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }
    
    public NotificationEvent withSource(String sourceService, String sourceEvent) {
        this.sourceService = sourceService;
        this.sourceEvent = sourceEvent;
        return this;
    }
    
    public NotificationEvent withImmediate(Boolean immediate) {
        this.immediate = immediate;
        return this;
    }
    
    public NotificationEvent withRetryConfig(Boolean retryOnFailure, Integer maxRetries) {
        this.retryOnFailure = retryOnFailure;
        this.maxRetries = maxRetries;
        return this;
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isImmediate() {
        return immediate != null && immediate;
    }
    
    @Override
    public String toString() {
        return "NotificationEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", userId='" + userId + '\'' +
                ", channel=" + channel +
                ", templateName='" + templateName + '\'' +
                ", priority=" + priority +
                ", immediate=" + immediate +
                '}';
    }
}
