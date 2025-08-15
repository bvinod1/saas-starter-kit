package com.saasstarterkit.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced User entity with GDPR and HIPAA compliance features
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_data_classification", columnList = "data_classification"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Column(name = "national_id")
    private String nationalId; // SSN, passport, etc.
    
    @Column(name = "address", columnDefinition = "jsonb")
    private Map<String, Object> address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_classification", nullable = false)
    private DataClassification dataClassification = DataClassification.PERSONAL;
    
    // GDPR Compliance Fields
    @Column(name = "gdpr_consent_given")
    private Boolean gdprConsentGiven = false;
    
    @Column(name = "gdpr_consent_date")
    private LocalDateTime gdprConsentDate;
    
    @Column(name = "gdpr_consent_version")
    private String gdprConsentVersion;
    
    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;
    
    @Column(name = "analytics_consent")
    private Boolean analyticsConsent = false;
    
    @Column(name = "third_party_sharing_consent")
    private Boolean thirdPartySharingConsent = false;
    
    @Column(name = "data_retention_consent")
    private Boolean dataRetentionConsent = false;
    
    @Column(name = "data_portability_consent")
    private Boolean dataPortabilityConsent = false;
    
    @Column(name = "automated_decision_consent")
    private Boolean automatedDecisionConsent = false;
    
    // HIPAA Compliance Fields
    @Column(name = "hipaa_authorization_given")
    private Boolean hipaaAuthorizationGiven = false;
    
    @Column(name = "hipaa_authorization_date")
    private LocalDateTime hipaaAuthorizationDate;
    
    @Column(name = "hipaa_authorization_expires")
    private LocalDateTime hipaaAuthorizationExpires;
    
    @Column(name = "hipaa_authorization_version")
    private String hipaaAuthorizationVersion;
    
    @Column(name = "treatment_consent")
    private Boolean treatmentConsent = false;
    
    @Column(name = "payment_consent")
    private Boolean paymentConsent = false;
    
    @Column(name = "operations_consent")
    private Boolean operationsConsent = false;
    
    @Column(name = "research_consent")
    private Boolean researchConsent = false;
    
    @Column(name = "marketing_health_consent")
    private Boolean marketingHealthConsent = false;
    
    // Privacy Controls
    @Column(name = "privacy_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private PrivacyLevel privacyLevel = PrivacyLevel.STANDARD;
    
    @Column(name = "data_anonymization_enabled")
    private Boolean dataAnonymizationEnabled = false;
    
    @Column(name = "pseudonymization_enabled")
    private Boolean pseudonymizationEnabled = false;
    
    @Column(name = "data_encryption_enabled")
    private Boolean dataEncryptionEnabled = true;
    
    @Column(name = "audit_logging_enabled")
    private Boolean auditLoggingEnabled = true;
    
    // Data Retention
    @Column(name = "data_retention_policy")
    private String dataRetentionPolicy;
    
    @Column(name = "retention_expires_at")
    private LocalDateTime retentionExpiresAt;
    
    @Column(name = "auto_deletion_enabled")
    private Boolean autoDeletionEnabled = false;
    
    @Column(name = "deletion_date")
    private LocalDateTime deletionDate;
    
    // Access Controls
    @Column(name = "access_controls", columnDefinition = "jsonb")
    private Map<String, Object> accessControls;
    
    @Column(name = "role_based_access")
    private Boolean roleBasedAccess = true;
    
    @Column(name = "multi_factor_required")
    private Boolean multiFactorRequired = false;
    
    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes = 30;
    
    // Audit Fields
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
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
    public User() {}
    
    public User(String tenantId, String email, String firstName, String lastName) {
        this.tenantId = tenantId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = UserStatus.ACTIVE;
        this.dataClassification = DataClassification.PERSONAL;
        this.privacyLevel = PrivacyLevel.STANDARD;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    
    public Map<String, Object> getAddress() { return address; }
    public void setAddress(Map<String, Object> address) { this.address = address; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public DataClassification getDataClassification() { return dataClassification; }
    public void setDataClassification(DataClassification dataClassification) { this.dataClassification = dataClassification; }
    
    // GDPR Consent Getters and Setters
    public Boolean getGdprConsentGiven() { return gdprConsentGiven; }
    public void setGdprConsentGiven(Boolean gdprConsentGiven) { this.gdprConsentGiven = gdprConsentGiven; }
    
    public LocalDateTime getGdprConsentDate() { return gdprConsentDate; }
    public void setGdprConsentDate(LocalDateTime gdprConsentDate) { this.gdprConsentDate = gdprConsentDate; }
    
    public String getGdprConsentVersion() { return gdprConsentVersion; }
    public void setGdprConsentVersion(String gdprConsentVersion) { this.gdprConsentVersion = gdprConsentVersion; }
    
    public Boolean getMarketingConsent() { return marketingConsent; }
    public void setMarketingConsent(Boolean marketingConsent) { this.marketingConsent = marketingConsent; }
    
    public Boolean getAnalyticsConsent() { return analyticsConsent; }
    public void setAnalyticsConsent(Boolean analyticsConsent) { this.analyticsConsent = analyticsConsent; }
    
    public Boolean getThirdPartySharingConsent() { return thirdPartySharingConsent; }
    public void setThirdPartySharingConsent(Boolean thirdPartySharingConsent) { this.thirdPartySharingConsent = thirdPartySharingConsent; }
    
    public Boolean getDataRetentionConsent() { return dataRetentionConsent; }
    public void setDataRetentionConsent(Boolean dataRetentionConsent) { this.dataRetentionConsent = dataRetentionConsent; }
    
    public Boolean getDataPortabilityConsent() { return dataPortabilityConsent; }
    public void setDataPortabilityConsent(Boolean dataPortabilityConsent) { this.dataPortabilityConsent = dataPortabilityConsent; }
    
    public Boolean getAutomatedDecisionConsent() { return automatedDecisionConsent; }
    public void setAutomatedDecisionConsent(Boolean automatedDecisionConsent) { this.automatedDecisionConsent = automatedDecisionConsent; }
    
    // HIPAA Authorization Getters and Setters
    public Boolean getHipaaAuthorizationGiven() { return hipaaAuthorizationGiven; }
    public void setHipaaAuthorizationGiven(Boolean hipaaAuthorizationGiven) { this.hipaaAuthorizationGiven = hipaaAuthorizationGiven; }
    
    public LocalDateTime getHipaaAuthorizationDate() { return hipaaAuthorizationDate; }
    public void setHipaaAuthorizationDate(LocalDateTime hipaaAuthorizationDate) { this.hipaaAuthorizationDate = hipaaAuthorizationDate; }
    
    public LocalDateTime getHipaaAuthorizationExpires() { return hipaaAuthorizationExpires; }
    public void setHipaaAuthorizationExpires(LocalDateTime hipaaAuthorizationExpires) { this.hipaaAuthorizationExpires = hipaaAuthorizationExpires; }
    
    public String getHipaaAuthorizationVersion() { return hipaaAuthorizationVersion; }
    public void setHipaaAuthorizationVersion(String hipaaAuthorizationVersion) { this.hipaaAuthorizationVersion = hipaaAuthorizationVersion; }
    
    public Boolean getTreatmentConsent() { return treatmentConsent; }
    public void setTreatmentConsent(Boolean treatmentConsent) { this.treatmentConsent = treatmentConsent; }
    
    public Boolean getPaymentConsent() { return paymentConsent; }
    public void setPaymentConsent(Boolean paymentConsent) { this.paymentConsent = paymentConsent; }
    
    public Boolean getOperationsConsent() { return operationsConsent; }
    public void setOperationsConsent(Boolean operationsConsent) { this.operationsConsent = operationsConsent; }
    
    public Boolean getResearchConsent() { return researchConsent; }
    public void setResearchConsent(Boolean researchConsent) { this.researchConsent = researchConsent; }
    
    public Boolean getMarketingHealthConsent() { return marketingHealthConsent; }
    public void setMarketingHealthConsent(Boolean marketingHealthConsent) { this.marketingHealthConsent = marketingHealthConsent; }
    
    // Privacy Controls Getters and Setters
    public PrivacyLevel getPrivacyLevel() { return privacyLevel; }
    public void setPrivacyLevel(PrivacyLevel privacyLevel) { this.privacyLevel = privacyLevel; }
    
    public Boolean getDataAnonymizationEnabled() { return dataAnonymizationEnabled; }
    public void setDataAnonymizationEnabled(Boolean dataAnonymizationEnabled) { this.dataAnonymizationEnabled = dataAnonymizationEnabled; }
    
    public Boolean getPseudonymizationEnabled() { return pseudonymizationEnabled; }
    public void setPseudonymizationEnabled(Boolean pseudonymizationEnabled) { this.pseudonymizationEnabled = pseudonymizationEnabled; }
    
    public Boolean getDataEncryptionEnabled() { return dataEncryptionEnabled; }
    public void setDataEncryptionEnabled(Boolean dataEncryptionEnabled) { this.dataEncryptionEnabled = dataEncryptionEnabled; }
    
    public Boolean getAuditLoggingEnabled() { return auditLoggingEnabled; }
    public void setAuditLoggingEnabled(Boolean auditLoggingEnabled) { this.auditLoggingEnabled = auditLoggingEnabled; }
    
    // Data Retention Getters and Setters
    public String getDataRetentionPolicy() { return dataRetentionPolicy; }
    public void setDataRetentionPolicy(String dataRetentionPolicy) { this.dataRetentionPolicy = dataRetentionPolicy; }
    
    public LocalDateTime getRetentionExpiresAt() { return retentionExpiresAt; }
    public void setRetentionExpiresAt(LocalDateTime retentionExpiresAt) { this.retentionExpiresAt = retentionExpiresAt; }
    
    public Boolean getAutoDeletionEnabled() { return autoDeletionEnabled; }
    public void setAutoDeletionEnabled(Boolean autoDeletionEnabled) { this.autoDeletionEnabled = autoDeletionEnabled; }
    
    public LocalDateTime getDeletionDate() { return deletionDate; }
    public void setDeletionDate(LocalDateTime deletionDate) { this.deletionDate = deletionDate; }
    
    // Access Controls Getters and Setters
    public Map<String, Object> getAccessControls() { return accessControls; }
    public void setAccessControls(Map<String, Object> accessControls) { this.accessControls = accessControls; }
    
    public Boolean getRoleBasedAccess() { return roleBasedAccess; }
    public void setRoleBasedAccess(Boolean roleBasedAccess) { this.roleBasedAccess = roleBasedAccess; }
    
    public Boolean getMultiFactorRequired() { return multiFactorRequired; }
    public void setMultiFactorRequired(Boolean multiFactorRequired) { this.multiFactorRequired = multiFactorRequired; }
    
    public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
    
    // Audit Fields Getters and Setters
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public LocalDateTime getLastPasswordChange() { return lastPasswordChange; }
    public void setLastPasswordChange(LocalDateTime lastPasswordChange) { this.lastPasswordChange = lastPasswordChange; }
    
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public LocalDateTime getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }
    
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
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }
    
    public boolean isLocked() {
        return accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil);
    }
    
    public boolean hasGdprConsent() {
        return Boolean.TRUE.equals(gdprConsentGiven);
    }
    
    public boolean hasHipaaAuthorization() {
        if (!Boolean.TRUE.equals(hipaaAuthorizationGiven)) return false;
        if (hipaaAuthorizationExpires == null) return true;
        return LocalDateTime.now().isBefore(hipaaAuthorizationExpires);
    }
    
    public boolean requiresMultiFactor() {
        return Boolean.TRUE.equals(multiFactorRequired) || 
               DataClassification.SecurityLevel.HIGH.equals(dataClassification.getMinimumSecurityLevel()) ||
               DataClassification.SecurityLevel.MAXIMUM.equals(dataClassification.getMinimumSecurityLevel());
    }
    
    public boolean canAccessHealthData() {
        return hasHipaaAuthorization() && 
               (Boolean.TRUE.equals(treatmentConsent) || Boolean.TRUE.equals(paymentConsent));
    }
    
    public boolean canShareForMarketing() {
        return hasGdprConsent() && Boolean.TRUE.equals(marketingConsent);
    }
    
    public boolean canShareForAnalytics() {
        return hasGdprConsent() && Boolean.TRUE.equals(analyticsConsent);
    }
    
    public boolean canShareWithThirdParties() {
        return hasGdprConsent() && Boolean.TRUE.equals(thirdPartySharingConsent);
    }
    
    public boolean isDataRetentionExpired() {
        if (retentionExpiresAt == null) return false;
        return LocalDateTime.now().isAfter(retentionExpiresAt);
    }
    
    public void giveGdprConsent(String version) {
        this.gdprConsentGiven = true;
        this.gdprConsentDate = LocalDateTime.now();
        this.gdprConsentVersion = version;
    }
    
    public void withdrawGdprConsent() {
        this.gdprConsentGiven = false;
        this.gdprConsentDate = null;
        this.gdprConsentVersion = null;
    }
    
    public void giveHipaaAuthorization(String version, LocalDateTime expiresAt) {
        this.hipaaAuthorizationGiven = true;
        this.hipaaAuthorizationDate = LocalDateTime.now();
        this.hipaaAuthorizationVersion = version;
        this.hipaaAuthorizationExpires = expiresAt;
    }
    
    public void withdrawHipaaAuthorization() {
        this.hipaaAuthorizationGiven = false;
        this.hipaaAuthorizationDate = null;
        this.hipaaAuthorizationVersion = null;
        this.hipaaAuthorizationExpires = null;
    }
    
    public void markForDeletion() {
        this.deletionDate = LocalDateTime.now().plusDays(30); // 30-day grace period
    }
    
    public boolean isMarkedForDeletion() {
        return deletionDate != null && LocalDateTime.now().isAfter(deletionDate);
    }
}
