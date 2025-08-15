package com.saasstarterkit.user.service.interfaces;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.security.CurrentUser;
import com.saasstarterkit.user.domain.model.Tenant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Multi-Tenant Authentication and Authorization Provider Interface
 * Extends the base IAM provider with tenant-aware authentication and authorization capabilities.
 * 
 * This interface supports:
 * - Tenant isolation in authentication context
 * - Multi-tenant user and role mapping
 * - Federated identity provider support per tenant
 * - Pluggable authentication mechanisms
 * - Token management with tenant context
 * - Authorization decision enforcement
 * 
 * @author SaaS Starter Kit Team
 */
public interface IMultiTenantAuthProvider extends IIAMProvider {
    
    // =============================================
    // TENANT-AWARE AUTHENTICATION
    // =============================================
    
    /**
     * Authenticate user within a specific tenant context
     */
    TenantAuthResult authenticateUserInTenant(String username, String password, String tenantId);
    
    /**
     * Authenticate user with federated identity provider within tenant context
     */
    TenantAuthResult authenticateWithFederatedIdP(String idpToken, String tenantId, String idpType);
    
    /**
     * Validate token within tenant context
     */
    TenantTokenValidationResult validateTokenInTenant(String token, String tenantId);
    
    /**
     * Get current user context with tenant information
     */
    TenantUserContext getCurrentUserInTenant(String token, String tenantId);
    
    // =============================================
    // TENANT USER MANAGEMENT
    // =============================================
    
    /**
     * Create user in specific tenant with roles
     */
    TenantUserDTO createUserInTenant(TenantUserDTO userDTO, String tenantId);
    
    /**
     * Get user's roles and permissions within a specific tenant
     */
    TenantUserRolesDTO getUserRolesInTenant(String userId, String tenantId);
    
    /**
     * Assign roles to user within a specific tenant
     */
    boolean assignRolesToUserInTenant(String userId, List<String> roles, String tenantId);
    
    /**
     * Remove roles from user within a specific tenant
     */
    boolean removeRolesFromUserInTenant(String userId, List<String> roles, String tenantId);
    
    /**
     * Check if user has specific role within tenant
     */
    boolean hasRoleInTenant(String userId, String role, String tenantId);
    
    /**
     * Check if user has specific permission within tenant
     */
    boolean hasPermissionInTenant(String userId, String permission, String tenantId);
    
    // =============================================
    // TENANT CONFIGURATION
    // =============================================
    
    /**
     * Configure identity provider for a specific tenant
     */
    boolean configureTenantIdP(String tenantId, TenantIdPConfig idpConfig);
    
    /**
     * Get tenant's identity provider configuration
     */
    Optional<TenantIdPConfig> getTenantIdPConfig(String tenantId);
    
    /**
     * Update tenant authentication policies
     */
    boolean updateTenantAuthPolicies(String tenantId, TenantAuthPolicies policies);
    
    /**
     * Get tenant's authentication policies
     */
    Optional<TenantAuthPolicies> getTenantAuthPolicies(String tenantId);
    
    // =============================================
    // TOKEN MANAGEMENT
    // =============================================
    
    /**
     * Generate tenant-aware JWT token
     */
    String generateTenantToken(TenantUserContext userContext, String tenantId);
    
    /**
     * Refresh tenant token
     */
    String refreshTenantToken(String refreshToken, String tenantId);
    
    /**
     * Revoke tenant token
     */
    boolean revokeTenantToken(String token, String tenantId);
    
    /**
     * Introspect token and get tenant context
     */
    TokenIntrospectionResult introspectToken(String token);
    
    // =============================================
    // AUTHORIZATION & POLICY ENFORCEMENT
    // =============================================
    
    /**
     * Check if user can access resource within tenant
     */
    AuthorizationResult authorizeAccess(String userId, String resource, String action, String tenantId);
    
    /**
     * Check if user can access resource with ABAC attributes
     */
    AuthorizationResult authorizeAccessWithAttributes(String userId, String resource, String action, 
                                                   String tenantId, Map<String, Object> attributes);
    
    /**
     * Get user's effective permissions within tenant
     */
    List<String> getUserEffectivePermissions(String userId, String tenantId);
    
    /**
     * Evaluate policy expression within tenant context
     */
    boolean evaluatePolicy(String policyExpression, TenantUserContext userContext, String tenantId);
    
    // =============================================
    // SESSION MANAGEMENT
    // =============================================
    
    /**
     * Create user session within tenant
     */
    TenantSessionDTO createTenantSession(String userId, String tenantId, Map<String, Object> sessionData);
    
    /**
     * Validate user session within tenant
     */
    boolean validateTenantSession(String sessionId, String tenantId);
    
    /**
     * Invalidate user session within tenant
     */
    boolean invalidateTenantSession(String sessionId, String tenantId);
    
    /**
     * Get active sessions for user within tenant
     */
    List<TenantSessionDTO> getUserActiveSessions(String userId, String tenantId);
    
    // =============================================
    // AUDIT & COMPLIANCE
    // =============================================
    
    /**
     * Log authentication event for tenant
     */
    void logAuthEvent(String userId, String tenantId, String eventType, Map<String, Object> eventData);
    
    /**
     * Get authentication audit trail for tenant
     */
    List<AuthAuditEvent> getTenantAuthAuditTrail(String tenantId, String userId, 
                                                String eventType, String startDate, String endDate);
    
    /**
     * Generate compliance report for tenant
     */
    TenantComplianceReport generateTenantComplianceReport(String tenantId, String reportType);
}
