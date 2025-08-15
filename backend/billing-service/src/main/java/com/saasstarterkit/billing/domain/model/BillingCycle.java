package com.saasstarterkit.billing.domain.model;

/**
 * Represents the billing cycle frequency for subscriptions
 */
public enum BillingCycle {
    /**
     * Billed monthly
     */
    MONTHLY,
    
    /**
     * Billed quarterly
     */
    QUARTERLY,
    
    /**
     * Billed annually
     */
    ANNUALLY,
    
    /**
     * Billed every 2 years
     */
    BIENNIALLY,
    
    /**
     * Billed every 3 years
     */
    TRIENNIALLY,
    
    /**
     * Billed weekly
     */
    WEEKLY,
    
    /**
     * Billed daily
     */
    DAILY,
    
    /**
     * One-time payment
     */
    ONE_TIME
}
