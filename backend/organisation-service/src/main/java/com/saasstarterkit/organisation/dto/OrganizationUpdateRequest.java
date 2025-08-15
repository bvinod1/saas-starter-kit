package com.saasstarterkit.organisation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class OrganizationUpdateRequest {
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String name;
    
    @Size(min = 2, max = 100, message = "Domain must be between 2 and 100 characters")
    private String domain;
    
    @Email(message = "Email should be valid")
    private String contactEmail;
    
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String contactPhone;
    
    @Size(max = 500, message = "Logo URL must be less than 500 characters")
    private String logoUrl;
    
    @Size(max = 500, message = "Website URL must be less than 500 characters")
    private String websiteUrl;
    
    private String tier;
    
    private String status;
    
    private String subscriptionStatus;

    // Default constructor
    public OrganizationUpdateRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
}
