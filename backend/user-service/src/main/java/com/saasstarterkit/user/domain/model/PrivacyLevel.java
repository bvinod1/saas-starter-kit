package com.saasstarterkit.user.domain.model;

/**
 * Privacy levels for user data handling
 */
public enum PrivacyLevel {
    /**
     * Minimum privacy - basic data protection
     */
    MINIMUM("Minimum", "Basic data protection with minimal privacy controls"),
    
    /**
     * Standard privacy - typical privacy controls
     */
    STANDARD("Standard", "Standard privacy controls for most users"),
    
    /**
     * Enhanced privacy - increased privacy protection
     */
    ENHANCED("Enhanced", "Enhanced privacy controls for sensitive data"),
    
    /**
     * Maximum privacy - highest level of privacy protection
     */
    MAXIMUM("Maximum", "Maximum privacy controls for highly sensitive data"),
    
    /**
     * Custom privacy - user-defined privacy settings
     */
    CUSTOM("Custom", "User-defined privacy settings and controls"),
    
    /**
     * Compliance privacy - regulatory compliance focused
     */
    COMPLIANCE("Compliance", "Privacy controls focused on regulatory compliance"),
    
    /**
     * Healthcare privacy - HIPAA-focused privacy controls
     */
    HEALTHCARE("Healthcare", "Privacy controls focused on HIPAA compliance"),
    
    /**
     * Financial privacy - PCI DSS focused privacy controls
     */
    FINANCIAL("Financial", "Privacy controls focused on PCI DSS compliance"),
    
    /**
     * Research privacy - research participant privacy controls
     */
    RESEARCH("Research", "Privacy controls for research participants"),
    
    /**
     * Government privacy - government security clearance levels
     */
    GOVERNMENT("Government", "Privacy controls for government security clearances");
    
    private final String displayName;
    private final String description;
    
    PrivacyLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this privacy level requires encryption
     */
    public boolean requiresEncryption() {
        return this == ENHANCED || this == MAXIMUM || this == COMPLIANCE || 
               this == HEALTHCARE || this == FINANCIAL || this == RESEARCH || 
               this == GOVERNMENT;
    }
    
    /**
     * Check if this privacy level requires audit logging
     */
    public boolean requiresAuditLogging() {
        return this == ENHANCED || this == MAXIMUM || this == COMPLIANCE || 
               this == HEALTHCARE || this == FINANCIAL || this == RESEARCH || 
               this == GOVERNMENT;
    }
    
    /**
     * Check if this privacy level requires access controls
     */
    public boolean requiresAccessControls() {
        return this != MINIMUM;
    }
    
    /**
     * Check if this privacy level requires data retention policies
     */
    public boolean requiresRetentionPolicies() {
        return this == COMPLIANCE || this == HEALTHCARE || this == FINANCIAL || 
               this == RESEARCH || this == GOVERNMENT;
    }
    
    /**
     * Check if this privacy level requires breach notification
     */
    public boolean requiresBreachNotification() {
        return this == COMPLIANCE || this == HEALTHCARE || this == FINANCIAL || 
               this == RESEARCH || this == GOVERNMENT;
    }
    
    /**
     * Check if this privacy level requires multi-factor authentication
     */
    public boolean requiresMultiFactor() {
        return this == MAXIMUM || this == COMPLIANCE || this == HEALTHCARE || 
               this == FINANCIAL || this == RESEARCH || this == GOVERNMENT;
    }
    
    /**
     * Get the minimum data classification required
     */
    public DataClassification getMinimumDataClassification() {
        switch (this) {
            case MINIMUM:
                return DataClassification.PUBLIC;
            case STANDARD:
                return DataClassification.INTERNAL;
            case ENHANCED:
                return DataClassification.CONFIDENTIAL;
            case MAXIMUM:
            case COMPLIANCE:
            case HEALTHCARE:
            case FINANCIAL:
            case RESEARCH:
            case GOVERNMENT:
                return DataClassification.RESTRICTED;
            case CUSTOM:
                return DataClassification.INTERNAL;
            default:
                return DataClassification.INTERNAL;
        }
    }
    
    /**
     * Check if this privacy level is compliant with GDPR
     */
    public boolean isGdprCompliant() {
        return this == ENHANCED || this == MAXIMUM || this == COMPLIANCE || 
               this == HEALTHCARE || this == FINANCIAL || this == RESEARCH;
    }
    
    /**
     * Check if this privacy level is compliant with HIPAA
     */
    public boolean isHipaaCompliant() {
        return this == HEALTHCARE || this == MAXIMUM || this == COMPLIANCE;
    }
    
    /**
     * Check if this privacy level is compliant with PCI DSS
     */
    public boolean isPciDssCompliant() {
        return this == FINANCIAL || this == MAXIMUM || this == COMPLIANCE;
    }
}
