package com.saasstarterkit.billing.domain.model;

/**
 * Represents the current status of a subscription
 */
public enum SubscriptionStatus {
    /**
     * Subscription is active and billing normally
     */
    ACTIVE,
    
    /**
     * Subscription is in trial period
     */
    TRIAL,
    
    /**
     * Subscription is past due on payment
     */
    PAST_DUE,
    
    /**
     * Subscription has been cancelled
     */
    CANCELLED,
    
    /**
     * Subscription is suspended due to payment issues
     */
    SUSPENDED,
    
    /**
     * Subscription is pending activation
     */
    PENDING,
    
    /**
     * Subscription has expired
     */
    EXPIRED,
    
    /**
     * Subscription is being processed for cancellation
     */
    CANCELLING,
    
    /**
     * Subscription is being processed for reactivation
     */
    REACTIVATING,
    
    /**
     * Subscription is in grace period
     */
    GRACE_PERIOD,
    
    /**
     * Subscription is being upgraded/downgraded
     */
    CHANGING_PLAN
}
