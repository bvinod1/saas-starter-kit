package com.saasstarterkit.user.dto;

import com.saasstarterkit.user.security.TenantUserContext;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * DTO representing the result of tenant authentication operations
 * 
 * @author SaaS Starter Kit Team
 */
public class TenantAuthResult {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("user_context")
    private TenantUserContext userContext;
    
    @JsonProperty("session")
    private TenantSessionDTO session;
    
    @JsonProperty("tenant_id")
    private String tenantId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_details")
    private String errorDetails;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public TenantAuthResult() {
        this.timestamp = Instant.now();
    }
    
    public TenantAuthResult(boolean success) {
        this();
        this.success = success;
    }
    
    // =============================================
    // BUILDER PATTERN
    // =============================================
    
    public static class Builder {
        private TenantAuthResult result;
        
        public Builder() {
            result = new TenantAuthResult();
        }
        
        public Builder success(boolean success) {
            result.success = success;
            return this;
        }
        
        public Builder accessToken(String accessToken) {
            result.accessToken = accessToken;
            return this;
        }
        
        public Builder refreshToken(String refreshToken) {
            result.refreshToken = refreshToken;
            return this;
        }
        
        public Builder tokenType(String tokenType) {
            result.tokenType = tokenType;
            return this;
        }
        
        public Builder expiresIn(Long expiresIn) {
            result.expiresIn = expiresIn;
            return this;
        }
        
        public Builder userContext(TenantUserContext userContext) {
            result.userContext = userContext;
            return this;
        }
        
        public Builder session(TenantSessionDTO session) {
            result.session = session;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            result.tenantId = tenantId;
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            result.errorCode = errorCode;
            return this;
        }
        
        public Builder errorDetails(String errorDetails) {
            result.errorDetails = errorDetails;
            return this;
        }
        
        public TenantAuthResult build() {
            return result;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // =============================================
    // UTILITY METHODS
    // =============================================
    
    /**
     * Check if authentication was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Check if authentication failed
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * Get token expiration time
     */
    public Instant getTokenExpiration() {
        if (userContext != null && userContext.getExpirationTime() != null) {
            return userContext.getExpirationTime();
        }
        if (expiresIn != null) {
            return Instant.now().plusSeconds(expiresIn);
        }
        return null;
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        Instant expiration = getTokenExpiration();
        return expiration != null && Instant.now().isAfter(expiration);
    }
    
    /**
     * Get time until token expires in seconds
     */
    public Long getTimeUntilExpiration() {
        Instant expiration = getTokenExpiration();
        if (expiration == null) {
            return null;
        }
        return Math.max(0, expiration.getEpochSecond() - Instant.now().getEpochSecond());
    }
    
    // =============================================
    // GETTERS AND SETTERS
    // =============================================
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    
    public TenantUserContext getUserContext() { return userContext; }
    public void setUserContext(TenantUserContext userContext) { this.userContext = userContext; }
    
    public TenantSessionDTO getSession() { return session; }
    public void setSession(TenantSessionDTO session) { this.session = session; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
}
