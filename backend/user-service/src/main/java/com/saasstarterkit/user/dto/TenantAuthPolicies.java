package com.saasstarterkit.user.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for tenant authentication policies.
 */
public class TenantAuthPolicies {
    
    private String tenantId;
    private String policyVersion;
    private Map<String, Object> passwordPolicy;
    private Map<String, Object> sessionPolicy;
    private Map<String, Object> mfaPolicy;
    private List<String> allowedAuthMethods;
    private Map<String, Object> rateLimiting;
    private Map<String, Object> lockoutPolicy;
    
    public TenantAuthPolicies() {}
    
    public TenantAuthPolicies(String tenantId, String policyVersion, Map<String, Object> passwordPolicy,
                             Map<String, Object> sessionPolicy, Map<String, Object> mfaPolicy,
                             List<String> allowedAuthMethods, Map<String, Object> rateLimiting,
                             Map<String, Object> lockoutPolicy) {
        this.tenantId = tenantId;
        this.policyVersion = policyVersion;
        this.passwordPolicy = passwordPolicy;
        this.sessionPolicy = sessionPolicy;
        this.mfaPolicy = mfaPolicy;
        this.allowedAuthMethods = allowedAuthMethods;
        this.rateLimiting = rateLimiting;
        this.lockoutPolicy = lockoutPolicy;
    }
    
    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
    
    public Map<String, Object> getPasswordPolicy() { return passwordPolicy; }
    public void setPasswordPolicy(Map<String, Object> passwordPolicy) { this.passwordPolicy = passwordPolicy; }
    
    public Map<String, Object> getSessionPolicy() { return sessionPolicy; }
    public void setSessionPolicy(Map<String, Object> sessionPolicy) { this.sessionPolicy = sessionPolicy; }
    
    public Map<String, Object> getMfaPolicy() { return mfaPolicy; }
    public void setMfaPolicy(Map<String, Object> mfaPolicy) { this.mfaPolicy = mfaPolicy; }
    
    public List<String> getAllowedAuthMethods() { return allowedAuthMethods; }
    public void setAllowedAuthMethods(List<String> allowedAuthMethods) { this.allowedAuthMethods = allowedAuthMethods; }
    
    public Map<String, Object> getRateLimiting() { return rateLimiting; }
    public void setRateLimiting(Map<String, Object> rateLimiting) { this.rateLimiting = rateLimiting; }
    
    public Map<String, Object> getLockoutPolicy() { return lockoutPolicy; }
    public void setLockoutPolicy(Map<String, Object> lockoutPolicy) { this.lockoutPolicy = lockoutPolicy; }
}
