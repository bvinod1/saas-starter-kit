package com.saasstarterkit.billing.domain.model;

/**
 * Represents the tier level of a subscription plan
 */
public enum PlanTier {
    /**
     * Free tier with basic features
     */
    FREE,
    
    /**
     * Starter tier with essential features
     */
    STARTER,
    
    /**
     * Professional tier with advanced features
     */
    PROFESSIONAL,
    
    /**
     * Enterprise tier with full features
     */
    ENTERPRISE,
    
    /**
     * Custom tier with tailored features
     */
    CUSTOM
}
