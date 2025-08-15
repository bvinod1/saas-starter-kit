package com.saasstarterkit.billing.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for creating a checkout session
 */
public class CreateCheckoutSessionRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotBlank(message = "Plan ID is required")
    private String planId;
    
    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency = "USD";
    
    private String couponCode;
    
    private Map<String, Object> metadata;
    
    private String customerEmail;
    
    private String customerName;
    
    private String successUrl;
    
    private String cancelUrl;
    
    private String returnUrl;
    
    private Boolean allowPromoCode = true;
    
    private Boolean collectBillingAddress = true;
    
    private Boolean collectShippingAddress = false;
    
    private Integer trialDays;
    
    // Constructors
    public CreateCheckoutSessionRequest() {}
    
    public CreateCheckoutSessionRequest(String tenantId, String planId, BillingCycle billingCycle) {
        this.tenantId = tenantId;
        this.planId = planId;
        this.billingCycle = billingCycle;
    }
    
    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
    
    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    
    public Boolean getAllowPromoCode() { return allowPromoCode; }
    public void setAllowPromoCode(Boolean allowPromoCode) { this.allowPromoCode = allowPromoCode; }
    
    public Boolean getCollectBillingAddress() { return collectBillingAddress; }
    public void setCollectBillingAddress(Boolean collectBillingAddress) { this.collectBillingAddress = collectBillingAddress; }
    
    public Boolean getCollectShippingAddress() { return collectShippingAddress; }
    public void setCollectShippingAddress(Boolean collectShippingAddress) { this.collectShippingAddress = collectShippingAddress; }
    
    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
}
