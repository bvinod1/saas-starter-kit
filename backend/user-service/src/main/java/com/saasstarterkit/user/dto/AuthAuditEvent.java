package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for authentication audit events.
 */
public class AuthAuditEvent {
    
    private String eventId;
    private UUID userId;
    private String tenantId;
    private String eventType;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private boolean success;
    private String failureReason;
    private Map<String, Object> eventData;
    private String source;
    
    public AuthAuditEvent() {}
    
    public AuthAuditEvent(String eventId, UUID userId, String tenantId, String eventType,
                         LocalDateTime timestamp, String ipAddress, String userAgent, String sessionId,
                         boolean success, String failureReason, Map<String, Object> eventData, String source) {
        this.eventId = eventId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.success = success;
        this.failureReason = failureReason;
        this.eventData = eventData;
        this.source = source;
    }
    
    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public Map<String, Object> getEventData() { return eventData; }
    public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
