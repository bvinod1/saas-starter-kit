package com.saasstarterkit.user.service.secrets;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health status information for a secret manager.
 */
public class SecretManagerHealth {
    
    public enum Status {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }
    
    private final Status status;
    private final String message;
    private final LocalDateTime lastCheck;
    private final long responseTimeMs;
    private final Map<String, Object> details;
    private final Exception lastError;
    
    private SecretManagerHealth(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.lastCheck = builder.lastCheck;
        this.responseTimeMs = builder.responseTimeMs;
        this.details = builder.details;
        this.lastError = builder.lastError;
    }
    
    // Getters
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getLastCheck() { return lastCheck; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public Map<String, Object> getDetails() { return details; }
    public Exception getLastError() { return lastError; }
    
    // Convenience methods
    public boolean isHealthy() { return status == Status.HEALTHY; }
    public boolean isDegraded() { return status == Status.DEGRADED; }
    public boolean isUnhealthy() { return status == Status.UNHEALTHY; }
    public boolean isUnknown() { return status == Status.UNKNOWN; }
    
    // Builder pattern
    public static class Builder {
        private Status status = Status.UNKNOWN;
        private String message = "Health check not performed";
        private LocalDateTime lastCheck = LocalDateTime.now();
        private long responseTimeMs = 0;
        private Map<String, Object> details;
        private Exception lastError;
        
        public Builder status(Status status) {
            this.status = status;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder lastCheck(LocalDateTime lastCheck) {
            this.lastCheck = lastCheck;
            return this;
        }
        
        public Builder responseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }
        
        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }
        
        public Builder lastError(Exception lastError) {
            this.lastError = lastError;
            return this;
        }
        
        public SecretManagerHealth build() {
            return new SecretManagerHealth(this);
        }
    }
    
    // Static factory methods for common health states
    public static SecretManagerHealth healthy(String message) {
        return new Builder()
                .status(Status.HEALTHY)
                .message(message)
                .lastCheck(LocalDateTime.now())
                .build();
    }
    
    public static SecretManagerHealth healthy(String message, long responseTimeMs) {
        return new Builder()
                .status(Status.HEALTHY)
                .message(message)
                .lastCheck(LocalDateTime.now())
                .responseTimeMs(responseTimeMs)
                .build();
    }
    
    public static SecretManagerHealth degraded(String message) {
        return new Builder()
                .status(Status.DEGRADED)
                .message(message)
                .lastCheck(LocalDateTime.now())
                .build();
    }
    
    public static SecretManagerHealth unhealthy(String message, Exception error) {
        return new Builder()
                .status(Status.UNHEALTHY)
                .message(message)
                .lastCheck(LocalDateTime.now())
                .lastError(error)
                .build();
    }
    
    public static SecretManagerHealth unknown(String message) {
        return new Builder()
                .status(Status.UNKNOWN)
                .message(message)
                .lastCheck(LocalDateTime.now())
                .build();
    }
    
    @Override
    public String toString() {
        return "SecretManagerHealth{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", lastCheck=" + lastCheck +
                ", responseTimeMs=" + responseTimeMs +
                ", hasError=" + (lastError != null) +
                '}';
    }
}
