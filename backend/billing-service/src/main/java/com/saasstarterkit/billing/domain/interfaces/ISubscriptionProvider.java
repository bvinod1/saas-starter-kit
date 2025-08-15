package com.saasstarterkit.billing.domain.interfaces;

import com.saasstarterkit.billing.domain.model.*;
import com.saasstarterkit.billing.domain.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for subscription management providers.
 * This interface allows different subscription management systems to be plugged in
 * without changing the application logic.
 */
public interface ISubscriptionProvider {
    
    /**
     * Create a new subscription for a tenant
     */
    CompletableFuture<Subscription> createSubscription(CreateSubscriptionRequest request);
    
    /**
     * Retrieve subscription details by ID
     */
    CompletableFuture<Optional<Subscription>> getSubscription(String subscriptionId);
    
    /**
     * Retrieve subscription by tenant ID
     */
    CompletableFuture<Optional<Subscription>> getSubscriptionByTenantId(String tenantId);
    
    /**
     * Update subscription details
     */
    CompletableFuture<Subscription> updateSubscription(String subscriptionId, UpdateSubscriptionRequest request);
    
    /**
     * Cancel a subscription
     */
    CompletableFuture<Subscription> cancelSubscription(String subscriptionId, CancelSubscriptionRequest request);
    
    /**
     * Reactivate a cancelled subscription
     */
    CompletableFuture<Subscription> reactivateSubscription(String subscriptionId);
    
    /**
     * Upgrade or downgrade subscription plan
     */
    CompletableFuture<Subscription> changePlan(String subscriptionId, ChangePlanRequest request);
    
    /**
     * Get all available plans
     */
    CompletableFuture<List<Plan>> getAvailablePlans();
    
    /**
     * Get plan details by ID
     */
    CompletableFuture<Optional<Plan>> getPlan(String planId);
    
    /**
     * Create checkout session for new subscriptions
     */
    CompletableFuture<CheckoutSession> createCheckoutSession(CreateCheckoutSessionRequest request);
    
    /**
     * Get checkout session by ID
     */
    CompletableFuture<Optional<CheckoutSession>> getCheckoutSession(String sessionId);
    
    /**
     * Process webhook events from the subscription provider
     */
    CompletableFuture<WebhookEvent> processWebhookEvent(String payload, String signature);
    
    /**
     * Get subscription usage and quota information
     */
    CompletableFuture<UsageQuota> getUsageQuota(String subscriptionId);
    
    /**
     * Record usage for metered billing
     */
    CompletableFuture<Void> recordUsage(String subscriptionId, RecordUsageRequest request);
    
    /**
     * Get billing history for a subscription
     */
    CompletableFuture<List<Invoice>> getBillingHistory(String subscriptionId);
    
    /**
     * Get invoice details
     */
    CompletableFuture<Optional<Invoice>> getInvoice(String invoiceId);
    
    /**
     * Validate webhook signature
     */
    boolean validateWebhookSignature(String payload, String signature);
    
    /**
     * Get provider-specific configuration
     */
    ProviderConfig getProviderConfig();
}
