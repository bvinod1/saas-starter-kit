package com.saasstarterkit.tenant.domain.model;

import com.saasstarterkit.tenant.domain.model.converter.JsonMapConverter;
import com.saasstarterkit.tenant.domain.model.enums.ApprovalStatus;
import com.saasstarterkit.tenant.domain.model.enums.RolloutStage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ConfigurationHistory - Tracks configuration changes for versioning and audit trails
 * 
 * Implements:
 * 4. Versioning & Staged Rollouts
 * 5. Compliance & Backup support
 * 9. Monitoring & Alerting
 */
@Entity
@Table(name = "configuration_history", indexes = {
    @Index(name = "idx_config_history_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_config_history_config_id", columnList = "configuration_id"),
    @Index(name = "idx_config_history_version", columnList = "version"),
    @Index(name = "idx_config_history_created_at", columnList = "created_at"),
    @Index(name = "idx_config_history_change_type", columnList = "change_type")
})
@EntityListeners(AuditingEntityListener.class)
public class ConfigurationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "configuration_id", nullable = false)
    private UUID configurationId;

    @NotBlank
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @NotBlank
    @Column(name = "configuration_key", nullable = false)
    private String configurationKey;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "change_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "change_description")
    private String changeDescription;

    @Column(name = "rollout_stage")
    @Enumerated(EnumType.STRING)
    private RolloutStage rolloutStage;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deployed_by")
    private String deployedBy;

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @Column(name = "rollback_version")
    private Long rollbackVersion;

    @Column(name = "rollback_reason")
    private String rollbackReason;

    @Column(name = "rollback_by")
    private String rollbackBy;

    @Column(name = "rollback_at")
    private LocalDateTime rollbackAt;

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter.class)
    private java.util.Map<String, Object> metadata = new java.util.HashMap<>();

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "correlation_id")
    private String correlationId;

    // Constructors
    public ConfigurationHistory() {}

    public ConfigurationHistory(UUID configurationId, String tenantId, String configurationKey, 
                              String oldValue, String newValue, Long version, ChangeType changeType) {
        this.configurationId = configurationId;
        this.tenantId = tenantId;
        this.configurationKey = configurationKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.version = version;
        this.changeType = changeType;
        this.createdAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isRollback() {
        return changeType == ChangeType.ROLLBACK;
    }

    public boolean isDeployment() {
        return changeType == ChangeType.DEPLOY || changeType == ChangeType.CANARY_DEPLOY;
    }

    public boolean isApproval() {
        return changeType == ChangeType.APPROVE || changeType == ChangeType.REJECT;
    }

    public boolean canRollback() {
        return rollbackVersion != null && !isRollback();
    }

    public void markAsRolledBack(Long rollbackVersion, String rollbackReason, String rollbackBy) {
        this.rollbackVersion = rollbackVersion;
        this.rollbackReason = rollbackReason;
        this.rollbackBy = rollbackBy;
        this.rollbackAt = LocalDateTime.now();
    }

    public void markAsDeployed(String deployedBy) {
        this.deployedBy = deployedBy;
        this.deployedAt = LocalDateTime.now();
    }

    public void markAsApproved(String approvedBy) {
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void markAsRejected(String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.changeReason = reason;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConfigurationId() { return configurationId; }
    public void setConfigurationId(UUID configurationId) { this.configurationId = configurationId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getConfigurationKey() { return configurationKey; }
    public void setConfigurationKey(String configurationKey) { this.configurationKey = configurationKey; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }

    public RolloutStage getRolloutStage() { return rolloutStage; }
    public void setRolloutStage(RolloutStage rolloutStage) { this.rolloutStage = rolloutStage; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getDeployedBy() { return deployedBy; }
    public void setDeployedBy(String deployedBy) { this.deployedBy = deployedBy; }

    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }

    public Long getRollbackVersion() { return rollbackVersion; }
    public void setRollbackVersion(Long rollbackVersion) { this.rollbackVersion = rollbackVersion; }

    public String getRollbackReason() { return rollbackReason; }
    public void setRollbackReason(String rollbackReason) { this.rollbackReason = rollbackReason; }

    public String getRollbackBy() { return rollbackBy; }
    public void setRollbackBy(String rollbackBy) { this.rollbackBy = rollbackBy; }

    public LocalDateTime getRollbackAt() { return rollbackAt; }
    public void setRollbackAt(LocalDateTime rollbackAt) { this.rollbackAt = rollbackAt; }

    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    @Override
    public String toString() {
        return "ConfigurationHistory{" +
                "id=" + id +
                ", configurationId=" + configurationId +
                ", tenantId='" + tenantId + '\'' +
                ", configurationKey='" + configurationKey + '\'' +
                ", version=" + version +
                ", changeType=" + changeType +
                ", rolloutStage=" + rolloutStage +
                ", approvalStatus=" + approvalStatus +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * ChangeType - Defines the type of configuration change
     */
    public enum ChangeType {
        CREATE("create", "Configuration was created"),
        UPDATE("update", "Configuration was updated"),
        DELETE("delete", "Configuration was deleted"),
        DEPLOY("deploy", "Configuration was deployed"),
        CANARY_DEPLOY("canary_deploy", "Configuration was deployed to canary"),
        ROLLBACK("rollback", "Configuration was rolled back"),
        APPROVE("approve", "Configuration change was approved"),
        REJECT("reject", "Configuration change was rejected"),
        ENABLE("enable", "Configuration was enabled"),
        DISABLE("disable", "Configuration was disabled"),
        ARCHIVE("archive", "Configuration was archived"),
        RESTORE("restore", "Configuration was restored"),
        MIGRATE("migrate", "Configuration was migrated"),
        VALIDATE("validate", "Configuration was validated"),
        TEST("test", "Configuration was tested"),
        AUDIT("audit", "Configuration was audited");

        private final String value;
        private final String description;

        ChangeType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static ChangeType fromValue(String value) {
            for (ChangeType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown ChangeType: " + value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
