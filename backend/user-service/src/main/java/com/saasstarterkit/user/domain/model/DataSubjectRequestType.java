package com.saasstarterkit.user.domain.model;

/**
 * Types of data subject rights requests (GDPR Articles 15-22)
 */
public enum DataSubjectRequestType {
    /**
     * Right of access (Article 15) - User wants to know what data is processed
     */
    ACCESS("Access", "Right to know what personal data is being processed"),
    
    /**
     * Right to rectification (Article 16) - User wants to correct inaccurate data
     */
    RECTIFICATION("Rectification", "Right to correct inaccurate personal data"),
    
    /**
     * Right to erasure (Article 17) - User wants to delete their data
     */
    ERASURE("Erasure", "Right to delete personal data (right to be forgotten)"),
    
    /**
     * Right to restrict processing (Article 18) - User wants to limit data processing
     */
    RESTRICT_PROCESSING("Restrict Processing", "Right to limit how personal data is processed"),
    
    /**
     * Right to data portability (Article 20) - User wants to export their data
     */
    DATA_PORTABILITY("Data Portability", "Right to receive personal data in a structured format"),
    
    /**
     * Right to object (Article 21) - User wants to object to data processing
     */
    OBJECT_TO_PROCESSING("Object to Processing", "Right to object to personal data processing"),
    
    /**
     * Right to withdraw consent (Article 7) - User wants to withdraw consent
     */
    WITHDRAW_CONSENT("Withdraw Consent", "Right to withdraw previously given consent"),
    
    /**
     * Right to lodge a complaint (Article 77) - User wants to file a complaint
     */
    LODGE_COMPLAINT("Lodge Complaint", "Right to file a complaint with supervisory authority"),
    
    /**
     * Right to compensation (Article 82) - User wants compensation for damages
     */
    COMPENSATION("Compensation", "Right to compensation for damages suffered"),
    
    /**
     * Right to representation (Article 80) - User wants to be represented
     */
    REPRESENTATION("Representation", "Right to be represented by a not-for-profit body"),
    
    /**
     * Right to human intervention (Article 22) - User wants human review of automated decisions
     */
    HUMAN_INTERVENTION("Human Intervention", "Right to human review of automated decisions"),
    
    /**
     * Right to explanation (Article 22) - User wants explanation of automated decisions
     */
    EXPLANATION("Explanation", "Right to explanation of automated decision-making logic"),
    
    /**
     * Right to notification (Article 19) - User wants to be notified of data changes
     */
    NOTIFICATION("Notification", "Right to be notified of data processing changes"),
    
    /**
     * Right to information (Article 13-14) - User wants detailed processing information
     */
    INFORMATION("Information", "Right to detailed information about data processing"),
    
    /**
     * Right to verification (Article 12) - User wants to verify their identity
     */
    VERIFICATION("Verification", "Right to verify identity for data access"),
    
    /**
     * Right to appeal (Article 78) - User wants to appeal a decision
     */
    APPEAL("Appeal", "Right to appeal against supervisory authority decisions");
    
    private final String displayName;
    private final String description;
    
    DataSubjectRequestType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this request type requires immediate action (within 30 days)
     */
    public boolean requiresImmediateAction() {
        return this == ACCESS || this == RECTIFICATION || this == ERASURE || this == RESTRICT_PROCESSING;
    }
    
    /**
     * Check if this request type can be extended (up to 60 days)
     */
    public boolean canBeExtended() {
        return this == DATA_PORTABILITY || this == OBJECT_TO_PROCESSING;
    }
    
    /**
     * Check if this request type requires verification
     */
    public boolean requiresVerification() {
        return this == ACCESS || this == ERASURE || this == DATA_PORTABILITY;
    }
}
