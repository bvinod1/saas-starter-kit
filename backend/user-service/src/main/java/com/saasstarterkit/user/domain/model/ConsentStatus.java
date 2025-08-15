package com.saasstarterkit.user.domain.model;

/**
 * Status of user consent for data processing
 */
public enum ConsentStatus {
    /**
     * Consent is pending user action
     */
    PENDING("Pending", "Consent has not been given yet"),
    
    /**
     * Consent has been actively given by the user
     */
    ACTIVE("Active", "User has given consent"),
    
    /**
     * Consent has been withdrawn by the user
     */
    WITHDRAWN("Withdrawn", "User has withdrawn consent"),
    
    /**
     * Consent has expired
     */
    EXPIRED("Expired", "Consent has expired and needs renewal"),
    
    /**
     * Consent is being reviewed
     */
    UNDER_REVIEW("Under Review", "Consent is being reviewed by administrators"),
    
    /**
     * Consent has been rejected
     */
    REJECTED("Rejected", "Consent has been rejected"),
    
    /**
     * Consent is suspended
     */
    SUSPENDED("Suspended", "Consent is temporarily suspended"),
    
    /**
     * Consent is archived
     */
    ARCHIVED("Archived", "Consent has been archived for record keeping");
    
    private final String displayName;
    private final String description;
    
    ConsentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
