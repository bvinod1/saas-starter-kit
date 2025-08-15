package com.saasstarterkit.billing.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a subscription plan with features, limits, and pricing
 */
@Entity
@Table(name = "plans", indexes = {
    @Index(name = "idx_plan_id", columnList = "plan_id"),
    @Index(name = "idx_active", columnList = "active"),
    @Index(name = "idx_tier", columnList = "tier")
})
public class Plan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "plan_id", nullable = false, unique = true)
    private String planId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private PlanTier tier;
    
    @Column(name = "active", nullable = false)
    private Boolean active;
    
    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "billing_cycle", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;
    
    @Column(name = "trial_days")
    private Integer trialDays;
    
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "max_storage_gb")
    private Integer maxStorageGB;
    
    @Column(name = "max_api_calls_per_month")
    private Long maxApiCallsPerMonth;
    
    @Column(name = "max_tenants")
    private Integer maxTenants;
    
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;
    
    @Column(name = "limits", columnDefinition = "jsonb")
    private Map<String, Object> limits;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Plan() {}
    
    public Plan(String planId, String name, PlanTier tier, BigDecimal basePrice) {
        this.planId = planId;
        this.name = name;
        this.tier = tier;
        this.basePrice = basePrice;
        this.currency = "USD";
        this.billingCycle = BillingCycle.MONTHLY;
        this.active = true;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public PlanTier getTier() { return tier; }
    public void setTier(PlanTier tier) { this.tier = tier; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    
    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
    
    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }
    
    public Integer getMaxStorageGB() { return maxStorageGB; }
    public void setMaxStorageGB(Integer maxStorageGB) { this.maxStorageGB = maxStorageGB; }
    
    public Long getMaxApiCallsPerMonth() { return maxApiCallsPerMonth; }
    public void setMaxApiCallsPerMonth(Long maxApiCallsPerMonth) { this.maxApiCallsPerMonth = maxApiCallsPerMonth; }
    
    public Integer getMaxTenants() { return maxTenants; }
    public void setMaxTenants(Integer maxTenants) { this.maxTenants = maxTenants; }
    
    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }
    
    public Map<String, Object> getLimits() { return limits; }
    public void setLimits(Map<String, Object> limits) { this.limits = limits; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business Logic Methods
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
    
    public boolean hasFeature(String featureName) {
        return features != null && features.containsKey(featureName) && 
               Boolean.TRUE.equals(features.get(featureName));
    }
    
    public boolean hasTrial() {
        return trialDays != null && trialDays > 0;
    }
    
    public boolean isUnlimited(String limitName) {
        return limits != null && limits.containsKey(limitName) && 
               "unlimited".equals(limits.get(limitName));
    }
    
    public Long getLimit(String limitName) {
        if (limits == null || !limits.containsKey(limitName)) {
            return null;
        }
        Object value = limits.get(limitName);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
