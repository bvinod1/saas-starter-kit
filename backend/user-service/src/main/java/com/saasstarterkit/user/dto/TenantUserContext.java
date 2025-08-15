package com.saasstarterkit.user.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for tenant user context.
 */
public class TenantUserContext {
    
    private UUID userId;
    private String tenantId;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    private Map<String, Object> attributes;
    private String sessionId;
    private String authMethod;
    
    public TenantUserContext() {}
    
    public TenantUserContext(UUID userId, String tenantId, String username, String email,
                            List<String> roles, List<String> permissions, Map<String, Object> attributes,
                            String sessionId, String authMethod) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
        this.attributes = attributes;
        this.sessionId = sessionId;
        this.authMethod = authMethod;
    }
    
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getAuthMethod() { return authMethod; }
    public void setAuthMethod(String authMethod) { this.authMethod = authMethod; }
}
