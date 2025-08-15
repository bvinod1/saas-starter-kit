package com.saasstarterkit.organizationalmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_alerts", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @NotNull(message = "Alert type is required")
    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @NotNull(message = "Alert severity is required")
    @Column(name = "alert_severity", nullable = false)
    private String alertSeverity;

    @NotNull(message = "Alert title is required")
    @Column(name = "alert_title", nullable = false)
    private String alertTitle;

    @NotNull(message = "Alert message is required")
    @Column(name = "alert_message", nullable = false)
    private String alertMessage;

    @Column(name = "alert_data", columnDefinition = "jsonb")
    private String alertData;

    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public OrganizationAlert() {}

    public OrganizationAlert(UUID organizationId, String alertType, String alertSeverity, String alertTitle, String alertMessage) {
        this.organizationId = organizationId;
        this.alertType = alertType;
        this.alertSeverity = alertSeverity;
        this.alertTitle = alertTitle;
        this.alertMessage = alertMessage;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getAlertSeverity() { return alertSeverity; }
    public void setAlertSeverity(String alertSeverity) { this.alertSeverity = alertSeverity; }

    public String getAlertTitle() { return alertTitle; }
    public void setAlertTitle(String alertTitle) { this.alertTitle = alertTitle; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getAlertData() { return alertData; }
    public void setAlertData(String alertData) { this.alertData = alertData; }

    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }

    public UUID getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(UUID acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public UUID getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business Methods
    public boolean isAcknowledged() {
        return Boolean.TRUE.equals(this.isAcknowledged);
    }

    public boolean isResolved() {
        return Boolean.TRUE.equals(this.isResolved);
    }

    public boolean isHighSeverity() {
        return "HIGH".equals(this.alertSeverity) || "CRITICAL".equals(this.alertSeverity);
    }

    public void acknowledge(UUID acknowledgedBy) {
        this.isAcknowledged = true;
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve(UUID resolvedBy, String resolutionNotes) {
        this.isResolved = true;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    @Override
    public String toString() {
        return "OrganizationAlert{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", alertType='" + alertType + '\'' +
                ", alertSeverity='" + alertSeverity + '\'' +
                ", alertTitle='" + alertTitle + '\'' +
                ", isAcknowledged=" + isAcknowledged +
                ", isResolved=" + isResolved +
                '}';
    }
}
