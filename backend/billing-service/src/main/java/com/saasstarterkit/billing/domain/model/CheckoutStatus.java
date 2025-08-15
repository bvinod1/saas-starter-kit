package com.saasstarterkit.billing.domain.model;

/**
 * Represents the status of a checkout session
 */
public enum CheckoutStatus {
    /**
     * Checkout session is pending completion
     */
    PENDING,
    
    /**
     * Checkout session has been completed successfully
     */
    COMPLETED,
    
    /**
     * Checkout session has expired
     */
    EXPIRED,
    
    /**
     * Checkout session was cancelled
     */
    CANCELLED,
    
    /**
     * Checkout session failed
     */
    FAILED
}
