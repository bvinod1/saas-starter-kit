package com.saasstarterkit.notification.service.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Webhook channel provider implementation
 * Handles sending notifications to external HTTP endpoints
 */
@Service
public class WebhookChannelProvider implements NotificationChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookChannelProvider.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${notification.providers.webhook.enabled:false}")
    private boolean webhookEnabled;
    
    @Value("${notification.providers.webhook.timeout-seconds:30}")
    private int timeoutSeconds;
    
    @Value("${notification.providers.webhook.max-retries:3}")
    private int maxRetries;
    
    @Value("${notification.providers.webhook.rate-limit-per-minute:60}")
    private int rateLimitPerMinute;
    
    @Value("${notification.providers.webhook.default-headers:}")
    private String defaultHeaders;
    
    // =============================================
    // INTERFACE IMPLEMENTATION
    // =============================================
    
    @Override
    public Notification.NotificationChannel getChannelType() {
        return Notification.NotificationChannel.WEBHOOK;
    }
    
    @Override
    public boolean canHandle(NotificationEvent event, UserNotificationPreferences preferences) {
        return preferences != null && 
               preferences.getWebhookEnabled() != null && 
               preferences.getWebhookEnabled() &&
               hasWebhookEndpoints(preferences);
    }
    
    @Override
    public DeliveryResult send(Notification notification, UserNotificationPreferences preferences) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate the notification
            ValidationResult validation = validate(notification, preferences);
            if (!validation.isValid()) {
                return DeliveryResult.failure()
                        .errorMessage(validation.getErrorMessage())
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Check if webhook notifications are enabled
            if (!webhookEnabled) {
                logger.warn("Webhook notification provider is disabled");
                return DeliveryResult.failure()
                        .errorMessage("Webhook notifications are disabled")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Send webhook notification
            return sendWebhookNotification(notification, preferences, startTime);
            
        } catch (Exception e) {
            logger.error("Error sending webhook notification: {}", notification.getId(), e);
            return DeliveryResult.failure()
                    .errorMessage("Failed to send webhook notification: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Check if webhook provider is enabled and RestTemplate is available
            return webhookEnabled && restTemplate != null;
        } catch (Exception e) {
            logger.warn("Webhook provider health check failed", e);
            return false;
        }
    }
    
    @Override
    public ChannelConfiguration getConfiguration() {
        return ChannelConfiguration.builder()
                .providerName("Webhook HTTP")
                .version("1.0.0")
                .enabled(webhookEnabled)
                .maxRetries(maxRetries)
                .retryDelayMs(1000)
                .rateLimitPerMinute(rateLimitPerMinute)
                .build();
    }
    
    @Override
    public ValidationResult validate(Notification notification, UserNotificationPreferences preferences) {
        // Check if webhook notifications are enabled for user
        if (preferences == null || !preferences.getWebhookEnabled()) {
            return ValidationResult.invalid()
                    .errorMessage("Webhook notifications are disabled for this user")
                    .build();
        }
        
        // Check if user has webhook endpoints
        if (!hasWebhookEndpoints(preferences)) {
            return ValidationResult.invalid()
                    .errorMessage("No webhook endpoints configured for this user")
                    .build();
        }
        
        // Check message content
        if (!StringUtils.hasText(notification.getMessage())) {
            return ValidationResult.invalid()
                    .errorMessage("Webhook notification message content is required")
                    .build();
        }
        
        return ValidationResult.valid().build();
    }
    
    @Override
    public long getEstimatedDeliveryTimeMs() {
        // Webhook delivery time depends on external service response
        return timeoutSeconds * 1000L; // Convert seconds to milliseconds
    }
    
    @Override
    public ChannelMetadata getMetadata() {
        return ChannelMetadata.builder()
                .providerName("Webhook HTTP")
                .version("1.0.0")
                .region("External")
                .supportsBulk(false)
                .supportsScheduling(true)
                .supportsTracking(false)
                .maxMessageSize(1024 * 1024) // 1MB
                .supportedFormats("json,xml,text")
                .build();
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Send webhook notification to external endpoints
     */
    private DeliveryResult sendWebhookNotification(Notification notification, 
                                                 UserNotificationPreferences preferences, 
                                                 long startTime) {
        try {
            // Get webhook endpoints
            List<String> webhookEndpoints = getWebhookEndpoints(preferences);
            if (webhookEndpoints.isEmpty()) {
                return DeliveryResult.failure()
                        .errorMessage("No webhook endpoints available")
                        .deliveryTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // Build webhook payload
            Map<String, Object> webhookPayload = buildWebhookPayload(notification, preferences);
            
            // Build headers
            HttpHeaders headers = buildWebhookHeaders(preferences);
            
            // Send to all endpoints
            List<CompletableFuture<WebhookResponse>> futures = webhookEndpoints.stream()
                    .map(endpoint -> sendToEndpoint(endpoint, webhookPayload, headers))
                    .toList();
            
            // Wait for all responses
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds, TimeUnit.SECONDS);
            
            // Collect results
            List<WebhookResponse> responses = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            
            // Analyze results
            long deliveryTime = System.currentTimeMillis() - startTime;
            boolean allSuccessful = responses.stream().allMatch(WebhookResponse::isSuccess);
            
            if (allSuccessful) {
                logger.info("Webhook notification sent successfully to {} endpoints in {}ms", 
                           webhookEndpoints.size(), deliveryTime);
                
                return DeliveryResult.success()
                        .messageId("webhook_" + System.currentTimeMillis())
                        .deliveryTimeMs(deliveryTime)
                        .providerResponse("Webhook notification delivered to " + webhookEndpoints.size() + " endpoints")
                        .metadata(getMetadata())
                        .build();
            } else {
                // Some endpoints failed
                long failedCount = responses.stream().filter(r -> !r.isSuccess()).count();
                logger.warn("Webhook notification partially failed: {} of {} endpoints failed", 
                           failedCount, webhookEndpoints.size());
                
                return DeliveryResult.failure()
                        .errorMessage("Webhook notification partially failed: " + failedCount + " of " + 
                                    webhookEndpoints.size() + " endpoints failed")
                        .deliveryTimeMs(deliveryTime)
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to send webhook notification", e);
            return DeliveryResult.failure()
                    .errorMessage("Webhook notification delivery failed: " + e.getMessage())
                    .deliveryTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Send webhook to a specific endpoint
     */
    private CompletableFuture<WebhookResponse> sendToEndpoint(String endpoint, 
                                                            Map<String, Object> payload, 
                                                            HttpHeaders headers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                
                logger.debug("Sending webhook to endpoint: {}", endpoint);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
                );
                
                return new WebhookResponse(endpoint, true, response.getStatusCodeValue(), 
                                        response.getBody(), null);
                
            } catch (Exception e) {
                logger.error("Failed to send webhook to endpoint: {}", endpoint, e);
                return new WebhookResponse(endpoint, false, 0, null, e.getMessage());
            }
        });
    }
    
    /**
     * Build webhook payload
     */
    private Map<String, Object> buildWebhookPayload(Notification notification, 
                                                   UserNotificationPreferences preferences) {
        return Map.of(
            "notificationId", notification.getId().toString(),
            "type", "WEBHOOK",
            "title", notification.getTitle() != null ? notification.getTitle() : "Notification",
            "message", notification.getMessage(),
            "priority", notification.getPriority().toString(),
            "category", notification.getSourceEvent() != null ? notification.getSourceEvent() : "general",
            "timestamp", LocalDateTime.now().toString(),
            "userId", notification.getUserId(),
            "tenantId", notification.getTenantId(),
            "organizationId", notification.getOrganizationId() != null ? notification.getOrganizationId() : "",
            "data", notification.getData() != null ? notification.getData() : "",
            "sourceEvent", notification.getSourceEvent() != null ? notification.getSourceEvent() : "",
            "correlationId", notification.getCorrelationId() != null ? notification.getCorrelationId() : ""
        );
    }
    
    /**
     * Build webhook headers
     */
    private HttpHeaders buildWebhookHeaders(UserNotificationPreferences preferences) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "SaaS-Starter-Kit-Notification-Service/1.0");
        headers.set("X-Notification-Type", "webhook");
        headers.set("X-Notification-Timestamp", String.valueOf(System.currentTimeMillis()));
        
        // Add custom headers if configured
        if (StringUtils.hasText(defaultHeaders)) {
            try {
                Map<String, String> customHeaders = objectMapper.readValue(defaultHeaders, Map.class);
                customHeaders.forEach(headers::set);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse default webhook headers", e);
            }
        }
        
        return headers;
    }
    
    /**
     * Check if user has webhook endpoints
     */
    private boolean hasWebhookEndpoints(UserNotificationPreferences preferences) {
        try {
            if (preferences.getWebhookEndpoints() == null) {
                return false;
            }
            
            List<String> endpoints = objectMapper.readValue(
                preferences.getWebhookEndpoints(), 
                List.class
            );
            
            return endpoints != null && !endpoints.isEmpty();
            
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse webhook endpoints for user", e);
            return false;
        }
    }
    
    /**
     * Get webhook endpoints from user preferences
     */
    private List<String> getWebhookEndpoints(UserNotificationPreferences preferences) {
        try {
            if (preferences.getWebhookEndpoints() == null) {
                return List.of();
            }
            
            return objectMapper.readValue(
                preferences.getWebhookEndpoints(), 
                List.class
            );
            
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse webhook endpoints for user", e);
            return List.of();
        }
    }
    
    /**
     * Validate webhook endpoint URL
     */
    private boolean isValidWebhookUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        
        // Basic URL validation
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    /**
     * Filter valid webhook endpoints
     */
    private List<String> filterValidEndpoints(List<String> endpoints) {
        return endpoints.stream()
                .filter(this::isValidWebhookUrl)
                .toList();
    }
    
    // =============================================
    // INNER CLASSES
    // =============================================
    
    /**
     * Webhook response wrapper
     */
    private static class WebhookResponse {
        private final String endpoint;
        private final boolean success;
        private final int statusCode;
        private final String responseBody;
        private final String errorMessage;
        
        public WebhookResponse(String endpoint, boolean success, int statusCode, 
                             String responseBody, String errorMessage) {
            this.endpoint = endpoint;
            this.success = success;
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getEndpoint() { return endpoint; }
        public int getStatusCode() { return statusCode; }
        public String getResponseBody() { return responseBody; }
        public String getErrorMessage() { return errorMessage; }
    }
}
