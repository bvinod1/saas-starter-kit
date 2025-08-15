package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for tenant session information.
 */
public class TenantSessionDTO {
    
    private String sessionId;
    private UUID userId;
    private String tenantId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
    private String status;
    private Map<String, Object> sessionData;
    private String ipAddress;
    private String userAgent;
    
    public TenantSessionDTO() {}
    
    public TenantSessionDTO(String sessionId, UUID userId, String tenantId, String username,
                           LocalDateTime createdAt, LocalDateTime lastAccessedAt, LocalDateTime expiresAt,
                           String status, Map<String, Object> sessionData, String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.sessionData = sessionData;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, Object> getSessionData() { return sessionData; }
    public void setSessionData(Map<String, Object> sessionData) { this.sessionData = sessionData; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
