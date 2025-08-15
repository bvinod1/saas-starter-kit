package com.saasstarterkit.user.domain.model;

/**
 * User account status
 */
public enum UserStatus {
    /**
     * User account is active and can access the system
     */
    ACTIVE("Active", "User account is active and can access the system"),
    
    /**
     * User account is inactive and cannot access the system
     */
    INACTIVE("Inactive", "User account is inactive and cannot access the system"),
    
    /**
     * User account is suspended temporarily
     */
    SUSPENDED("Suspended", "User account is temporarily suspended"),
    
    /**
     * User account is locked due to security reasons
     */
    LOCKED("Locked", "User account is locked due to security reasons"),
    
    /**
     * User account is pending verification
     */
    PENDING_VERIFICATION("Pending Verification", "User account is pending email/phone verification"),
    
    /**
     * User account is pending approval
     */
    PENDING_APPROVAL("Pending Approval", "User account is pending administrator approval"),
    
    /**
     * User account is marked for deletion
     */
    MARKED_FOR_DELETION("Marked for Deletion", "User account is marked for deletion"),
    
    /**
     * User account is archived
     */
    ARCHIVED("Archived", "User account is archived for record keeping"),
    
    /**
     * User account is expired
     */
    EXPIRED("Expired", "User account has expired"),
    
    /**
     * User account is in trial period
     */
    TRIAL("Trial", "User account is in trial period"),
    
    /**
     * User account is pending payment
     */
    PENDING_PAYMENT("Pending Payment", "User account is pending payment verification");
    
    private final String displayName;
    private final String description;
    
    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if the user can access the system
     */
    public boolean canAccess() {
        return this == ACTIVE || this == TRIAL;
    }
    
    /**
     * Check if the user account is in a final state
     */
    public boolean isFinal() {
        return this == ARCHIVED || this == EXPIRED;
    }
    
    /**
     * Check if the user account requires action
     */
    public boolean requiresAction() {
        return this == PENDING_VERIFICATION || this == PENDING_APPROVAL || 
               this == PENDING_PAYMENT || this == SUSPENDED;
    }
    
    /**
     * Check if the user account can be reactivated
     */
    public boolean canBeReactivated() {
        return this == INACTIVE || this == SUSPENDED || this == LOCKED;
    }
}
