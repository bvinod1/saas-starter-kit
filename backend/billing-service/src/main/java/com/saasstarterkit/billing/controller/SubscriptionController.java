package com.saasstarterkit.billing.controller;

import com.saasstarterkit.billing.service.SubscriptionService;
import com.saasstarterkit.billing.domain.model.*;
import com.saasstarterkit.billing.domain.dto.*;
import com.saasstarterkit.billing.exception.SubscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for managing subscriptions and plans
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Validated
public class SubscriptionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    
    private final SubscriptionService subscriptionService;
    
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    /**
     * Create a new subscription
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canCreateSubscription(#request.tenantId)")
    public CompletableFuture<ResponseEntity<Subscription>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        
        return subscriptionService.createSubscription(request)
            .thenApply(subscription -> ResponseEntity.status(HttpStatus.CREATED).body(subscription))
            .exceptionally(throwable -> {
                logger.error("Error creating subscription", throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Get subscription by ID
     */
    @GetMapping("/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessSubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<Subscription>> getSubscription(@PathVariable String subscriptionId) {
        
        return subscriptionService.getSubscription(subscriptionId)
            .thenApply(subscriptionOpt -> {
                if (subscriptionOpt.isPresent()) {
                    return ResponseEntity.ok(subscriptionOpt.get());
                } else {
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error fetching subscription: {}", subscriptionId, throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Get subscription by tenant ID
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessTenant(#tenantId)")
    public CompletableFuture<ResponseEntity<Subscription>> getSubscriptionByTenantId(@PathVariable String tenantId) {
        
        return subscriptionService.getSubscriptionByTenantId(tenantId)
            .thenApply(subscriptionOpt -> {
                if (subscriptionOpt.isPresent()) {
                    return ResponseEntity.ok(subscriptionOpt.get());
                } else {
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error fetching subscription for tenant: {}", tenantId, throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Update subscription
     */
    @PutMapping("/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canModifySubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<Subscription>> updateSubscription(
            @PathVariable String subscriptionId,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        
        return subscriptionService.updateSubscription(subscriptionId, request)
            .thenApply(subscription -> ResponseEntity.ok(subscription))
            .exceptionally(throwable -> {
                logger.error("Error updating subscription: {}", subscriptionId, throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Cancel subscription
     */
    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canModifySubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<Subscription>> cancelSubscription(
            @PathVariable String subscriptionId,
            @Valid @RequestBody CancelSubscriptionRequest request) {
        
        return subscriptionService.cancelSubscription(subscriptionId, request)
            .thenApply(subscription -> ResponseEntity.ok(subscription))
            .exceptionally(throwable -> {
                logger.error("Error cancelling subscription: {}", subscriptionId, throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Reactivate subscription
     */
    @PostMapping("/{subscriptionId}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canModifySubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<Subscription>> reactivateSubscription(@PathVariable String subscriptionId) {
        
        return subscriptionService.reactivateSubscription(subscriptionId)
            .thenApply(subscription -> ResponseEntity.ok(subscription))
            .exceptionally(throwable -> {
                logger.error("Error reactivating subscription: {}", subscriptionId, throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Change subscription plan
     */
    @PostMapping("/{subscriptionId}/change-plan")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canModifySubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<Subscription>> changePlan(
            @PathVariable String subscriptionId,
            @Valid @RequestBody ChangePlanRequest request) {
        
        return subscriptionService.changePlan(subscriptionId, request)
            .thenApply(subscription -> ResponseEntity.ok(subscription))
            .exceptionally(throwable -> {
                logger.error("Error changing plan for subscription: {}", subscriptionId, throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Create checkout session
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canCreateSubscription(#request.tenantId)")
    public CompletableFuture<ResponseEntity<CheckoutSession>> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request) {
        
        return subscriptionService.createCheckoutSession(request)
            .thenApply(checkoutSession -> ResponseEntity.status(HttpStatus.CREATED).body(checkoutSession))
            .exceptionally(throwable -> {
                logger.error("Error creating checkout session", throwable);
                if (throwable.getCause() instanceof SubscriptionException) {
                    return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Get checkout session
     */
    @GetMapping("/checkout/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessCheckoutSession(#sessionId)")
    public CompletableFuture<ResponseEntity<CheckoutSession>> getCheckoutSession(@PathVariable String sessionId) {
        
        return subscriptionService.getCheckoutSession(sessionId)
            .thenApply(sessionOpt -> {
                if (sessionOpt.isPresent()) {
                    return ResponseEntity.ok(sessionOpt.get());
                } else {
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error fetching checkout session: {}", sessionId, throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Get available plans
     */
    @GetMapping("/plans")
    public CompletableFuture<ResponseEntity<List<Plan>>> getAvailablePlans() {
        
        return subscriptionService.getAvailablePlans()
            .thenApply(plans -> ResponseEntity.ok(plans))
            .exceptionally(throwable -> {
                logger.error("Error fetching available plans", throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Get plan by ID
     */
    @GetMapping("/plans/{planId}")
    public CompletableFuture<ResponseEntity<Plan>> getPlan(@PathVariable String planId) {
        
        return subscriptionService.getPlan(planId)
            .thenApply(planOpt -> {
                if (planOpt.isPresent()) {
                    return ResponseEntity.ok(planOpt.get());
                } else {
                    return ResponseEntity.notFound().build();
                }
            })
            .exceptionally(throwable -> {
                logger.error("Error fetching plan: {}", planId, throwable);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }
    
    /**
     * Check feature access for tenant
     */
    @GetMapping("/tenant/{tenantId}/features/{featureName}/access")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessTenant(#tenantId)")
    public CompletableFuture<ResponseEntity<Boolean>> hasFeatureAccess(
            @PathVariable String tenantId,
            @PathVariable String featureName) {
        
        boolean hasAccess = subscriptionService.hasFeatureAccess(tenantId, featureName);
        return CompletableFuture.completedFuture(ResponseEntity.ok(hasAccess));
    }
    
    /**
     * Check if tenant can perform action
     */
    @PostMapping("/tenant/{tenantId}/actions/{action}/check")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessTenant(#tenantId)")
    public CompletableFuture<ResponseEntity<Boolean>> canPerformAction(
            @PathVariable String tenantId,
            @PathVariable String action,
            @RequestParam(defaultValue = "1") int quantity) {
        
        boolean canPerform = subscriptionService.canPerformAction(tenantId, action, quantity);
        return CompletableFuture.completedFuture(ResponseEntity.ok(canPerform));
    }
    
    /**
     * Record usage for metered billing
     */
    @PostMapping("/tenant/{tenantId}/usage")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessTenant(#tenantId)")
    public CompletableFuture<ResponseEntity<Void>> recordUsage(
            @PathVariable String tenantId,
            @RequestParam String action,
            @RequestParam(defaultValue = "1") int quantity) {
        
        subscriptionService.recordUsage(tenantId, action, quantity);
        return CompletableFuture.completedFuture(ResponseEntity.ok().build());
    }
    
    /**
     * Get subscription usage and quota
     */
    @GetMapping("/{subscriptionId}/usage")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessSubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<UsageQuota>> getUsageQuota(@PathVariable String subscriptionId) {
        
        // This would typically call the subscription provider to get usage data
        // For now, return a placeholder response
        UsageQuota usageQuota = new UsageQuota();
        usageQuota.setSubscriptionId(subscriptionId);
        // Set other fields as needed
        
        return CompletableFuture.completedFuture(ResponseEntity.ok(usageQuota));
    }
    
    /**
     * Get billing history for subscription
     */
    @GetMapping("/{subscriptionId}/billing")
    @PreAuthorize("hasRole('ADMIN') or @subscriptionSecurityService.canAccessSubscription(#subscriptionId)")
    public CompletableFuture<ResponseEntity<List<Invoice>>> getBillingHistory(@PathVariable String subscriptionId) {
        
        // This would typically call the subscription provider to get billing data
        // For now, return an empty list
        return CompletableFuture.completedFuture(ResponseEntity.ok(List.of()));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Subscription service is healthy");
    }
}
