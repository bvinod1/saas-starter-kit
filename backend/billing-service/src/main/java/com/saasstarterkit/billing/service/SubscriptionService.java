package com.saasstarterkit.billing.service;

import com.saasstarterkit.billing.domain.interfaces.ISubscriptionProvider;
import com.saasstarterkit.billing.domain.model.*;
import com.saasstarterkit.billing.domain.dto.*;
import com.saasstarterkit.billing.exception.SubscriptionException;
import com.saasstarterkit.billing.exception.FeatureAccessException;
import com.saasstarterkit.billing.repository.SubscriptionRepository;
import com.saasstarterkit.billing.repository.PlanRepository;
import com.saasstarterkit.billing.repository.CheckoutSessionRepository;
import com.saasstarterkit.billing.event.SubscriptionEventPublisher;
import com.saasstarterkit.billing.event.SubscriptionCreatedEvent;
import com.saasstarterkit.billing.event.SubscriptionUpdatedEvent;
import com.saasstarterkit.billing.event.SubscriptionCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Core subscription service that implements business logic and feature access control
 */
@Service
@Transactional
public class SubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final ISubscriptionProvider subscriptionProvider;
    private final SubscriptionEventPublisher eventPublisher;
    private final FeatureAccessService featureAccessService;
    private final TenantOnboardingService tenantOnboardingService;
    
    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            CheckoutSessionRepository checkoutSessionRepository,
            @Qualifier("zohoBillingProvider") ISubscriptionProvider subscriptionProvider,
            SubscriptionEventPublisher eventPublisher,
            FeatureAccessService featureAccessService,
            TenantOnboardingService tenantOnboardingService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.planRepository = planRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.subscriptionProvider = subscriptionProvider;
        this.eventPublisher = eventPublisher;
        this.featureAccessService = featureAccessService;
        this.tenantOnboardingService = tenantOnboardingService;
    }
    
    /**
     * Create a new subscription for a tenant
     */
    public CompletableFuture<Subscription> createSubscription(CreateSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Creating subscription for tenant: {}", request.getTenantId());
                
                // Validate tenant exists and can have subscriptions
                validateTenantForSubscription(request.getTenantId());
                
                // Check if tenant already has an active subscription
                Optional<Subscription> existingSubscription = subscriptionRepository.findByTenantIdAndStatusIn(
                    request.getTenantId(), 
                    List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL)
                );
                
                if (existingSubscription.isPresent()) {
                    throw new SubscriptionException("Tenant already has an active subscription");
                }
                
                // Create subscription in external provider
                Subscription subscription = subscriptionProvider.createSubscription(request).join();
                
                // Save subscription locally
                subscription = subscriptionRepository.save(subscription);
                
                // Update tenant features and limits
                featureAccessService.updateTenantFeatures(subscription.getTenantId(), subscription);
                
                // Trigger onboarding process
                tenantOnboardingService.startOnboarding(subscription.getTenantId(), subscription);
                
                // Publish event
                eventPublisher.publishSubscriptionCreated(new SubscriptionCreatedEvent(subscription));
                
                logger.info("Successfully created subscription: {} for tenant: {}", 
                    subscription.getId(), request.getTenantId());
                
                return subscription;
                
            } catch (Exception e) {
                logger.error("Error creating subscription for tenant: {}", request.getTenantId(), e);
                throw new SubscriptionException("Failed to create subscription", e);
            }
        });
    }
    
    /**
     * Get subscription by ID
     */
    public CompletableFuture<Optional<Subscription>> getSubscription(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching subscription: {}", subscriptionId);
                
                // First try to get from local database
                Optional<Subscription> localSubscription = subscriptionRepository.findById(subscriptionId);
                
                if (localSubscription.isPresent()) {
                    return localSubscription;
                }
                
                // If not found locally, try to get from external provider
                Optional<Subscription> externalSubscription = subscriptionProvider.getSubscription(subscriptionId).join();
                
                if (externalSubscription.isPresent()) {
                    // Save to local database
                    Subscription subscription = subscriptionRepository.save(externalSubscription.get());
                    return Optional.of(subscription);
                }
                
                return Optional.empty();
                
            } catch (Exception e) {
                logger.error("Error fetching subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionException("Failed to fetch subscription", e));
            }
        });
    }
    
    /**
     * Get subscription by tenant ID
     */
    public CompletableFuture<Optional<Subscription>> getSubscriptionByTenantId(String tenantId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching subscription for tenant: {}", tenantId);
                
                // First try to get from local database
                Optional<Subscription> localSubscription = subscriptionRepository.findByTenantId(tenantId);
                
                if (localSubscription.isPresent()) {
                    return localSubscription;
                }
                
                // If not found locally, try to get from external provider
                Optional<Subscription> externalSubscription = subscriptionProvider.getSubscriptionByTenantId(tenantId).join();
                
                if (externalSubscription.isPresent()) {
                    // Save to local database
                    Subscription subscription = subscriptionRepository.save(externalSubscription.get());
                    return Optional.of(subscription);
                }
                
                return Optional.empty();
                
            } catch (Exception e) {
                logger.error("Error fetching subscription for tenant: {}", tenantId, e);
                throw new CompletionException(new SubscriptionException("Failed to fetch subscription by tenant ID", e));
            }
        });
    }
    
    /**
     * Update subscription
     */
    public CompletableFuture<Subscription> updateSubscription(String subscriptionId, UpdateSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Updating subscription: {}", subscriptionId);
                
                // Validate subscription exists
                Optional<Subscription> existingSubscription = getSubscription(subscriptionId).join();
                if (existingSubscription.isEmpty()) {
                    throw new SubscriptionException("Subscription not found: " + subscriptionId);
                }
                
                Subscription subscription = existingSubscription.get();
                
                // Update in external provider
                Subscription updatedSubscription = subscriptionProvider.updateSubscription(subscriptionId, request).join();
                
                // Update local subscription
                updatedSubscription = subscriptionRepository.save(updatedSubscription);
                
                // Update tenant features and limits
                featureAccessService.updateTenantFeatures(updatedSubscription.getTenantId(), updatedSubscription);
                
                // Publish event
                eventPublisher.publishSubscriptionUpdated(new SubscriptionUpdatedEvent(subscription, updatedSubscription));
                
                logger.info("Successfully updated subscription: {}", subscriptionId);
                return updatedSubscription;
                
            } catch (Exception e) {
                logger.error("Error updating subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionException("Failed to update subscription", e));
            }
        });
    }
    
    /**
     * Cancel subscription
     */
    public CompletableFuture<Subscription> cancelSubscription(String subscriptionId, CancelSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Cancelling subscription: {}", subscriptionId);
                
                // Validate subscription exists
                Optional<Subscription> existingSubscription = getSubscription(subscriptionId).join();
                if (existingSubscription.isEmpty()) {
                    throw new SubscriptionException("Subscription not found: " + subscriptionId);
                }
                
                Subscription subscription = existingSubscription.get();
                
                // Cancel in external provider
                Subscription cancelledSubscription = subscriptionProvider.cancelSubscription(subscriptionId, request).join();
                
                // Update local subscription
                cancelledSubscription = subscriptionRepository.save(cancelledSubscription);
                
                // Update tenant features and limits
                featureAccessService.updateTenantFeatures(cancelledSubscription.getTenantId(), cancelledSubscription);
                
                // Start offboarding process
                tenantOnboardingService.startOffboarding(cancelledSubscription.getTenantId(), cancelledSubscription);
                
                // Publish event
                eventPublisher.publishSubscriptionCancelled(new SubscriptionCancelledEvent(subscription, cancelledSubscription));
                
                logger.info("Successfully cancelled subscription: {}", subscriptionId);
                return cancelledSubscription;
                
            } catch (Exception e) {
                logger.error("Error cancelling subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionException("Failed to cancel subscription", e));
            }
        });
    }
    
    /**
     * Reactivate subscription
     */
    public CompletableFuture<Subscription> reactivateSubscription(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Reactivating subscription: {}", subscriptionId);
                
                // Validate subscription exists
                Optional<Subscription> existingSubscription = getSubscription(subscriptionId).join();
                if (existingSubscription.isEmpty()) {
                    throw new SubscriptionException("Subscription not found: " + subscriptionId);
                }
                
                Subscription subscription = existingSubscription.get();
                
                // Reactivate in external provider
                Subscription reactivatedSubscription = subscriptionProvider.reactivateSubscription(subscriptionId).join();
                
                // Update local subscription
                reactivatedSubscription = subscriptionRepository.save(reactivatedSubscription);
                
                // Update tenant features and limits
                featureAccessService.updateTenantFeatures(reactivatedSubscription.getTenantId(), reactivatedSubscription);
                
                // Resume onboarding process
                tenantOnboardingService.resumeOnboarding(reactivatedSubscription.getTenantId(), reactivatedSubscription);
                
                logger.info("Successfully reactivated subscription: {}", subscriptionId);
                return reactivatedSubscription;
                
            } catch (Exception e) {
                logger.error("Error reactivating subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionException("Failed to reactivate subscription", e));
            }
        });
    }
    
    /**
     * Change subscription plan
     */
    public CompletableFuture<Subscription> changePlan(String subscriptionId, ChangePlanRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Changing plan for subscription: {}", subscriptionId);
                
                // Validate subscription exists
                Optional<Subscription> existingSubscription = getSubscription(subscriptionId).join();
                if (existingSubscription.isEmpty()) {
                    throw new SubscriptionException("Subscription not found: " + subscriptionId);
                }
                
                Subscription subscription = existingSubscription.get();
                
                // Validate new plan exists
                Optional<Plan> newPlan = planRepository.findByPlanId(request.getNewPlanId());
                if (newPlan.isEmpty()) {
                    throw new SubscriptionException("Plan not found: " + request.getNewPlanId());
                }
                
                // Change plan in external provider
                Subscription updatedSubscription = subscriptionProvider.changePlan(subscriptionId, request).join();
                
                // Update local subscription
                updatedSubscription = subscriptionRepository.save(updatedSubscription);
                
                // Update tenant features and limits
                featureAccessService.updateTenantFeatures(updatedSubscription.getTenantId(), updatedSubscription);
                
                // Publish event
                eventPublisher.publishSubscriptionUpdated(new SubscriptionUpdatedEvent(subscription, updatedSubscription));
                
                logger.info("Successfully changed plan for subscription: {}", subscriptionId);
                return updatedSubscription;
                
            } catch (Exception e) {
                logger.error("Error changing plan for subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionException("Failed to change plan", e));
            }
        });
    }
    
    /**
     * Create checkout session
     */
    public CompletableFuture<CheckoutSession> createCheckoutSession(CreateCheckoutSessionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Creating checkout session for tenant: {}", request.getTenantId());
                
                // Validate tenant exists
                validateTenantForSubscription(request.getTenantId());
                
                // Create checkout session in external provider
                CheckoutSession checkoutSession = subscriptionProvider.createCheckoutSession(request).join();
                
                // Save checkout session locally
                checkoutSession = checkoutSessionRepository.save(checkoutSession);
                
                logger.info("Successfully created checkout session: {} for tenant: {}", 
                    checkoutSession.getId(), request.getTenantId());
                
                return checkoutSession;
                
            } catch (Exception e) {
                logger.error("Error creating checkout session for tenant: {}", request.getTenantId(), e);
                throw new CompletionException(new SubscriptionException("Failed to create checkout session", e));
            }
        });
    }
    
    /**
     * Get checkout session
     */
    public CompletableFuture<Optional<CheckoutSession>> getCheckoutSession(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching checkout session: {}", sessionId);
                
                // First try to get from local database
                Optional<CheckoutSession> localSession = checkoutSessionRepository.findBySessionId(sessionId);
                
                if (localSession.isPresent()) {
                    return localSession;
                }
                
                // If not found locally, try to get from external provider
                Optional<CheckoutSession> externalSession = subscriptionProvider.getCheckoutSession(sessionId).join();
                
                if (externalSession.isPresent()) {
                    // Save to local database
                    CheckoutSession session = checkoutSessionRepository.save(externalSession.get());
                    return Optional.of(session);
                }
                
                return Optional.empty();
                
            } catch (Exception e) {
                logger.error("Error fetching checkout session: {}", sessionId, e);
                throw new CompletionException(new SubscriptionException("Failed to fetch checkout session", e));
            }
        });
    }
    
    /**
     * Process webhook event
     */
    public CompletableFuture<WebhookEvent> processWebhookEvent(String payload, String signature) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Processing webhook event from subscription provider");
                
                // Process webhook in external provider
                WebhookEvent webhookEvent = subscriptionProvider.processWebhookEvent(payload, signature).join();
                
                // Handle webhook event based on type
                handleWebhookEvent(webhookEvent);
                
                logger.info("Successfully processed webhook event: {}", webhookEvent.getEventId());
                return webhookEvent;
                
            } catch (Exception e) {
                logger.error("Error processing webhook event", e);
                throw new CompletionException(new SubscriptionException("Failed to process webhook event", e));
            }
        });
    }
    
    /**
     * Get available plans
     */
    public CompletableFuture<List<Plan>> getAvailablePlans() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching available plans");
                
                // First try to get from local database
                List<Plan> localPlans = planRepository.findByActiveTrueOrderBySortOrderAsc();
                
                if (!localPlans.isEmpty()) {
                    return localPlans;
                }
                
                // If no local plans, fetch from external provider
                List<Plan> externalPlans = subscriptionProvider.getAvailablePlans().join();
                
                // Save plans locally
                for (Plan plan : externalPlans) {
                    planRepository.save(plan);
                }
                
                return externalPlans;
                
            } catch (Exception e) {
                logger.error("Error fetching available plans", e);
                throw new CompletionException(new SubscriptionException("Failed to fetch available plans", e));
            }
        });
    }
    
    /**
     * Get plan by ID
     */
    public CompletableFuture<Optional<Plan>> getPlan(String planId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching plan: {}", planId);
                
                // First try to get from local database
                Optional<Plan> localPlan = planRepository.findByPlanId(planId);
                
                if (localPlan.isPresent()) {
                    return localPlan;
                }
                
                // If not found locally, try to get from external provider
                Optional<Plan> externalPlan = subscriptionProvider.getPlan(planId).join();
                
                if (externalPlan.isPresent()) {
                    // Save to local database
                    Plan plan = planRepository.save(externalPlan.get());
                    return Optional.of(plan);
                }
                
                return Optional.empty();
                
            } catch (Exception e) {
                logger.error("Error fetching plan: {}", planId, e);
                throw new CompletionException(new SubscriptionException("Failed to fetch plan", e));
            }
        });
    }
    
    /**
     * Check if tenant has access to a specific feature
     */
    public boolean hasFeatureAccess(String tenantId, String featureName) {
        try {
            Optional<Subscription> subscription = getSubscriptionByTenantId(tenantId).join();
            if (subscription.isEmpty()) {
                return false;
            }
            
            return featureAccessService.hasFeatureAccess(tenantId, featureName, subscription.get());
            
        } catch (Exception e) {
            logger.error("Error checking feature access for tenant: {} and feature: {}", tenantId, featureName, e);
            return false;
        }
    }
    
    /**
     * Check if tenant can perform an action based on usage limits
     */
    public boolean canPerformAction(String tenantId, String action, int quantity) {
        try {
            Optional<Subscription> subscription = getSubscriptionByTenantId(tenantId).join();
            if (subscription.isEmpty()) {
                return false;
            }
            
            return featureAccessService.canPerformAction(tenantId, action, quantity, subscription.get());
            
        } catch (Exception e) {
            logger.error("Error checking action permission for tenant: {} and action: {}", tenantId, action, e);
            return false;
        }
    }
    
    /**
     * Record usage for metered billing
     */
    public void recordUsage(String tenantId, String action, int quantity) {
        try {
            Optional<Subscription> subscription = getSubscriptionByTenantId(tenantId).join();
            if (subscription.isEmpty()) {
                logger.warn("Cannot record usage for tenant without subscription: {}", tenantId);
                return;
            }
            
            // Record usage in external provider
            RecordUsageRequest usageRequest = new RecordUsageRequest();
            usageRequest.setAction(action);
            usageRequest.setQuantity(quantity);
            
            subscriptionProvider.recordUsage(subscription.get().getExternalSubscriptionId(), usageRequest).join();
            
            // Update local usage tracking
            featureAccessService.recordUsage(tenantId, action, quantity, subscription.get());
            
        } catch (Exception e) {
            logger.error("Error recording usage for tenant: {} and action: {}", tenantId, action, e);
        }
    }
    
    /**
     * Synchronize subscriptions with external provider
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void syncSubscriptions() {
        logger.info("Starting subscription synchronization");
        
        try {
            // Get all local subscriptions that need syncing
            List<Subscription> subscriptionsToSync = subscriptionRepository.findBySyncStatusIn(
                List.of(SyncStatus.PENDING, SyncStatus.FAILED, SyncStatus.RETRY_SCHEDULED)
            );
            
            for (Subscription subscription : subscriptionsToSync) {
                try {
                    syncSubscription(subscription);
                } catch (Exception e) {
                    logger.error("Error syncing subscription: {}", subscription.getId(), e);
                    subscription.setSyncStatus(SyncStatus.FAILED);
                    subscriptionRepository.save(subscription);
                }
            }
            
            logger.info("Completed subscription synchronization for {} subscriptions", subscriptionsToSync.size());
            
        } catch (Exception e) {
            logger.error("Error during subscription synchronization", e);
        }
    }
    
    /**
     * Handle webhook events
     */
    private void handleWebhookEvent(WebhookEvent webhookEvent) {
        try {
            switch (webhookEvent.getEventType()) {
                case SUBSCRIPTION_CREATED:
                    handleSubscriptionCreated(webhookEvent);
                    break;
                case SUBSCRIPTION_UPDATED:
                    handleSubscriptionUpdated(webhookEvent);
                    break;
                case SUBSCRIPTION_CANCELLED:
                    handleSubscriptionCancelled(webhookEvent);
                    break;
                case SUBSCRIPTION_RENEWED:
                    handleSubscriptionRenewed(webhookEvent);
                    break;
                case PAYMENT_FAILED:
                    handlePaymentFailed(webhookEvent);
                    break;
                case INVOICE_CREATED:
                    handleInvoiceCreated(webhookEvent);
                    break;
                default:
                    logger.warn("Unhandled webhook event type: {}", webhookEvent.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error handling webhook event: {}", webhookEvent.getEventId(), e);
        }
    }
    
    /**
     * Handle subscription created webhook
     */
    private void handleSubscriptionCreated(WebhookEvent webhookEvent) {
        // Update local subscription status
        String subscriptionId = webhookEvent.getSubscriptionId();
        Optional<Subscription> subscription = subscriptionRepository.findByExternalSubscriptionId(subscriptionId);
        
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setSyncStatus(SyncStatus.SYNCED);
            sub.setLastSyncAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            
            // Update tenant features
            featureAccessService.updateTenantFeatures(sub.getTenantId(), sub);
        }
    }
    
    /**
     * Handle subscription updated webhook
     */
    private void handleSubscriptionUpdated(WebhookEvent webhookEvent) {
        // Sync subscription from external provider
        String subscriptionId = webhookEvent.getSubscriptionId();
        syncSubscriptionByExternalId(subscriptionId);
    }
    
    /**
     * Handle subscription cancelled webhook
     */
    private void handleSubscriptionCancelled(WebhookEvent webhookEvent) {
        // Update local subscription status
        String subscriptionId = webhookEvent.getSubscriptionId();
        Optional<Subscription> subscription = subscriptionRepository.findByExternalSubscriptionId(subscriptionId);
        
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            sub.setStatus(SubscriptionStatus.CANCELLED);
            sub.setCancelledAt(LocalDateTime.now());
            sub.setSyncStatus(SyncStatus.SYNCED);
            sub.setLastSyncAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            
            // Update tenant features
            featureAccessService.updateTenantFeatures(sub.getTenantId(), sub);
        }
    }
    
    /**
     * Handle subscription renewed webhook
     */
    private void handleSubscriptionRenewed(WebhookEvent webhookEvent) {
        // Sync subscription from external provider
        String subscriptionId = webhookEvent.getSubscriptionId();
        syncSubscriptionByExternalId(subscriptionId);
    }
    
    /**
     * Handle payment failed webhook
     */
    private void handlePaymentFailed(WebhookEvent webhookEvent) {
        // Update local subscription status
        String subscriptionId = webhookEvent.getSubscriptionId();
        Optional<Subscription> subscription = subscriptionRepository.findByExternalSubscriptionId(subscriptionId);
        
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            sub.setStatus(SubscriptionStatus.PAST_DUE);
            sub.setSyncStatus(SyncStatus.SYNCED);
            sub.setLastSyncAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            
            // Update tenant features
            featureAccessService.updateTenantFeatures(sub.getTenantId(), sub);
        }
    }
    
    /**
     * Handle invoice created webhook
     */
    private void handleInvoiceCreated(WebhookEvent webhookEvent) {
        // Handle invoice creation if needed
        logger.info("Invoice created for subscription: {}", webhookEvent.getSubscriptionId());
    }
    
    /**
     * Validate tenant for subscription
     */
    private void validateTenantForSubscription(String tenantId) {
        // This would typically validate against your tenant service
        // For now, we'll just check if the tenant ID is not null
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new SubscriptionException("Invalid tenant ID");
        }
    }
    
    /**
     * Sync subscription with external provider
     */
    private void syncSubscription(Subscription subscription) {
        try {
            subscription.setSyncStatus(SyncStatus.IN_PROGRESS);
            subscriptionRepository.save(subscription);
            
            // Get updated subscription from external provider
            Optional<Subscription> updatedSubscription = subscriptionProvider.getSubscription(
                subscription.getExternalSubscriptionId()
            ).join();
            
            if (updatedSubscription.isPresent()) {
                Subscription updated = updatedSubscription.get();
                updated.setId(subscription.getId()); // Preserve local ID
                updated.setSyncStatus(SyncStatus.SYNCED);
                updated.setLastSyncAt(LocalDateTime.now());
                
                subscriptionRepository.save(updated);
                
                // Update tenant features
                featureAccessService.updateTenantFeatures(updated.getTenantId(), updated);
            } else {
                subscription.setSyncStatus(SyncStatus.FAILED);
                subscriptionRepository.save(subscription);
            }
            
        } catch (Exception e) {
            logger.error("Error syncing subscription: {}", subscription.getId(), e);
            subscription.setSyncStatus(SyncStatus.FAILED);
            subscriptionRepository.save(subscription);
        }
    }
    
    /**
     * Sync subscription by external ID
     */
    private void syncSubscriptionByExternalId(String externalSubscriptionId) {
        Optional<Subscription> subscription = subscriptionRepository.findByExternalSubscriptionId(externalSubscriptionId);
        if (subscription.isPresent()) {
            syncSubscription(subscription.get());
        }
    }
}
