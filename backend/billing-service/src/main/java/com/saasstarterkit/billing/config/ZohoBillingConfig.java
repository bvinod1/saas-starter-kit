package com.saasstarterkit.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for Zoho Billing integration
 */
@Configuration
@ConfigurationProperties(prefix = "zoho.billing")
@Validated
public class ZohoBillingConfig {
    
    @NotBlank(message = "Zoho Billing access token is required")
    private String accessToken;
    
    @NotBlank(message = "Zoho Billing organization ID is required")
    private String organizationId;
    
    @NotBlank(message = "Zoho Billing webhook secret is required")
    private String webhookSecret;
    
    @NotBlank(message = "Zoho Billing API base URL is required")
    private String apiBaseUrl = "https://www.zohoapis.com/billing/v1";
    
    @NotNull(message = "Zoho Billing API timeout is required")
    private Integer apiTimeout = 30000; // 30 seconds
    
    @NotNull(message = "Zoho Billing retry attempts is required")
    private Integer retryAttempts = 3;
    
    @NotNull(message = "Zoho Billing retry delay is required")
    private Integer retryDelay = 1000; // 1 second
    
    @NotNull(message = "Zoho Billing webhook validation enabled flag is required")
    private Boolean webhookValidationEnabled = true;
    
    @NotBlank(message = "Zoho Billing default currency is required")
    private String defaultCurrency = "USD";
    
    @NotBlank(message = "Zoho Billing default timezone is required")
    private String defaultTimezone = "UTC";
    
    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    
    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    
    public Integer getApiTimeout() { return apiTimeout; }
    public void setApiTimeout(Integer apiTimeout) { this.apiTimeout = apiTimeout; }
    
    public Integer getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(Integer retryAttempts) { this.retryAttempts = retryAttempts; }
    
    public Integer getRetryDelay() { return retryDelay; }
    public void setRetryDelay(Integer retryDelay) { this.retryDelay = retryDelay; }
    
    public Boolean getWebhookValidationEnabled() { return webhookValidationEnabled; }
    public void setWebhookValidationEnabled(Boolean webhookValidationEnabled) { this.webhookValidationEnabled = webhookValidationEnabled; }
    
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    
    public String getDefaultTimezone() { return defaultTimezone; }
    public void setDefaultTimezone(String defaultTimezone) { this.defaultTimezone = defaultTimezone; }
}
