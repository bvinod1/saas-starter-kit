package com.saasstarterkit.tenant.domain.model.enums;

/**
 * ApprovalStatus - Defines the approval status of configuration changes
 * 
 * This enum represents the different approval statuses for configuration
 * changes in the tenant configuration system.
 */
public enum ApprovalStatus {
    
    /**
     * Pending approval - waiting for approval
     */
    PENDING("pending"),
    
    /**
     * Approved - configuration change has been approved
     */
    APPROVED("approved"),
    
    /**
     * Rejected - configuration change has been rejected
     */
    REJECTED("rejected"),
    
    /**
     * Under review - configuration change is being reviewed
     */
    UNDER_REVIEW("under_review"),
    
    /**
     * Requires changes - configuration change needs modifications
     */
    REQUIRES_CHANGES("requires_changes"),
    
    /**
     * Auto-approved - configuration change was automatically approved
     */
    AUTO_APPROVED("auto_approved"),
    
    /**
     * Escalated - configuration change has been escalated for higher approval
     */
    ESCALATED("escalated"),
    
    /**
     * Deferred - configuration change approval has been deferred
     */
    DEFERRED("deferred"),
    
    /**
     * Cancelled - configuration change approval has been cancelled
     */
    CANCELLED("cancelled"),
    
    /**
     * Expired - configuration change approval has expired
     */
    EXPIRED("expired"),
    
    /**
     * Conditional approval - configuration change approved with conditions
     */
    CONDITIONAL_APPROVAL("conditional_approval"),
    
    /**
     * Partial approval - configuration change partially approved
     */
    PARTIAL_APPROVAL("partial_approval"),
    
    /**
     * Emergency approval - configuration change approved in emergency
     */
    EMERGENCY_APPROVAL("emergency_approval"),
    
    /**
     * Temporary approval - configuration change temporarily approved
     */
    TEMPORARY_APPROVAL("temporary_approval"),
    
    /**
     * Custom approval status
     */
    CUSTOM("custom");
    
    private final String value;
    
    ApprovalStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ApprovalStatus fromValue(String value) {
        for (ApprovalStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalStatus: " + value);
    }
    
    /**
     * Check if this status indicates approval
     */
    public boolean isApproved() {
        return this == APPROVED || this == AUTO_APPROVED || this == CONDITIONAL_APPROVAL ||
               this == PARTIAL_APPROVAL || this == EMERGENCY_APPROVAL || this == TEMPORARY_APPROVAL;
    }
    
    /**
     * Check if this status indicates rejection
     */
    public boolean isRejected() {
        return this == REJECTED || this == CANCELLED || this == EXPIRED;
    }
    
    /**
     * Check if this status is pending
     */
    public boolean isPending() {
        return this == PENDING || this == UNDER_REVIEW || this == ESCALATED || 
               this == DEFERRED || this == REQUIRES_CHANGES;
    }
    
    /**
     * Check if this status allows deployment
     */
    public boolean allowsDeployment() {
        return this == APPROVED || this == AUTO_APPROVED || this == EMERGENCY_APPROVAL;
    }
    
    /**
     * Check if this status requires action
     */
    public boolean requiresAction() {
        return this == PENDING || this == UNDER_REVIEW || this == REQUIRES_CHANGES ||
               this == ESCALATED || this == DEFERRED;
    }
    
    /**
     * Check if this status is final
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || 
               this == EXPIRED;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
