package com.saasstarterkit.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity to track user consent for data processing activities (GDPR compliance)
 */
@Entity
@Table(name = "data_consents", indexes = {
    @Index(name = "idx_consent_user_id", columnList = "user_id"),
    @Index(name = "idx_consent_type", columnList = "consent_type"),
    @Index(name = "idx_consent_status", columnList = "status"),
    @Index(name = "idx_consent_created_at", columnList = "created_at")
})
public class DataConsent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false)
    private ConsentType consentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;
    
    @Column(name = "consent_text", nullable = false)
    private String consentText;
    
    @Column(name = "consent_version", nullable = false)
    private String consentVersion;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;
    
    @Column(name = "consent_withdrawn_at")
    private LocalDateTime consentWithdrawnAt;
    
    @Column(name = "consent_expires_at")
    private LocalDateTime consentExpiresAt;
    
    @Column(name = "legal_basis", nullable = false)
    private String legalBasis; // GDPR Article 6 legal basis
    
    @Column(name = "purpose", nullable = false)
    private String purpose;
    
    @Column(name = "data_categories", columnDefinition = "jsonb")
    private Map<String, Object> dataCategories;
    
    @Column(name = "third_party_sharing")
    private Boolean thirdPartySharing;
    
    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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
    public DataConsent() {}
    
    public DataConsent(UUID userId, String tenantId, ConsentType consentType, String consentText) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.consentType = consentType;
        this.consentText = consentText;
        this.status = ConsentStatus.PENDING;
        this.consentVersion = "1.0";
        this.legalBasis = "consent";
        this.purpose = "Service provision";
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public ConsentType getConsentType() { return consentType; }
    public void setConsentType(ConsentType consentType) { this.consentType = consentType; }
    
    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }
    
    public String getConsentText() { return consentText; }
    public void setConsentText(String consentText) { this.consentText = consentText; }
    
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }
    
    public LocalDateTime getConsentWithdrawnAt() { return consentWithdrawnAt; }
    public void setConsentWithdrawnAt(LocalDateTime consentWithdrawnAt) { this.consentWithdrawnAt = consentWithdrawnAt; }
    
    public LocalDateTime getConsentExpiresAt() { return consentExpiresAt; }
    public void setConsentExpiresAt(LocalDateTime consentExpiresAt) { this.consentExpiresAt = consentExpiresAt; }
    
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public Map<String, Object> getDataCategories() { return dataCategories; }
    public void setDataCategories(Map<String, Object> dataCategories) { this.dataCategories = dataCategories; }
    
    public Boolean getThirdPartySharing() { return thirdPartySharing; }
    public void setThirdPartySharing(Boolean thirdPartySharing) { this.thirdPartySharing = thirdPartySharing; }
    
    public Integer getRetentionPeriodDays() { return retentionPeriodDays; }
    public void setRetentionPeriodDays(Integer retentionPeriodDays) { this.retentionPeriodDays = retentionPeriodDays; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business Logic Methods
    public boolean isActive() {
        return ConsentStatus.ACTIVE.equals(status);
    }
    
    public boolean isExpired() {
        return consentExpiresAt != null && LocalDateTime.now().isAfter(consentExpiresAt);
    }
    
    public boolean canWithdraw() {
        return isActive() && !isExpired();
    }
    
    public void giveConsent() {
        this.status = ConsentStatus.ACTIVE;
        this.consentGivenAt = LocalDateTime.now();
    }
    
    public void withdrawConsent() {
        this.status = ConsentStatus.WITHDRAWN;
        this.consentWithdrawnAt = LocalDateTime.now();
    }
    
    public boolean requiresRenewal() {
        if (consentExpiresAt == null) return false;
        LocalDateTime renewalThreshold = consentExpiresAt.minusDays(30); // 30 days before expiry
        return LocalDateTime.now().isAfter(renewalThreshold);
    }
}
