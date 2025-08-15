package com.saasstarterkit.billing.adapters.zoho;

import com.saasstarterkit.billing.domain.interfaces.ISubscriptionProvider;
import com.saasstarterkit.billing.domain.model.*;
import com.saasstarterkit.billing.domain.dto.*;
import com.saasstarterkit.billing.config.ZohoBillingConfig;
import com.saasstarterkit.billing.exception.SubscriptionProviderException;
import com.saasstarterkit.billing.util.WebhookSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Zoho Billing implementation of the subscription provider interface
 */
@Component("zohoBillingProvider")
public class ZohoBillingProvider implements ISubscriptionProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ZohoBillingProvider.class);
    private static final String API_BASE_URL = "https://www.zohoapis.com/billing/v1";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final WebClient webClient;
    private final ZohoBillingConfig config;
    private final WebhookSignatureValidator signatureValidator;
    private final ZohoBillingMapper mapper;
    
    public ZohoBillingProvider(ZohoBillingConfig config, WebhookSignatureValidator signatureValidator, ZohoBillingMapper mapper) {
        this.config = config;
        this.signatureValidator = signatureValidator;
        this.mapper = mapper;
        
        this.webClient = WebClient.builder()
            .baseUrl(API_BASE_URL)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Zoho-oauthtoken " + config.getAccessToken())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-com-zoho-billing-organizationid", config.getOrganizationId())
            .build();
    }
    
    @Override
    public CompletableFuture<Subscription> createSubscription(CreateSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Creating subscription for tenant: {}", request.getTenantId());
                
                // Map to Zoho Billing format
                Map<String, Object> zohoRequest = mapper.toZohoSubscriptionRequest(request);
                
                // Call Zoho Billing API
                Map<String, Object> response = webClient.post()
                    .uri("/subscriptions")
                    .bodyValue(zohoRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, request.getTenantId());
                
                logger.info("Successfully created subscription: {} for tenant: {}", 
                    subscription.getExternalSubscriptionId(), request.getTenantId());
                
                return subscription;
                
            } catch (WebClientResponseException e) {
                logger.error("Zoho Billing API error: {}", e.getResponseBodyAsString(), e);
                throw new SubscriptionProviderException("Failed to create subscription: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error creating subscription for tenant: {}", request.getTenantId(), e);
                throw new SubscriptionProviderException("Failed to create subscription", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Subscription>> getSubscription(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching subscription: {}", subscriptionId);
                
                Map<String, Object> response = webClient.get()
                    .uri("/subscriptions/{subscriptionId}", subscriptionId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    return Optional.empty();
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, null);
                
                return Optional.of(subscription);
                
            } catch (WebClientResponseException.NotFound e) {
                logger.debug("Subscription not found: {}", subscriptionId);
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error fetching subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch subscription", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Subscription>> getSubscriptionByTenantId(String tenantId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching subscription for tenant: {}", tenantId);
                
                // Search for subscription by custom field (tenant_id)
                Map<String, Object> searchParams = Map.of(
                    "custom_field_hash", Map.of("tenant_id", tenantId)
                );
                
                Map<String, Object> response = webClient.post()
                    .uri("/subscriptions/search")
                    .bodyValue(searchParams)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscriptions")) {
                    return Optional.empty();
                }
                
                List<Map<String, Object>> subscriptions = (List<Map<String, Object>>) response.get("subscriptions");
                if (subscriptions.isEmpty()) {
                    return Optional.empty();
                }
                
                Map<String, Object> subscriptionData = subscriptions.get(0);
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, tenantId);
                
                return Optional.of(subscription);
                
            } catch (Exception e) {
                logger.error("Error fetching subscription for tenant: {}", tenantId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch subscription by tenant ID", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Subscription> updateSubscription(String subscriptionId, UpdateSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Updating subscription: {}", subscriptionId);
                
                Map<String, Object> zohoRequest = mapper.toZohoUpdateRequest(request);
                
                Map<String, Object> response = webClient.put()
                    .uri("/subscriptions/{subscriptionId}", subscriptionId)
                    .bodyValue(zohoRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, null);
                
                logger.info("Successfully updated subscription: {}", subscriptionId);
                return subscription;
                
            } catch (Exception e) {
                logger.error("Error updating subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to update subscription", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Subscription> cancelSubscription(String subscriptionId, CancelSubscriptionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Cancelling subscription: {}", subscriptionId);
                
                Map<String, Object> zohoRequest = Map.of(
                    "cancel_at_end", request.getCancelAtEndOfPeriod(),
                    "reason", request.getReason()
                );
                
                Map<String, Object> response = webClient.post()
                    .uri("/subscriptions/{subscriptionId}/cancel", subscriptionId)
                    .bodyValue(zohoRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, null);
                
                logger.info("Successfully cancelled subscription: {}", subscriptionId);
                return subscription;
                
            } catch (Exception e) {
                logger.error("Error cancelling subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to cancel subscription", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Subscription> reactivateSubscription(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Reactivating subscription: {}", subscriptionId);
                
                Map<String, Object> response = webClient.post()
                    .uri("/subscriptions/{subscriptionId}/reactivate", subscriptionId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, null);
                
                logger.info("Successfully reactivated subscription: {}", subscriptionId);
                return subscription;
                
            } catch (Exception e) {
                logger.error("Error reactivating subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to reactivate subscription", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Subscription> changePlan(String subscriptionId, ChangePlanRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Changing plan for subscription: {}", subscriptionId);
                
                Map<String, Object> zohoRequest = Map.of(
                    "plan", Map.of("plan_code", request.getNewPlanId()),
                    "quantity", request.getQuantity(),
                    "effective_from", request.getEffectiveDate().format(DATE_FORMATTER)
                );
                
                Map<String, Object> response = webClient.post()
                    .uri("/subscriptions/{subscriptionId}/changeplan", subscriptionId)
                    .bodyValue(zohoRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("subscription")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> subscriptionData = (Map<String, Object>) response.get("subscription");
                Subscription subscription = mapper.fromZohoSubscription(subscriptionData, null);
                
                logger.info("Successfully changed plan for subscription: {}", subscriptionId);
                return subscription;
                
            } catch (Exception e) {
                logger.error("Error changing plan for subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to change plan", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Plan>> getAvailablePlans() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching available plans from Zoho Billing");
                
                Map<String, Object> response = webClient.get()
                    .uri("/plans")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("plans")) {
                    return new ArrayList<>();
                }
                
                List<Map<String, Object>> plansData = (List<Map<String, Object>>) response.get("plans");
                List<Plan> plans = new ArrayList<>();
                
                for (Map<String, Object> planData : plansData) {
                    Plan plan = mapper.fromZohoPlan(planData);
                    if (plan.isActive()) {
                        plans.add(plan);
                    }
                }
                
                logger.debug("Found {} active plans", plans.size());
                return plans;
                
            } catch (Exception e) {
                logger.error("Error fetching available plans", e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch available plans", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Plan>> getPlan(String planId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching plan: {}", planId);
                
                Map<String, Object> response = webClient.get()
                    .uri("/plans/{planId}", planId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("plan")) {
                    return Optional.empty();
                }
                
                Map<String, Object> planData = (Map<String, Object>) response.get("plan");
                Plan plan = mapper.fromZohoPlan(planData);
                
                return Optional.of(plan);
                
            } catch (WebClientResponseException.NotFound e) {
                logger.debug("Plan not found: {}", planId);
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error fetching plan: {}", planId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch plan", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<CheckoutSession> createCheckoutSession(CreateCheckoutSessionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Creating checkout session for tenant: {}", request.getTenantId());
                
                Map<String, Object> zohoRequest = mapper.toZohoHostedPageRequest(request);
                
                Map<String, Object> response = webClient.post()
                    .uri("/hostedpages/newsubscription")
                    .bodyValue(zohoRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("hostedpage")) {
                    throw new SubscriptionProviderException("Invalid response from Zoho Billing API");
                }
                
                Map<String, Object> hostedPageData = (Map<String, Object>) response.get("hostedpage");
                CheckoutSession checkoutSession = mapper.fromZohoHostedPage(hostedPageData);
                
                logger.info("Successfully created checkout session: {}", checkoutSession.getId());
                return checkoutSession;
                
            } catch (Exception e) {
                logger.error("Error creating checkout session for tenant: {}", request.getTenantId(), e);
                throw new CompletionException(new SubscriptionProviderException("Failed to create checkout session", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<CheckoutSession>> getCheckoutSession(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching checkout session: {}", sessionId);
                
                Map<String, Object> response = webClient.get()
                    .uri("/hostedpages/{sessionId}", sessionId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("hostedpage")) {
                    return Optional.empty();
                }
                
                Map<String, Object> hostedPageData = (Map<String, Object>) response.get("hostedpage");
                CheckoutSession checkoutSession = mapper.fromZohoHostedPage(hostedPageData);
                
                return Optional.of(checkoutSession);
                
            } catch (WebClientResponseException.NotFound e) {
                logger.debug("Checkout session not found: {}", sessionId);
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error fetching checkout session: {}", sessionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch checkout session", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<WebhookEvent> processWebhookEvent(String payload, String signature) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Processing webhook event from Zoho Billing");
                
                // Validate webhook signature
                if (!validateWebhookSignature(payload, signature)) {
                    throw new SubscriptionProviderException("Invalid webhook signature");
                }
                
                // Parse webhook payload
                WebhookEvent webhookEvent = mapper.fromZohoWebhook(payload);
                
                logger.info("Successfully processed webhook event: {}", webhookEvent.getEventId());
                return webhookEvent;
                
            } catch (Exception e) {
                logger.error("Error processing webhook event", e);
                throw new CompletionException(new SubscriptionProviderException("Failed to process webhook event", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<UsageQuota> getUsageQuota(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching usage quota for subscription: {}", subscriptionId);
                
                // Zoho Billing doesn't have built-in usage tracking, so we return basic quota info
                // based on the subscription plan
                Optional<Subscription> subscriptionOpt = getSubscription(subscriptionId).join();
                if (subscriptionOpt.isEmpty()) {
                    throw new SubscriptionProviderException("Subscription not found: " + subscriptionId);
                }
                
                Subscription subscription = subscriptionOpt.get();
                Optional<Plan> planOpt = getPlan(subscription.getPlanId()).join();
                
                if (planOpt.isEmpty()) {
                    throw new SubscriptionProviderException("Plan not found: " + subscription.getPlanId());
                }
                
                Plan plan = planOpt.get();
                
                UsageQuota usageQuota = new UsageQuota();
                usageQuota.setSubscriptionId(subscriptionId);
                usageQuota.setMaxUsers(plan.getMaxUsers());
                usageQuota.setMaxStorageGB(plan.getMaxStorageGB());
                usageQuota.setMaxApiCallsPerMonth(plan.getMaxApiCallsPerMonth());
                
                // Set current usage to 0 as Zoho doesn't track this
                usageQuota.setCurrentUsers(0);
                usageQuota.setCurrentStorageGB(0L);
                usageQuota.setCurrentApiCallsThisMonth(0L);
                
                return usageQuota;
                
            } catch (Exception e) {
                logger.error("Error fetching usage quota for subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch usage quota", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> recordUsage(String subscriptionId, RecordUsageRequest request) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Recording usage for subscription: {}", subscriptionId);
                
                // Zoho Billing doesn't support usage-based billing, so we just log the usage
                // In a real implementation, you might want to store this in your own database
                logger.info("Usage recorded - Subscription: {}, Users: {}, Storage: {}GB, API Calls: {}", 
                    subscriptionId, request.getUserCount(), request.getStorageGB(), request.getApiCalls());
                
            } catch (Exception e) {
                logger.error("Error recording usage for subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to record usage", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Invoice>> getBillingHistory(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching billing history for subscription: {}", subscriptionId);
                
                Map<String, Object> response = webClient.get()
                    .uri("/subscriptions/{subscriptionId}/invoices", subscriptionId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("invoices")) {
                    return new ArrayList<>();
                }
                
                List<Map<String, Object>> invoicesData = (List<Map<String, Object>>) response.get("invoices");
                List<Invoice> invoices = new ArrayList<>();
                
                for (Map<String, Object> invoiceData : invoicesData) {
                    Invoice invoice = mapper.fromZohoInvoice(invoiceData);
                    invoices.add(invoice);
                }
                
                logger.debug("Found {} invoices for subscription: {}", invoices.size(), subscriptionId);
                return invoices;
                
            } catch (Exception e) {
                logger.error("Error fetching billing history for subscription: {}", subscriptionId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch billing history", e));
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Invoice>> getInvoice(String invoiceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching invoice: {}", invoiceId);
                
                Map<String, Object> response = webClient.get()
                    .uri("/invoices/{invoiceId}", invoiceId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (response == null || !response.containsKey("invoice")) {
                    return Optional.empty();
                }
                
                Map<String, Object> invoiceData = (Map<String, Object>) response.get("invoice");
                Invoice invoice = mapper.fromZohoInvoice(invoiceData);
                
                return Optional.of(invoice);
                
            } catch (WebClientResponseException.NotFound e) {
                logger.debug("Invoice not found: {}", invoiceId);
                return Optional.empty();
            } catch (Exception e) {
                logger.error("Error fetching invoice: {}", invoiceId, e);
                throw new CompletionException(new SubscriptionProviderException("Failed to fetch invoice", e));
            }
        });
    }
    
    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        return signatureValidator.validateSignature(payload, signature, config.getWebhookSecret());
    }
    
    @Override
    public ProviderConfig getProviderConfig() {
        ProviderConfig config = new ProviderConfig();
        config.setProviderName("Zoho Billing");
        config.setApiVersion("v1");
        config.setBaseUrl(API_BASE_URL);
        config.setWebhookUrl("/webhooks/zoho-billing");
        config.setSupportedFeatures(Arrays.asList(
            "subscription_management",
            "plan_management", 
            "hosted_checkout",
            "webhooks",
            "billing_history"
        ));
        return config;
    }
}
