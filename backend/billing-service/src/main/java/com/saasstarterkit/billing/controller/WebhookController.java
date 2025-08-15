package com.saasstarterkit.billing.controller;

import com.saasstarterkit.billing.service.SubscriptionService;
import com.saasstarterkit.billing.domain.model.WebhookEvent;
import com.saasstarterkit.billing.exception.SubscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling webhook events from subscription providers
 */
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final SubscriptionService subscriptionService;
    
    public WebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    /**
     * Handle Zoho Billing webhooks
     */
    @PostMapping("/zoho-billing")
    public CompletableFuture<ResponseEntity<String>> handleZohoBillingWebhook(
            HttpServletRequest request,
            @RequestBody String payload) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Received webhook from Zoho Billing");
                
                // Extract signature from headers
                String signature = extractSignature(request);
                if (signature == null) {
                    logger.warn("No signature found in webhook request");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
                }
                
                // Process webhook event
                WebhookEvent webhookEvent = subscriptionService.processWebhookEvent(payload, signature).join();
                
                logger.info("Successfully processed webhook event: {}", webhookEvent.getEventId());
                return ResponseEntity.ok("Webhook processed successfully");
                
            } catch (SubscriptionException e) {
                logger.error("Error processing Zoho Billing webhook", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error processing Zoho Billing webhook", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
            }
        });
    }
    
    /**
     * Handle Stripe webhooks (for future use)
     */
    @PostMapping("/stripe")
    public CompletableFuture<ResponseEntity<String>> handleStripeWebhook(
            HttpServletRequest request,
            @RequestBody String payload) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Received webhook from Stripe");
                
                // Extract signature from headers
                String signature = extractSignature(request);
                if (signature == null) {
                    logger.warn("No signature found in webhook request");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
                }
                
                // TODO: Implement Stripe webhook processing
                logger.info("Stripe webhook processing not yet implemented");
                return ResponseEntity.ok("Webhook received (processing not implemented)");
                
            } catch (Exception e) {
                logger.error("Error processing Stripe webhook", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
            }
        });
    }
    
    /**
     * Handle generic subscription provider webhooks
     */
    @PostMapping("/{provider}")
    public CompletableFuture<ResponseEntity<String>> handleGenericWebhook(
            @PathVariable String provider,
            HttpServletRequest request,
            @RequestBody String payload) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Received webhook from provider: {}", provider);
                
                // Extract signature from headers
                String signature = extractSignature(request);
                if (signature == null) {
                    logger.warn("No signature found in webhook request");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
                }
                
                // Route to appropriate provider based on path
                switch (provider.toLowerCase()) {
                    case "zoho-billing":
                    case "zoho":
                        return handleZohoBillingWebhook(request, payload).join();
                    case "stripe":
                        return handleStripeWebhook(request, payload).join();
                    default:
                        logger.warn("Unsupported webhook provider: {}", provider);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported provider: " + provider);
                }
                
            } catch (Exception e) {
                logger.error("Error processing webhook from provider: {}", provider, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
            }
        });
    }
    
    /**
     * Health check endpoint for webhooks
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook service is healthy");
    }
    
    /**
     * Extract signature from request headers
     */
    private String extractSignature(HttpServletRequest request) {
        // Try different header names that providers might use
        String signature = request.getHeader("X-Zoho-Signature");
        if (signature != null) {
            return signature;
        }
        
        signature = request.getHeader("X-Stripe-Signature");
        if (signature != null) {
            return signature;
        }
        
        signature = request.getHeader("X-Webhook-Signature");
        if (signature != null) {
            return signature;
        }
        
        signature = request.getHeader("Signature");
        if (signature != null) {
            return signature;
        }
        
        // For providers that don't use signature headers, return a default value
        // In production, you should enforce signature validation for security
        return "no-signature";
    }
}
