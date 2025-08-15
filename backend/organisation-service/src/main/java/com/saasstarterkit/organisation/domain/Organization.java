package com.saasstarterkit.organisation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organizations", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Organization slug is required")
    @Size(min = 2, max = 100, message = "Organization slug must be between 2 and 100 characters")
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Size(max = 255, message = "Domain must not exceed 255 characters")
    @Column(name = "domain")
    private String domain;

    @Email(message = "Contact email must be a valid email address")
    @Column(name = "contact_email")
    private String contactEmail;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private OrganizationTier tier = OrganizationTier.STARTER;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    @Column(name = "subscription_plan_id")
    private UUID subscriptionPlanId;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "current_users")
    private Integer currentUsers = 0;

    @Column(name = "storage_limit_gb")
    private Integer storageLimitGB;

    @Column(name = "current_storage_gb")
    private Integer currentStorageGB = 0;

    @Column(name = "api_rate_limit")
    private Integer apiRateLimit;

    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings;

    @Column(name = "compliance_settings", columnDefinition = "jsonb")
    private String complianceSettings;

    @Column(name = "billing_info", columnDefinition = "jsonb")
    private String billingInfo;

    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted = false;

    @Column(name = "onboarding_step")
    private String onboardingStep;

    @Column(name = "notes")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrganizationMember> members = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrganizationAuditLog> auditLogs = new HashSet<>();

    // Constructors
    public Organization() {}

    public Organization(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.trialEndsAt = LocalDateTime.now().plusDays(14); // Default 14-day trial
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public OrganizationStatus getStatus() { return status; }
    public void setStatus(OrganizationStatus status) { this.status = status; }

    public OrganizationTier getTier() { return tier; }
    public void setTier(OrganizationTier tier) { this.tier = tier; }

    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public UUID getSubscriptionPlanId() { return subscriptionPlanId; }
    public void setSubscriptionPlanId(UUID subscriptionPlanId) { this.subscriptionPlanId = subscriptionPlanId; }

    public LocalDateTime getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(LocalDateTime trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public LocalDateTime getSubscriptionEndsAt() { return subscriptionEndsAt; }
    public void setSubscriptionEndsAt(LocalDateTime subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }

    public Integer getCurrentUsers() { return currentUsers; }
    public void setCurrentUsers(Integer currentUsers) { this.currentUsers = currentUsers; }

    public Integer getStorageLimitGB() { return storageLimitGB; }
    public void setStorageLimitGB(Integer storageLimitGB) { this.storageLimitGB = storageLimitGB; }

    public Integer getCurrentStorageGB() { return currentStorageGB; }
    public void setCurrentStorageGB(Integer currentStorageGB) { this.currentStorageGB = currentStorageGB; }

    public Integer getApiRateLimit() { return apiRateLimit; }
    public void setApiRateLimit(Integer apiRateLimit) { this.apiRateLimit = apiRateLimit; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }

    public String getComplianceSettings() { return complianceSettings; }
    public void setComplianceSettings(String complianceSettings) { this.complianceSettings = complianceSettings; }

    public String getBillingInfo() { return billingInfo; }
    public void setBillingInfo(String billingInfo) { this.billingInfo = billingInfo; }

    public Boolean getOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(Boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }

    public String getOnboardingStep() { return onboardingStep; }
    public void setOnboardingStep(String onboardingStep) { this.onboardingStep = onboardingStep; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Set<OrganizationMember> getMembers() { return members; }
    public void setMembers(Set<OrganizationMember> members) { this.members = members; }

    public Set<OrganizationAuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(Set<OrganizationAuditLog> auditLogs) { this.auditLogs = auditLogs; }

    // Business Methods
    public boolean isActive() {
        return OrganizationStatus.ACTIVE.equals(this.status);
    }

    public boolean isTrialExpired() {
        return this.trialEndsAt != null && LocalDateTime.now().isAfter(this.trialEndsAt);
    }

    public boolean isSubscriptionExpired() {
        return this.subscriptionEndsAt != null && LocalDateTime.now().isAfter(this.subscriptionEndsAt);
    }

    public boolean canAddUser() {
        return this.maxUsers == null || this.currentUsers < this.maxUsers;
    }

    public boolean canUseStorage(int additionalGB) {
        return this.storageLimitGB == null || (this.currentStorageGB + additionalGB) <= this.storageLimitGB;
    }

    public void incrementUserCount() {
        if (this.currentUsers == null) this.currentUsers = 0;
        this.currentUsers++;
    }

    public void decrementUserCount() {
        if (this.currentUsers != null && this.currentUsers > 0) {
            this.currentUsers--;
        }
    }

    public void addStorage(int gb) {
        if (this.currentStorageGB == null) this.currentStorageGB = 0;
        this.currentStorageGB += gb;
    }

    public void removeStorage(int gb) {
        if (this.currentStorageGB != null && this.currentStorageGB >= gb) {
            this.currentStorageGB -= gb;
        }
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", status=" + status +
                ", tier=" + tier +
                ", subscriptionStatus=" + subscriptionStatus +
                '}';
    }
}
