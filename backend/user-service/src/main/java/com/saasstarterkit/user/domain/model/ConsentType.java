package com.saasstarterkit.user.domain.model;

/**
 * Types of data processing consent for GDPR compliance
 */
public enum ConsentType {
    /**
     * Consent for essential service functionality
     */
    ESSENTIAL_SERVICES("Essential Services", "Required for basic service operation"),
    
    /**
     * Consent for marketing communications
     */
    MARKETING("Marketing Communications", "Email, SMS, and push notifications for marketing"),
    
    /**
     * Consent for analytics and tracking
     */
    ANALYTICS("Analytics and Tracking", "Usage analytics and performance monitoring"),
    
    /**
     * Consent for third-party integrations
     */
    THIRD_PARTY_INTEGRATIONS("Third-Party Integrations", "Data sharing with third-party services"),
    
    /**
     * Consent for personalized content
     */
    PERSONALIZATION("Personalization", "Personalized content and recommendations"),
    
    /**
     * Consent for research and development
     */
    RESEARCH_AND_DEVELOPMENT("Research and Development", "Data used for product improvement"),
    
    /**
     * Consent for customer support
     */
    CUSTOMER_SUPPORT("Customer Support", "Data used for customer service and support"),
    
    /**
     * Consent for legal compliance
     */
    LEGAL_COMPLIANCE("Legal Compliance", "Data processing required by law"),
    
    /**
     * Consent for data portability
     */
    DATA_PORTABILITY("Data Portability", "Export and transfer of personal data"),
    
    /**
     * Consent for automated decision making
     */
    AUTOMATED_DECISION_MAKING("Automated Decision Making", "AI-powered decisions and profiling"),
    
    /**
     * Consent for health data processing (HIPAA)
     */
    HEALTH_DATA_PROCESSING("Health Data Processing", "Processing of health-related information"),
    
    /**
     * Consent for sensitive data processing
     */
    SENSITIVE_DATA_PROCESSING("Sensitive Data Processing", "Processing of special category data");
    
    private final String displayName;
    private final String description;
    
    ConsentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
