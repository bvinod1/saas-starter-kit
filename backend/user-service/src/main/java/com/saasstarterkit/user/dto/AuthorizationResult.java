package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for authorization results.
 */
public class AuthorizationResult {
    
    private boolean authorized;
    private String userId;
    private String tenantId;
    private String resource;
    private String action;
    private List<String> allowedActions;
    private Map<String, Object> context;
    private String reason;
    private LocalDateTime decisionTime;
    
    public AuthorizationResult() {}
    
    public AuthorizationResult(boolean authorized, String userId, String tenantId, String resource,
                              String action, List<String> allowedActions, Map<String, Object> context,
                              String reason, LocalDateTime decisionTime) {
        this.authorized = authorized;
        this.userId = userId;
        this.tenantId = tenantId;
        this.resource = resource;
        this.action = action;
        this.allowedActions = allowedActions;
        this.context = context;
        this.reason = reason;
        this.decisionTime = decisionTime;
    }
    
    // Getters and Setters
    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public List<String> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getDecisionTime() { return decisionTime; }
    public void setDecisionTime(LocalDateTime decisionTime) { this.decisionTime = decisionTime; }
}
