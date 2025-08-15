package com.saasstarterkit.organisation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationCreateRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Organization slug is required")
    @Size(min = 2, max = 100, message = "Organization slug must be between 2 and 100 characters")
    private String slug;

    private String domain;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private String websiteUrl;
    private String tier = "STARTER";

    // Constructors
    public OrganizationCreateRequest() {}

    public OrganizationCreateRequest(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    // Getters and Setters
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

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    @Override
    public String toString() {
        return "OrganizationCreateRequest{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", domain='" + domain + '\'' +
                ", tier='" + tier + '\'' +
                '}';
    }
}
