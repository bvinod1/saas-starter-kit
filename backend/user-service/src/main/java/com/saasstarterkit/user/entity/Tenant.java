package com.saasstarterkit.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "tenant_management")
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;
    
    @Column(name = "domain")
    private String domain;
    
    @Column(name = "keycloak_realm_id", unique = true)
    private String keycloakRealmId;
    
    @Column(name = "keycloak_client_id")
    private String keycloakClientId;
    
    @Column(name = "status")
    private String status = "active";
    
    @Column(name = "subscription_plan_id")
    private UUID subscriptionPlanId;
    
    @Column(name = "subscription_status")
    private String subscriptionStatus = "trial";
    
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;
    
    @Column(name = "max_users")
    private Integer maxUsers = 10;
    
    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings = "{}";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Ensure settings is valid JSON
        if (settings == null || settings.trim().isEmpty()) {
            settings = "{}";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getKeycloakRealmId() {
        return keycloakRealmId;
    }
    
    public void setKeycloakRealmId(String keycloakRealmId) {
        this.keycloakRealmId = keycloakRealmId;
    }
    
    public String getKeycloakClientId() {
        return keycloakClientId;
    }
    
    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getSubscriptionPlanId() {
        return subscriptionPlanId;
    }
    
    public void setSubscriptionPlanId(UUID subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }
    
    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }
    
    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
    
    public LocalDateTime getTrialEndsAt() {
        return trialEndsAt;
    }
    
    public void setTrialEndsAt(LocalDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }
    
    public Integer getMaxUsers() {
        return maxUsers;
    }
    
    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }
    
    public String getSettings() {
        return settings;
    }
    
    public void setSettings(String settings) {
        this.settings = settings;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
