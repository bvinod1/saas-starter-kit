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
 * Notification template entity for storing reusable notification templates
 * Supports multiple channels (email, SMS, push, in-app) and languages
 */
@Entity
@Table(name = "notification_templates", schema = "shared")
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @NotBlank
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel type;
    
    @Column(name = "subject")
    private String subject;
    
    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "language", length = 10)
    private String language = "en";
    
    @Column(name = "variables", columnDefinition = "JSONB")
    private String variables; // JSON string of available variables
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "tenant_id")
    private String tenantId; // null for shared templates
    
    @Column(name = "category")
    private String category; // welcome, password_reset, billing, etc.
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @Column(name = "retry_count")
    private Integer retryCount = 3;
    
    @Column(name = "expiry_hours")
    private Integer expiryHours = 24;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public NotificationTemplate() {}
    
    public NotificationTemplate(String name, NotificationChannel type, String content) {
        this.name = name;
        this.type = type;
        this.content = content;
    }
    
    // =============================================
    // GETTERS AND SETTERS
    // =============================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public NotificationChannel getType() { return type; }
    public void setType(NotificationChannel type) { this.type = type; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public Integer getExpiryHours() { return expiryHours; }
    public void setExpiryHours(Integer expiryHours) { this.expiryHours = expiryHours; }
    
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
    
    public enum NotificationPriority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    public boolean isShared() {
        return tenantId == null;
    }
    
    public boolean isActive() {
        return isActive != null && isActive;
    }
    
    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", language='" + language + '\'' +
                ", isActive=" + isActive +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
