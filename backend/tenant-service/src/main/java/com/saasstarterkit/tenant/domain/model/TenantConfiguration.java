package com.saasstarterkit.tenant.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.saasstarterkit.tenant.domain.model.enums.ApprovalStatus;
import com.saasstarterkit.tenant.domain.model.enums.ConfigurationScope;
import com.saasstarterkit.tenant.domain.model.enums.ConfigurationType;
import com.saasstarterkit.tenant.domain.model.enums.DataType;
import com.saasstarterkit.tenant.domain.model.enums.Environment;
import com.saasstarterkit.tenant.domain.model.enums.RolloutStage;
import com.saasstarterkit.tenant.domain.model.enums.ServiceTier;
import com.saasstarterkit.tenant.domain.model.converter.JsonMapConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TenantConfiguration - Core entity for tenant-aware configuration management
 * 
 * Implements:
 * 1. Tenant-Aware Configuration Storage
 * 2. Hierarchical config (global defaults, tenant overrides, environment-specific)
 * 3. Versioning & Staged Rollouts
 * 4. Environment and Tier Support
 * 5. Compliance & Backup support
 */
@Entity
@Table(name = "tenant_configurations", indexes = {
    @Index(name = "idx_tenant_config_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_tenant_config_env_tier", columnList = "environment, service_tier"),
    @Index(name = "idx_tenant_config_version", columnList = "version"),
    @Index(name = "idx_tenant_config_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class TenantConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @NotBlank
    @Column(name = "configuration_key", nullable = false)
    private String configurationKey;

    @Column(name = "configuration_value", columnDefinition = "TEXT")
    private String configurationValue;

    @Column(name = "configuration_type")
    @Enumerated(EnumType.STRING)
    private ConfigurationType configurationType = ConfigurationType.STRING;

    @Column(name = "data_type")
    @Enumerated(EnumType.STRING)
    private DataType dataType = DataType.STRING;

    @Column(name = "is_encrypted")
    private boolean encrypted = false;

    @Column(name = "is_sensitive")
    private boolean sensitive = false;

    @Column(name = "is_feature_flag")
    private boolean featureFlag = false;

    @Column(name = "feature_flag_enabled")
    private boolean featureFlagEnabled = false;

    @Column(name = "environment")
    @Enumerated(EnumType.STRING)
    private Environment environment = Environment.PRODUCTION;

    @Column(name = "service_tier")
    @Enumerated(EnumType.STRING)
    private ServiceTier serviceTier = ServiceTier.STANDARD;

    @Column(name = "configuration_scope")
    @Enumerated(EnumType.STRING)
    private ConfigurationScope scope = ConfigurationScope.TENANT;

    @Column(name = "parent_configuration_id")
    private UUID parentConfigurationId;

    @Column(name = "version", nullable = false)
    private Long version = 1L;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "rollout_stage")
    @Enumerated(EnumType.STRING)
    private RolloutStage rolloutStage = RolloutStage.PRODUCTION;

    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage = 100;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "validation_rules", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> validationRules = new HashMap<>();

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Constructors
    public TenantConfiguration() {}

    public TenantConfiguration(String tenantId, String configurationKey, String configurationValue) {
        this.tenantId = tenantId;
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        return (effectiveFrom == null || now.isAfter(effectiveFrom)) &&
               (effectiveUntil == null || now.isBefore(effectiveUntil));
    }

    public boolean isInRolloutStage() {
        return rolloutStage == RolloutStage.PRODUCTION || 
               (rolloutStage == RolloutStage.CANARY && rolloutPercentage > 0);
    }

    public void incrementVersion() {
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    public void markForApproval(String changeReason) {
        this.approvalStatus = ApprovalStatus.PENDING;
        this.changeReason = changeReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String approvedBy) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.changeReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getConfigurationKey() { return configurationKey; }
    public void setConfigurationKey(String configurationKey) { this.configurationKey = configurationKey; }

    public String getConfigurationValue() { return configurationValue; }
    public void setConfigurationValue(String configurationValue) { this.configurationValue = configurationValue; }

    public ConfigurationType getConfigurationType() { return configurationType; }
    public void setConfigurationType(ConfigurationType configurationType) { this.configurationType = configurationType; }

    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

    public boolean isFeatureFlag() { return featureFlag; }
    public void setFeatureFlag(boolean featureFlag) { this.featureFlag = featureFlag; }

    public boolean isFeatureFlagEnabled() { return featureFlagEnabled; }
    public void setFeatureFlagEnabled(boolean featureFlagEnabled) { this.featureFlagEnabled = featureFlagEnabled; }

    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }

    public ServiceTier getServiceTier() { return serviceTier; }
    public void setServiceTier(ServiceTier serviceTier) { this.serviceTier = serviceTier; }

    public ConfigurationScope getScope() { return scope; }
    public void setScope(ConfigurationScope scope) { this.scope = scope; }

    public UUID getParentConfigurationId() { return parentConfigurationId; }
    public void setParentConfigurationId(UUID parentConfigurationId) { this.parentConfigurationId = parentConfigurationId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public RolloutStage getRolloutStage() { return rolloutStage; }
    public void setRolloutStage(RolloutStage rolloutStage) { this.rolloutStage = rolloutStage; }

    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }

    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDateTime getEffectiveUntil() { return effectiveUntil; }
    public void setEffectiveUntil(LocalDateTime effectiveUntil) { this.effectiveUntil = effectiveUntil; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Map<String, Object> getValidationRules() { return validationRules; }
    public void setValidationRules(Map<String, Object> validationRules) { this.validationRules = validationRules; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    @Override
    public String toString() {
        return "TenantConfiguration{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                ", configurationKey='" + configurationKey + '\'' +
                ", version=" + version +
                ", environment=" + environment +
                ", serviceTier=" + serviceTier +
                ", scope=" + scope +
                ", active=" + active +
                ", rolloutStage=" + rolloutStage +
                '}';
    }
}
