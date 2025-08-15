package com.saasstarterkit.billing.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core subscription entity that maps tenant subscriptions to external subscription management systems
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_external_subscription_id", columnList = "external_subscription_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "external_subscription_id", nullable = false, unique = true)
    private String externalSubscriptionId;
    
    @Column(name = "plan_id", nullable = false)
    private String planId;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;
    
    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;
    
    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;
    
    @Column(name = "trial_start")
    private LocalDateTime trialStart;
    
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "max_storage_gb")
    private Integer maxStorageGB;
    
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "provider", nullable = false)
    private String provider;
    
    @Column(name = "sync_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus;
    
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (syncStatus == null) {
            syncStatus = SyncStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Subscription() {}
    
    public Subscription(String tenantId, String externalSubscriptionId, String planId, String planName) {
        this.tenantId = tenantId;
        this.externalSubscriptionId = externalSubscriptionId;
        this.planId = planId;
        this.planName = planName;
        this.status = SubscriptionStatus.ACTIVE;
        this.billingCycle = BillingCycle.MONTHLY;
        this.quantity = 1;
        this.currency = "USD";
        this.provider = "zoho";
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getExternalSubscriptionId() { return externalSubscriptionId; }
    public void setExternalSubscriptionId(String externalSubscriptionId) { this.externalSubscriptionId = externalSubscriptionId; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    
    public LocalDateTime getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    
    public LocalDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    
    public LocalDateTime getTrialStart() { return trialStart; }
    public void setTrialStart(LocalDateTime trialStart) { this.trialStart = trialStart; }
    
    public LocalDateTime getTrialEnd() { return trialEnd; }
    public void setTrialEnd(LocalDateTime trialEnd) { this.trialEnd = trialEnd; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }
    
    public Integer getMaxStorageGB() { return maxStorageGB; }
    public void setMaxStorageGB(Integer maxStorageGB) { this.maxStorageGB = maxStorageGB; }
    
    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public LocalDateTime getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(LocalDateTime nextBillingDate) { this.nextBillingDate = nextBillingDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    
    // Business Logic Methods
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(status);
    }
    
    public boolean isTrial() {
        return trialStart != null && trialEnd != null && 
               LocalDateTime.now().isAfter(trialStart) && LocalDateTime.now().isBefore(trialEnd);
    }
    
    public boolean isExpired() {
        return currentPeriodEnd != null && LocalDateTime.now().isAfter(currentPeriodEnd);
    }
    
    public boolean isCancelled() {
        return SubscriptionStatus.CANCELLED.equals(status) || cancelledAt != null;
    }
    
    public boolean hasFeature(String featureName) {
        return features != null && features.containsKey(featureName) && 
               Boolean.TRUE.equals(features.get(featureName));
    }
    
    public boolean canAddUser() {
        return maxUsers == null || quantity < maxUsers;
    }
    
    public boolean canUseStorage(long storageGB) {
        return maxStorageGB == null || storageGB <= maxStorageGB;
    }
}
