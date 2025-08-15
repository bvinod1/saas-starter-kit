package com.saasstarterkit.user.domain.model;

/**
 * Status of data subject rights requests
 */
public enum DataSubjectRequestStatus {
    /**
     * Request has been submitted and is pending review
     */
    PENDING("Pending", "Request submitted and awaiting review"),
    
    /**
     * Request is being processed
     */
    IN_PROGRESS("In Progress", "Request is being processed"),
    
    /**
     * Request has been completed successfully
     */
    COMPLETED("Completed", "Request has been completed successfully"),
    
    /**
     * Request has been rejected
     */
    REJECTED("Rejected", "Request has been rejected"),
    
    /**
     * Request is on hold pending additional information
     */
    ON_HOLD("On Hold", "Request is on hold pending additional information"),
    
    /**
     * Request has been cancelled by the user
     */
    CANCELLED("Cancelled", "Request has been cancelled by the user"),
    
    /**
     * Request is under review by legal/compliance team
     */
    UNDER_LEGAL_REVIEW("Under Legal Review", "Request is being reviewed by legal/compliance team"),
    
    /**
     * Request requires additional verification
     */
    AWAITING_VERIFICATION("Awaiting Verification", "Request requires additional verification"),
    
    /**
     * Request has been escalated
     */
    ESCALATED("Escalated", "Request has been escalated for higher-level review"),
    
    /**
     * Request is being appealed
     */
    UNDER_APPEAL("Under Appeal", "Request is being appealed"),
    
    /**
     * Request has been partially completed
     */
    PARTIALLY_COMPLETED("Partially Completed", "Request has been partially completed"),
    
    /**
     * Request has been archived
     */
    ARCHIVED("Archived", "Request has been archived for record keeping");
    
    private final String displayName;
    private final String description;
    
    DataSubjectRequestStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if the request is in a final state
     */
    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED || this == ARCHIVED;
    }
    
    /**
     * Check if the request is in an active state
     */
    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS || this == ON_HOLD || 
               this == UNDER_LEGAL_REVIEW || this == AWAITING_VERIFICATION || 
               this == ESCALATED || this == UNDER_APPEAL;
    }
    
    /**
     * Check if the request can be cancelled
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == ON_HOLD || this == AWAITING_VERIFICATION;
    }
    
    /**
     * Check if the request can be escalated
     */
    public boolean canBeEscalated() {
        return this == PENDING || this == ON_HOLD || this == AWAITING_VERIFICATION;
    }
}
