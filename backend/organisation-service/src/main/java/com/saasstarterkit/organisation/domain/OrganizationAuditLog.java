package com.saasstarterkit.organisation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_audit_logs", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Organization ID is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @NotNull(message = "Resource type is required")
    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "details", columnDefinition = "jsonb")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "compliance_category")
    private String complianceCategory;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "tags")
    private String tags;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OrganizationAuditLog() {}

    public OrganizationAuditLog(Organization organization, UUID userId, AuditAction action, String resourceType) {
        this.organization = organization;
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getComplianceCategory() { return complianceCategory; }
    public void setComplianceCategory(String complianceCategory) { this.complianceCategory = complianceCategory; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Business Methods
    public boolean isHighRisk() {
        return RiskLevel.HIGH.equals(this.riskLevel);
    }

    public boolean isMediumRisk() {
        return RiskLevel.MEDIUM.equals(this.riskLevel);
    }

    public boolean isLowRisk() {
        return RiskLevel.LOW.equals(this.riskLevel);
    }

    public boolean isComplianceRelated() {
        return this.complianceCategory != null && !this.complianceCategory.isEmpty();
    }

    public boolean isSecurityEvent() {
        return AuditAction.LOGIN_FAILED.equals(this.action) ||
               AuditAction.ACCESS_DENIED.equals(this.action) ||
               AuditAction.PERMISSION_CHANGED.equals(this.action) ||
               AuditAction.ROLE_CHANGED.equals(this.action);
    }

    @Override
    public String toString() {
        return "OrganizationAuditLog{" +
                "id=" + id +
                ", organizationId=" + (organization != null ? organization.getId() : "null") +
                ", userId=" + userId +
                ", action=" + action +
                ", resourceType='" + resourceType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
