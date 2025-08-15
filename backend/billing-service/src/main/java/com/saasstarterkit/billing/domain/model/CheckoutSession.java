package com.saasstarterkit.billing.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a hosted checkout session for subscription creation
 */
@Entity
@Table(name = "checkout_sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class CheckoutSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "plan_id", nullable = false)
    private String planId;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckoutStatus status;
    
    @Column(name = "checkout_url", nullable = false)
    private String checkoutUrl;
    
    @Column(name = "success_url")
    private String successUrl;
    
    @Column(name = "cancel_url")
    private String cancelUrl;
    
    @Column(name = "return_url")
    private String returnUrl;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private java.math.BigDecimal amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "billing_cycle", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;
    
    @Column(name = "trial_days")
    private Integer trialDays;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "provider", nullable = false)
    private String provider;
    
    @Column(name = "external_session_id")
    private String externalSessionId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CheckoutStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public CheckoutSession() {}
    
    public CheckoutSession(String sessionId, String tenantId, String planId) {
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.planId = planId;
        this.status = CheckoutStatus.PENDING;
        this.currency = "USD";
        this.quantity = 1;
        this.provider = "zoho";
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public CheckoutStatus getStatus() { return status; }
    public void setStatus(CheckoutStatus status) { this.status = status; }
    
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    
    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
    
    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    
    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getExternalSessionId() { return externalSessionId; }
    public void setExternalSessionId(String externalSessionId) { this.externalSessionId = externalSessionId; }
    
    // Business Logic Methods
    public boolean isPending() {
        return CheckoutStatus.PENDING.equals(status);
    }
    
    public boolean isCompleted() {
        return CheckoutStatus.COMPLETED.equals(status);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canBeCompleted() {
        return isPending() && !isExpired();
    }
    
    public void markCompleted() {
        this.status = CheckoutStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markExpired() {
        this.status = CheckoutStatus.EXPIRED;
    }
}
