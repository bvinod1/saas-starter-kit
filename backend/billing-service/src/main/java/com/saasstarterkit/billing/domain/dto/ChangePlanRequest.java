package com.saasstarterkit.billing.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * DTO for changing a subscription plan
 */
public class ChangePlanRequest {
    
    @NotBlank(message = "New plan ID is required")
    private String newPlanId;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;
    
    @NotNull(message = "Effective date is required")
    private LocalDateTime effectiveDate;
    
    private Boolean prorate = true;
    
    private String reason;
    
    // Constructors
    public ChangePlanRequest() {}
    
    public ChangePlanRequest(String newPlanId, LocalDateTime effectiveDate) {
        this.newPlanId = newPlanId;
        this.effectiveDate = effectiveDate;
    }
    
    // Getters and Setters
    public String getNewPlanId() { return newPlanId; }
    public void setNewPlanId(String newPlanId) { this.newPlanId = newPlanId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public Boolean getProrate() { return prorate; }
    public void setProrate(Boolean prorate) { this.prorate = prorate; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
