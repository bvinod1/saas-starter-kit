package com.saasstarterkit.organisation.dto;

import com.saasstarterkit.organisation.domain.Organization;
import com.saasstarterkit.organisation.domain.OrganizationStatus;
import com.saasstarterkit.organisation.domain.OrganizationTier;
import com.saasstarterkit.organisation.domain.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrganizationResponse {
    private UUID id;
    private String name;
    private String slug;
    private String domain;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private String websiteUrl;
    private OrganizationTier tier;
    private OrganizationStatus status;
    private SubscriptionStatus subscriptionStatus;
    private boolean onboardingCompleted;
    private String onboardingStep;
    private LocalDateTime trialEndsAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int maxUsers;
    private int currentUserCount;

    // Default constructor
    public OrganizationResponse() {}

    // Constructor from Organization entity
    public OrganizationResponse(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.slug = organization.getSlug();
        this.domain = organization.getDomain();
        this.contactEmail = organization.getContactEmail();
        this.contactPhone = organization.getContactPhone();
        this.logoUrl = organization.getLogoUrl();
        this.websiteUrl = organization.getWebsiteUrl();
        this.tier = organization.getTier();
        this.status = organization.getStatus();
        this.subscriptionStatus = organization.getSubscriptionStatus();
        this.onboardingCompleted = organization.getOnboardingCompleted();
        this.onboardingStep = organization.getOnboardingStep();
        this.trialEndsAt = organization.getTrialEndsAt();
        this.createdAt = organization.getCreatedAt();
        this.updatedAt = organization.getUpdatedAt();
        this.maxUsers = organization.getMaxUsers();
        this.currentUserCount = organization.getCurrentUsers();
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

    public OrganizationTier getTier() { return tier; }
    public void setTier(OrganizationTier tier) { this.tier = tier; }

    public OrganizationStatus getStatus() { return status; }
    public void setStatus(OrganizationStatus status) { this.status = status; }

    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }

    public String getOnboardingStep() { return onboardingStep; }
    public void setOnboardingStep(String onboardingStep) { this.onboardingStep = onboardingStep; }

    public LocalDateTime getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(LocalDateTime trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }

    public int getCurrentUserCount() { return currentUserCount; }
    public void setCurrentUserCount(int currentUserCount) { this.currentUserCount = currentUserCount; }
}
