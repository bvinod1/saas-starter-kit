package com.saasstarterkit.billing.domain.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for updating an existing subscription
 */
public class UpdateSubscriptionRequest {
    
    private String planId;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency;
    
    private Map<String, Object> metadata;
    
    private String customerEmail;
    
    private String customerName;
    
    private String paymentMethodId;
    
    private Boolean autoRenew;
    
    // Constructors
    public UpdateSubscriptionRequest() {}
    
    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
}
