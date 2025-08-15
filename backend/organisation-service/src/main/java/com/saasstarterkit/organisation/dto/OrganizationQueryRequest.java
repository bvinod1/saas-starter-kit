package com.saasstarterkit.organizationalmanagement.dto;

import java.util.List;

public class OrganizationQueryRequest {
    private String name;
    private String slug;
    private String domain;
    private String status;
    private String tier;
    private String subscriptionStatus;
    private Boolean onboardingCompleted;
    private List<String> tags;
    private String searchTerm;

    // Constructors
    public OrganizationQueryRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public Boolean getOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(Boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }

    @Override
    public String toString() {
        return "OrganizationQueryRequest{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", status='" + status + '\'' +
                ", tier='" + tier + '\'' +
                ", searchTerm='" + searchTerm + '\'' +
                '}';
    }
}
