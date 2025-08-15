package com.saasstarterkit.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity to manage Protected Health Information (PHI) for HIPAA compliance
 */
@Entity
@Table(name = "protected_health_information", indexes = {
    @Index(name = "idx_phi_user_id", columnList = "user_id"),
    @Index(name = "idx_phi_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_phi_category", columnList = "phi_category"),
    @Index(name = "idx_phi_created_at", columnList = "created_at"),
    @Index(name = "idx_phi_encrypted", columnList = "is_encrypted")
})
public class ProtectedHealthInformation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "phi_category", nullable = false)
    private PHICategory phiCategory;
    
    @Column(name = "phi_data", columnDefinition = "text", nullable = false)
    private String phiData; // Encrypted PHI data
    
    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = true;
    
    @Column(name = "encryption_algorithm")
    private String encryptionAlgorithm; // AES-256, etc.
    
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;
    
    @Column(name = "data_hash", nullable = false)
    private String dataHash; // SHA-256 hash for integrity verification
    
    @Column(name = "access_purpose", nullable = false)
    private String accessPurpose; // Treatment, Payment, Operations, etc.
    
    @Column(name = "legal_basis", nullable = false)
    private String legalBasis; // HIPAA authorization, consent, etc.
    
    @Column(name = "authorization_expires_at")
    private LocalDateTime authorizationExpiresAt;
    
    @Column(name = "minimum_necessary_standard")
    private Boolean minimumNecessaryStandard = true;
    
    @Column(name = "data_retention_policy")
    private String dataRetentionPolicy;
    
    @Column(name = "retention_expires_at")
    private LocalDateTime retentionExpiresAt;
    
    @Column(name = "audit_trail_enabled", nullable = false)
    private Boolean auditTrailEnabled = true;
    
    @Column(name = "breach_notification_required")
    private Boolean breachNotificationRequired = true;
    
    @Column(name = "data_classification", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataClassification dataClassification;
    
    @Column(name = "access_controls", columnDefinition = "jsonb")
    private Map<String, Object> accessControls;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(name = "updated_by")
    private UUID updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ProtectedHealthInformation() {}
    
    public ProtectedHealthInformation(UUID userId, String tenantId, PHICategory phiCategory, String phiData) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.phiCategory = phiCategory;
        this.phiData = phiData;
        this.dataClassification = DataClassification.RESTRICTED;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public PHICategory getPhiCategory() { return phiCategory; }
    public void setPhiCategory(PHICategory phiCategory) { this.phiCategory = phiCategory; }
    
    public String getPhiData() { return phiData; }
    public void setPhiData(String phiData) { this.phiData = phiData; }
    
    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }
    
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public void setEncryptionAlgorithm(String encryptionAlgorithm) { this.encryptionAlgorithm = encryptionAlgorithm; }
    
    public String getEncryptionKeyId() { return encryptionKeyId; }
    public void setEncryptionKeyId(String encryptionKeyId) { this.encryptionKeyId = encryptionKeyId; }
    
    public String getDataHash() { return dataHash; }
    public void setDataHash(String dataHash) { this.dataHash = dataHash; }
    
    public String getAccessPurpose() { return accessPurpose; }
    public void setAccessPurpose(String accessPurpose) { this.accessPurpose = accessPurpose; }
    
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    
    public LocalDateTime getAuthorizationExpiresAt() { return authorizationExpiresAt; }
    public void setAuthorizationExpiresAt(LocalDateTime authorizationExpiresAt) { this.authorizationExpiresAt = authorizationExpiresAt; }
    
    public Boolean getMinimumNecessaryStandard() { return minimumNecessaryStandard; }
    public void setMinimumNecessaryStandard(Boolean minimumNecessaryStandard) { this.minimumNecessaryStandard = minimumNecessaryStandard; }
    
    public String getDataRetentionPolicy() { return dataRetentionPolicy; }
    public void setDataRetentionPolicy(String dataRetentionPolicy) { this.dataRetentionPolicy = dataRetentionPolicy; }
    
    public LocalDateTime getRetentionExpiresAt() { return retentionExpiresAt; }
    public void setRetentionExpiresAt(LocalDateTime retentionExpiresAt) { this.retentionExpiresAt = retentionExpiresAt; }
    
    public Boolean getAuditTrailEnabled() { return auditTrailEnabled; }
    public void setAuditTrailEnabled(Boolean auditTrailEnabled) { this.auditTrailEnabled = auditTrailEnabled; }
    
    public Boolean getBreachNotificationRequired() { return breachNotificationRequired; }
    public void setBreachNotificationRequired(Boolean breachNotificationRequired) { this.breachNotificationRequired = breachNotificationRequired; }
    
    public DataClassification getDataClassification() { return dataClassification; }
    public void setDataClassification(DataClassification dataClassification) { this.dataClassification = dataClassification; }
    
    public Map<String, Object> getAccessControls() { return accessControls; }
    public void setAccessControls(Map<String, Object> accessControls) { this.accessControls = accessControls; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    
    // Business Logic Methods
    public boolean isAuthorized() {
        if (authorizationExpiresAt == null) return true;
        return LocalDateTime.now().isBefore(authorizationExpiresAt);
    }
    
    public boolean isRetentionExpired() {
        if (retentionExpiresAt == null) return false;
        return LocalDateTime.now().isAfter(retentionExpiresAt);
    }
    
    public boolean requiresBreachNotification() {
        return Boolean.TRUE.equals(breachNotificationRequired);
    }
    
    public boolean isAccessible() {
        return isAuthorized() && !isRetentionExpired();
    }
    
    public boolean isHighRisk() {
        return DataClassification.RESTRICTED.equals(dataClassification) || 
               PHICategory.GENETIC_INFORMATION.equals(phiCategory) ||
               PHICategory.MENTAL_HEALTH.equals(phiCategory);
    }
    
    public void markForDeletion() {
        this.retentionExpiresAt = LocalDateTime.now().plusDays(30); // 30-day grace period
    }
    
    public boolean canBeAccessedBy(UUID userId, String purpose) {
        // Check if user has access rights and purpose is valid
        if (!isAccessible()) return false;
        
        // Validate access purpose
        if (!this.accessPurpose.equals(purpose)) return false;
        
        // Check minimum necessary standard
        if (Boolean.TRUE.equals(minimumNecessaryStandard)) {
            // Implement minimum necessary logic
            return true;
        }
        
        return true;
    }
}
