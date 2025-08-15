package com.saasstarterkit.user.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comprehensive tenant user context that includes all necessary security claims
 * and tenant information for multi-tenant authentication and authorization.
 * 
 * This class serves as the central data structure for:
 * - User identity within tenant context
 * - Role and permission claims
 * - Tenant-specific attributes
 * - Security metadata
 * - Session information
 * 
 * @author SaaS Starter Kit Team
 */
public class TenantUserContext {
    
    // =============================================
    // CORE IDENTITY CLAIMS (JWT Standard Claims)
    // =============================================
    
    @JsonProperty("sub")
    private String userId;
    
    @JsonProperty("iss")
    private String issuer;
    
    @JsonProperty("aud")
    private String audience;
    
    @JsonProperty("exp")
    private Instant expirationTime;
    
    @JsonProperty("iat")
    private Instant issuedAt;
    
    @JsonProperty("nbf")
    private Instant notBefore;
    
    @JsonProperty("jti")
    private String jwtId;
    
    // =============================================
    // TENANT-SPECIFIC CLAIMS
    // =============================================
    
    @JsonProperty("tid")
    private String tenantId;
    
    @JsonProperty("tname")
    private String tenantName;
    
    @JsonProperty("tslug")
    private String tenantSlug;
    
    @JsonProperty("ttype")
    private String tenantType; // "basic", "standard", "enterprise"
    
    @JsonProperty("tstatus")
    private String tenantStatus; // "active", "suspended", "trial"
    
    // =============================================
    // USER ATTRIBUTES
    // =============================================
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("given_name")
    private String firstName;
    
    @JsonProperty("family_name")
    private String lastName;
    
    @JsonProperty("preferred_username")
    private String preferredUsername;
    
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("phone_number_verified")
    private Boolean phoneNumberVerified;
    
    // =============================================
    // ROLE AND PERMISSION CLAIMS
    // =============================================
    
    @JsonProperty("roles")
    private Set<String> roles;
    
    @JsonProperty("permissions")
    private Set<String> permissions;
    
    @JsonProperty("groups")
    private Set<String> groups;
    
    @JsonProperty("scopes")
    private Set<String> scopes;
    
    // =============================================
    // ORGANIZATION CONTEXT
    // =============================================
    
    @JsonProperty("org_id")
    private String organizationId;
    
    @JsonProperty("org_name")
    private String organizationName;
    
    @JsonProperty("org_role")
    private String organizationRole; // "owner", "admin", "member", "viewer"
    
    @JsonProperty("org_permissions")
    private Set<String> organizationPermissions;
    
    // =============================================
    // SECURITY AND COMPLIANCE CLAIMS
    // =============================================
    
    @JsonProperty("mfa_enabled")
    private Boolean mfaEnabled;
    
    @JsonProperty("mfa_methods")
    private List<String> mfaMethods;
    
    @JsonProperty("last_password_change")
    private Instant lastPasswordChange;
    
    @JsonProperty("failed_login_attempts")
    private Integer failedLoginAttempts;
    
    @JsonProperty("account_locked")
    private Boolean accountLocked;
    
    @JsonProperty("account_locked_until")
    private Instant accountLockedUntil;
    
    @JsonProperty("consent_given")
    private Boolean consentGiven;
    
    @JsonProperty("consent_timestamp")
    private Instant consentTimestamp;
    
    @JsonProperty("data_classification")
    private String dataClassification; // "public", "internal", "confidential", "restricted"
    
    // =============================================
    // SESSION AND TRACKING CLAIMS
    // =============================================
    
    @JsonProperty("sid")
    private String sessionId;
    
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("client_name")
    private String clientName;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("login_timestamp")
    private Instant loginTimestamp;
    
    @JsonProperty("last_activity")
    private Instant lastActivity;
    
    // =============================================
    // CUSTOM AND EXTENDED CLAIMS
    // =============================================
    
    @JsonProperty("custom_claims")
    private Map<String, Object> customClaims;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public TenantUserContext() {}
    
    public TenantUserContext(String userId, String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.issuedAt = Instant.now();
        this.jwtId = java.util.UUID.randomUUID().toString();
    }
    
    // =============================================
    // UTILITY METHODS
    // =============================================
    
    /**
     * Check if the token is expired
     */
    @JsonIgnore
    public boolean isExpired() {
        return expirationTime != null && Instant.now().isAfter(expirationTime);
    }
    
    /**
     * Check if the token is valid (not expired and not before valid time)
     */
    @JsonIgnore
    public boolean isValid() {
        Instant now = Instant.now();
        return !isExpired() && 
               (notBefore == null || now.isAfter(notBefore) || now.equals(notBefore));
    }
    
    /**
     * Check if user has a specific role
     */
    @JsonIgnore
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if user has any of the specified roles
     */
    @JsonIgnore
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) return false;
        for (String role : roles) {
            if (this.roles.contains(role)) return true;
        }
        return false;
    }
    
    /**
     * Check if user has a specific permission
     */
    @JsonIgnore
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Check if user has any of the specified permissions
     */
    @JsonIgnore
    public boolean hasAnyPermission(String... permissions) {
        if (this.permissions == null) return false;
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) return true;
        }
        return false;
    }
    
    /**
     * Check if user is an organization admin
     */
    @JsonIgnore
    public boolean isOrganizationAdmin() {
        return "admin".equals(organizationRole) || "owner".equals(organizationRole);
    }
    
    /**
     * Check if user is a tenant admin
     */
    @JsonIgnore
    public boolean isTenantAdmin() {
        return hasRole("tenant-admin") || hasRole("admin");
    }
    
    /**
     * Check if user is a super admin
     */
    @JsonIgnore
    public boolean isSuperAdmin() {
        return hasRole("super-admin") || hasRole("system-admin");
    }
    
    /**
     * Get user's display name
     */
    @JsonIgnore
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (preferredUsername != null) {
            return preferredUsername;
        } else {
            return username;
        }
    }
    
    /**
     * Get time until token expires in seconds
     */
    @JsonIgnore
    public long getTimeUntilExpiration() {
        if (expirationTime == null) return -1;
        return Math.max(0, expirationTime.getEpochSecond() - Instant.now().getEpochSecond());
    }
    
    // =============================================
    // BUILDER PATTERN
    // =============================================
    
    public static class Builder {
        private TenantUserContext context;
        
        public Builder(String userId, String tenantId) {
            context = new TenantUserContext(userId, tenantId);
        }
        
        public Builder withIssuer(String issuer) {
            context.issuer = issuer;
            return this;
        }
        
        public Builder withAudience(String audience) {
            context.audience = audience;
            return this;
        }
        
        public Builder withExpiration(Instant expiration) {
            context.expirationTime = expiration;
            return this;
        }
        
        public Builder withRoles(Set<String> roles) {
            context.roles = roles;
            return this;
        }
        
        public Builder withPermissions(Set<String> permissions) {
            context.permissions = permissions;
            return this;
        }
        
        public Builder withTenantInfo(String tenantName, String tenantSlug, String tenantType) {
            context.tenantName = tenantName;
            context.tenantSlug = tenantSlug;
            context.tenantType = tenantType;
            return this;
        }
        
        public Builder withUserInfo(String username, String email, String firstName, String lastName) {
            context.username = username;
            context.email = email;
            context.firstName = firstName;
            context.lastName = lastName;
            return this;
        }
        
        public Builder withOrganizationInfo(String orgId, String orgName, String orgRole) {
            context.organizationId = orgId;
            context.organizationName = orgName;
            context.organizationRole = orgRole;
            return this;
        }
        
        public Builder withCustomClaims(Map<String, Object> customClaims) {
            context.customClaims = customClaims;
            return this;
        }
        
        public TenantUserContext build() {
            return context;
        }
    }
    
    // =============================================
    // GETTERS AND SETTERS
    // =============================================
    
    // Core Identity Claims
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    
    public Instant getExpirationTime() { return expirationTime; }
    public void setExpirationTime(Instant expirationTime) { this.expirationTime = expirationTime; }
    
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    
    public Instant getNotBefore() { return notBefore; }
    public void setNotBefore(Instant notBefore) { this.notBefore = notBefore; }
    
    public String getJwtId() { return jwtId; }
    public void setJwtId(String jwtId) { this.jwtId = jwtId; }
    
    // Tenant Claims
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    
    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }
    
    public String getTenantType() { return tenantType; }
    public void setTenantType(String tenantType) { this.tenantType = tenantType; }
    
    public String getTenantStatus() { return tenantStatus; }
    public void setTenantStatus(String tenantStatus) { this.tenantStatus = tenantStatus; }
    
    // User Attributes
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPreferredUsername() { return preferredUsername; }
    public void setPreferredUsername(String preferredUsername) { this.preferredUsername = preferredUsername; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public Boolean getPhoneNumberVerified() { return phoneNumberVerified; }
    public void setPhoneNumberVerified(Boolean phoneNumberVerified) { this.phoneNumberVerified = phoneNumberVerified; }
    
    // Role and Permission Claims
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    
    public Set<String> getGroups() { return groups; }
    public void setGroups(Set<String> groups) { this.groups = groups; }
    
    public Set<String> getScopes() { return scopes; }
    public void setScopes(Set<String> scopes) { this.scopes = scopes; }
    
    // Organization Context
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    
    public String getOrganizationRole() { return organizationRole; }
    public void setOrganizationRole(String organizationRole) { this.organizationRole = organizationRole; }
    
    public Set<String> getOrganizationPermissions() { return organizationPermissions; }
    public void setOrganizationPermissions(Set<String> organizationPermissions) { this.organizationPermissions = organizationPermissions; }
    
    // Security and Compliance Claims
    public Boolean getMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(Boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
    
    public List<String> getMfaMethods() { return mfaMethods; }
    public void setMfaMethods(List<String> mfaMethods) { this.mfaMethods = mfaMethods; }
    
    public Instant getLastPasswordChange() { return lastPasswordChange; }
    public void setLastPasswordChange(Instant lastPasswordChange) { this.lastPasswordChange = lastPasswordChange; }
    
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    
    public Instant getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(Instant accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }
    
    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }
    
    public Instant getConsentTimestamp() { return consentTimestamp; }
    public void setConsentTimestamp(Instant consentTimestamp) { this.consentTimestamp = consentTimestamp; }
    
    public String getDataClassification() { return dataClassification; }
    public void setDataClassification(String dataClassification) { this.dataClassification = dataClassification; }
    
    // Session and Tracking Claims
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public Instant getLoginTimestamp() { return loginTimestamp; }
    public void setLoginTimestamp(Instant loginTimestamp) { this.loginTimestamp = loginTimestamp; }
    
    public Instant getLastActivity() { return lastActivity; }
    public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }
    
    // Custom and Extended Claims
    public Map<String, Object> getCustomClaims() { return customClaims; }
    public void setCustomClaims(Map<String, Object> customClaims) { this.customClaims = customClaims; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
