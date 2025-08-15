package com.saasstarterkit.billing.domain.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for cancelling a subscription
 */
public class CancelSubscriptionRequest {
    
    @NotNull(message = "Cancel at end of period flag is required")
    private Boolean cancelAtEndOfPeriod;
    
    private String reason;
    
    private String feedback;
    
    // Constructors
    public CancelSubscriptionRequest() {}
    
    public CancelSubscriptionRequest(Boolean cancelAtEndOfPeriod) {
        this.cancelAtEndOfPeriod = cancelAtEndOfPeriod;
    }
    
    // Getters and Setters
    public Boolean getCancelAtEndOfPeriod() { return cancelAtEndOfPeriod; }
    public void setCancelAtEndOfPeriod(Boolean cancelAtEndOfPeriod) { this.cancelAtEndOfPeriod = cancelAtEndOfPeriod; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
