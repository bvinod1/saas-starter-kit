package com.saasstarterkit.user.service.interfaces;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.security.CurrentUser;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive IAM Provider interface that abstracts all IAM operations.
 * This interface allows easy swapping of IAM providers (Keycloak, Auth0, Okta, etc.)
 * through configuration changes.
 * 
 * @author SaaS Starter Kit Team
 */
public interface IIAMProvider {
    
    // =============================================
    // USER IDENTITY MANAGEMENT
    // =============================================
    
    /**
     * Create a new user in the IAM system
     */
    IAMUserDTO createUser(IAMUserDTO userDTO);
    
    /**
     * Get user information by ID
     */
    IAMUserInfoDTO getUser(String userId);
    
    /**
     * Get user information by username/email
     */
    IAMUserInfoDTO getUserByUsername(String username);
    
    /**
     * Update user information
     */
    IAMUpdateUserDTO updateUser(IAMUpdateUserDTO userDTO);
    
    /**
     * Delete user from IAM system
     */
    boolean deleteUser(String userId);
    
    /**
     * Check if user exists
     */
    IAMUserExistDTO isUserExists(String username);
    
    /**
     * Enable/disable user
     */
    boolean updateUserState(String userId, boolean enabled);
    
    /**
     * Get all users with filtering and pagination
     */
    List<UserModel> getUsers(String filter, int firstIndex, int maxResult);
    
    // =============================================
    // AUTHENTICATION
    // =============================================
    
    /**
     * Authenticate user and get token
     */
    String authenticateUser(String username, String password);
    
    /**
     * Validate token
     */
    boolean validateToken(String token);
    
    /**
     * Refresh token
     */
    String refreshToken(String refreshToken);
    
    /**
     * Logout user
     */
    boolean logoutUser(String userId);
    
    /**
     * Logout all user sessions
     */
    int logoutAllUserSessions(String userId);
    
    // =============================================
    // ROLE MANAGEMENT
    // =============================================
    
    /**
     * Get all available roles
     */
    List<String> getRoles();
    
    /**
     * Create new role
     */
    boolean createRole(String roleName, String description);
    
    /**
     * Delete role
     */
    boolean deleteRole(String roleName);
    
    /**
     * Get user roles
     */
    List<String> getUserRoles(String userId);
    
    /**
     * Assign roles to user
     */
    boolean assignRoles(String userId, List<String> roles);
    
    /**
     * Remove roles from user
     */
    boolean removeRoles(String userId, List<String> roles);
    
    // =============================================
    // ATTRIBUTE MANAGEMENT
    // =============================================
    
    /**
     * Get user attributes
     */
    Map<String, List<String>> getUserAttributes(String userId);
    
    /**
     * Update user attributes
     */
    boolean updateUserAttributes(String userId, Map<String, List<String>> attributes);
    
    /**
     * Remove specific attribute
     */
    boolean removeUserAttribute(String userId, String attributeKey);
    
    // =============================================
    // PASSWORD MANAGEMENT
    // =============================================
    
    /**
     * Reset user password
     */
    boolean resetPassword(String userId, String newPassword, boolean temporary);
    
    /**
     * Change user password
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);
    
    /**
     * Force password change on next login
     */
    boolean forcePasswordChange(String userId);
    
    // =============================================
    // EMAIL ACTIONS
    // =============================================
    
    /**
     * Trigger email actions (verification, password reset, etc.)
     */
    String triggerEmail(String userId, List<MailAction> emailActions);
    
    // =============================================
    // CURRENT USER CONTEXT
    // =============================================
    
    /**
     * Get current authenticated user
     */
    CurrentUser getCurrentUser();
    
    /**
     * Get current user roles
     */
    List<String> getCurrentUserRoles();
    
    /**
     * Get current user attributes
     */
    Map<String, Object> getCurrentUserAttributes();
    
    // =============================================
    // REALM/TENANT MANAGEMENT
    // =============================================
    
    /**
     * Get current realm/tenant context
     */
    String getCurrentRealm();
    
    /**
     * Switch to different realm/tenant
     */
    boolean switchRealm(String realmName);
    
    // =============================================
    // PROVIDER INFORMATION
    // =============================================
    
    /**
     * Get provider name (e.g., "keycloak", "auth0", "okta")
     */
    String getProviderName();
    
    /**
     * Check if provider is healthy/available
     */
    boolean isHealthy();
    
    // =============================================
    // ENUMERATIONS
    // =============================================
    
    enum MailAction {
        VERIFY_EMAIL,
        UPDATE_PASSWORD,
        RESET_PASSWORD,
        WELCOME_EMAIL
    }
    
    enum RoleAction {
        ASSIGN,
        REMOVE
    }
}
