package com.saasstarterkit.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification entity for storing individual notification records
 * Tracks delivery status, retry attempts, and user interactions
 */
@Entity
@Table(name = "notifications", schema = "shared")
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @Column(name = "organization_id")
    private String organizationId;
    
    @Column(name = "template_id")
    private Long templateId;
    
    @NotBlank
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel type;
    
    @Column(name = "title")
    private String title;
    
    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "recipient", nullable = false)
    private String recipient; // email, phone, device token, etc.
    
    @Column(name = "data", columnDefinition = "JSONB")
    private String data; // JSON string of additional data
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;
    
    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "provider_response")
    private String providerResponse; // Response from external service
    
    @Column(name = "channel_specific_data", columnDefinition = "JSONB")
    private String channelSpecificData; // Channel-specific metadata
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "source_event")
    private String sourceEvent; // Event that triggered this notification
    
    @Column(name = "correlation_id")
    private String correlationId; // For tracking related notifications
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public Notification() {}
    
    public Notification(String userId, NotificationChannel type, String message, String recipient) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.recipient = recipient;
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
    
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    
    public NotificationChannel getType() { return type; }
    public void setType(NotificationChannel type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    
    public String getChannelSpecificData() { return channelSpecificData; }
    public void setChannelSpecificData(String channelSpecificData) { this.channelSpecificData = channelSpecificData; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getSourceEvent() { return sourceEvent; }
    public void setSourceEvent(String sourceEvent) { this.sourceEvent = sourceEvent; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // =============================================
    // ENUMS
    // =============================================
    
    public enum NotificationChannel {
        EMAIL, SMS, PUSH, IN_APP, WEBHOOK
    }
    
    public enum NotificationStatus {
        PENDING, SCHEDULED, SENDING, SENT, DELIVERED, READ, FAILED, CANCELLED, EXPIRED
    }
    
    public enum NotificationPriority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }
    
    public boolean isScheduled() {
        return status == NotificationStatus.SCHEDULED;
    }
    
    public boolean isSent() {
        return status == NotificationStatus.SENT || status == NotificationStatus.DELIVERED;
    }
    
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }
    
    public boolean isExpired() {
        return status == NotificationStatus.EXPIRED;
    }
    
    public boolean canRetry() {
        return attemptCount < maxAttempts && (isFailed() || status == NotificationStatus.PENDING);
    }
    
    public boolean isRead() {
        return readAt != null;
    }
    
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.attemptCount++;
    }
    
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.attemptCount++;
    }
    
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", recipient='" + recipient + '\'' +
                ", attemptCount=" + attemptCount +
                '}';
    }
}
