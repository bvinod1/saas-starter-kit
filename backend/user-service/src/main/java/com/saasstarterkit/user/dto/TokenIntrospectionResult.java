package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for token introspection results.
 */
public class TokenIntrospectionResult {
    
    private boolean active;
    private UUID userId;
    private String tenantId;
    private String username;
    private List<String> scopes;
    private List<String> roles;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String tokenType;
    private Map<String, Object> claims;
    
    public TokenIntrospectionResult() {}
    
    public TokenIntrospectionResult(boolean active, UUID userId, String tenantId, String username,
                                   List<String> scopes, List<String> roles, LocalDateTime issuedAt,
                                   LocalDateTime expiresAt, String tokenType, Map<String, Object> claims) {
        this.active = active;
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.scopes = scopes;
        this.roles = roles;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.tokenType = tokenType;
        this.claims = claims;
    }
    
    // Getters and Setters
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Map<String, Object> getClaims() { return claims; }
    public void setClaims(Map<String, Object> claims) { this.claims = claims; }
}
