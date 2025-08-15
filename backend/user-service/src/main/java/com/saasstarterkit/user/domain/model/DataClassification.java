package com.saasstarterkit.user.domain.model;

/**
 * Data classification levels for security and compliance
 */
public enum DataClassification {
    /**
     * Public data - can be freely shared
     */
    PUBLIC("Public", "Data that can be freely shared with anyone"),
    
    /**
     * Internal data - for internal use only
     */
    INTERNAL("Internal", "Data for internal business use only"),
    
    /**
     * Confidential data - requires authorization
     */
    CONFIDENTIAL("Confidential", "Data that requires authorization to access"),
    
    /**
     * Restricted data - highly sensitive, limited access
     */
    RESTRICTED("Restricted", "Highly sensitive data with limited access"),
    
    /**
     * Classified data - government or military classification
     */
    CLASSIFIED("Classified", "Government or military classified information"),
    
    /**
     * Personal data - GDPR personal information
     */
    PERSONAL("Personal", "Personal data protected under GDPR"),
    
    /**
     * Sensitive personal data - GDPR special categories
     */
    SENSITIVE_PERSONAL("Sensitive Personal", "Special category personal data under GDPR"),
    
    /**
     * Health data - HIPAA protected health information
     */
    HEALTH_DATA("Health Data", "Protected health information under HIPAA"),
    
    /**
     * Financial data - PCI DSS protected information
     */
    FINANCIAL_DATA("Financial Data", "Financial information protected under PCI DSS"),
    
    /**
     * Legal data - attorney-client privilege
     */
    LEGAL_DATA("Legal Data", "Legal information with attorney-client privilege"),
    
    /**
     * Research data - research participant information
     */
    RESEARCH_DATA("Research Data", "Research participant and study information");
    
    private final String displayName;
    private final String description;
    
    DataClassification(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this classification requires encryption
     */
    public boolean requiresEncryption() {
        return this == RESTRICTED || this == CLASSIFIED || this == SENSITIVE_PERSONAL || 
               this == HEALTH_DATA || this == FINANCIAL_DATA || this == LEGAL_DATA;
    }
    
    /**
     * Check if this classification requires audit logging
     */
    public boolean requiresAuditLogging() {
        return this == CONFIDENTIAL || this == RESTRICTED || this == CLASSIFIED || 
               this == PERSONAL || this == SENSITIVE_PERSONAL || this == HEALTH_DATA || 
               this == FINANCIAL_DATA || this == LEGAL_DATA;
    }
    
    /**
     * Check if this classification requires access controls
     */
    public boolean requiresAccessControls() {
        return this == CONFIDENTIAL || this == RESTRICTED || this == CLASSIFIED || 
               this == PERSONAL || this == SENSITIVE_PERSONAL || this == HEALTH_DATA || 
               this == FINANCIAL_DATA || this == LEGAL_DATA || this == RESEARCH_DATA;
    }
    
    /**
     * Check if this classification requires data retention policies
     */
    public boolean requiresRetentionPolicies() {
        return this == PERSONAL || this == SENSITIVE_PERSONAL || this == HEALTH_DATA || 
               this == FINANCIAL_DATA || this == LEGAL_DATA || this == RESEARCH_DATA;
    }
    
    /**
     * Check if this classification requires breach notification
     */
    public boolean requiresBreachNotification() {
        return this == SENSITIVE_PERSONAL || this == HEALTH_DATA || 
               this == FINANCIAL_DATA || this == LEGAL_DATA;
    }
    
    /**
     * Get the minimum security level required
     */
    public SecurityLevel getMinimumSecurityLevel() {
        switch (this) {
            case PUBLIC:
                return SecurityLevel.BASIC;
            case INTERNAL:
                return SecurityLevel.STANDARD;
            case CONFIDENTIAL:
                return SecurityLevel.ENHANCED;
            case RESTRICTED:
            case PERSONAL:
            case SENSITIVE_PERSONAL:
            case HEALTH_DATA:
            case FINANCIAL_DATA:
            case LEGAL_DATA:
            case RESEARCH_DATA:
                return SecurityLevel.HIGH;
            case CLASSIFIED:
                return SecurityLevel.MAXIMUM;
            default:
                return SecurityLevel.STANDARD;
        }
    }
    
    /**
     * Security levels for data classification
     */
    public enum SecurityLevel {
        BASIC("Basic", "Standard security measures"),
        STANDARD("Standard", "Enhanced security measures"),
        ENHANCED("Enhanced", "Strong security measures"),
        HIGH("High", "Very strong security measures"),
        MAXIMUM("Maximum", "Maximum security measures");
        
        private final String displayName;
        private final String description;
        
        SecurityLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
