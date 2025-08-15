package com.saasstarterkit.user.service.adapters;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.security.CurrentUser;
import com.saasstarterkit.user.service.interfaces.IIAMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Abstract base class for IAM providers that provides common functionality
 * and default implementations. This makes it easier to implement new IAM providers.
 * 
 * @author SaaS Starter Kit Team
 */
public abstract class AbstractIAMProvider implements IIAMProvider {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    // =============================================
    // DEFAULT IMPLEMENTATIONS
    // =============================================
    
    @Override
    public boolean isHealthy() {
        try {
            // Default health check - try to get current realm
            getCurrentRealm();
            return true;
        } catch (Exception e) {
            logger.warn("Health check failed for provider {}: {}", getProviderName(), e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean switchRealm(String realmName) {
        // Default implementation - most providers don't support dynamic realm switching
        logger.warn("Realm switching not supported by provider: {}", getProviderName());
        return false;
    }
    
    @Override
    public boolean forcePasswordChange(String userId) {
        // Default implementation - set temporary password flag
        return resetPassword(userId, generateTemporaryPassword(), true);
    }
    
    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        // Default implementation - validate old password then reset
        if (validateUserPassword(userId, oldPassword)) {
            return resetPassword(userId, newPassword, false);
        }
        return false;
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    /**
     * Generate a temporary password for forced password changes
     */
    protected String generateTemporaryPassword() {
        return "TempPass" + System.currentTimeMillis();
    }
    
    /**
     * Validate user password (to be implemented by concrete providers)
     */
    protected abstract boolean validateUserPassword(String userId, String password);
    
    /**
     * Log provider operation for debugging
     */
    protected void logOperation(String operation, String userId, boolean success) {
        if (success) {
            logger.debug("Provider {}: {} operation successful for user {}", 
                getProviderName(), operation, userId);
        } else {
            logger.warn("Provider {}: {} operation failed for user {}", 
                getProviderName(), operation, userId);
        }
    }
    
    /**
     * Log provider operation with additional context
     */
    protected void logOperation(String operation, String userId, String context, boolean success) {
        if (success) {
            logger.debug("Provider {}: {} operation successful for user {} - {}", 
                getProviderName(), operation, userId, context);
        } else {
            logger.warn("Provider {}: {} operation failed for user {} - {}", 
                getProviderName(), operation, userId, context);
        }
    }
    
    /**
     * Validate required parameters
     */
    protected void validateRequired(String paramName, Object value) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            throw new IllegalArgumentException("Required parameter '" + paramName + "' cannot be null or empty");
        }
    }
    
    /**
     * Validate user ID format (to be implemented by concrete providers)
     */
    protected abstract boolean isValidUserId(String userId);
    
    /**
     * Validate username format (to be implemented by concrete providers)
     */
    protected abstract boolean isValidUsername(String username);
    
    /**
     * Validate email format
     */
    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Validate password strength
     */
    protected boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
    
    // =============================================
    // COMMON VALIDATION METHODS
    // =============================================
    
    /**
     * Validate user creation request
     */
    protected void validateUserCreation(IAMUserDTO userDTO) {
        validateRequired("email", userDTO.getEmail());
        validateRequired("firstName", userDTO.getFirstName());
        validateRequired("lastName", userDTO.getLastName());
        validateRequired("password", userDTO.getPassword());
        
        if (!isValidEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + userDTO.getEmail());
        }
        
        if (!isValidPassword(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }
    
    /**
     * Validate user update request
     */
    protected void validateUserUpdate(IAMUpdateUserDTO userDTO) {
        validateRequired("userId", userDTO.getUserid());
        
        if (!isValidUserId(userDTO.getUserid())) {
            throw new IllegalArgumentException("Invalid user ID format: " + userDTO.getUserid());
        }
        
        if (userDTO.getEmail() != null && !isValidEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + userDTO.getEmail());
        }
    }
    
    /**
     * Validate role operations
     */
    protected void validateRoleOperation(String userId, List<String> roles) {
        validateRequired("userId", userId);
        validateRequired("roles", roles);
        
        if (!isValidUserId(userId)) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId);
        }
        
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Roles list cannot be empty");
        }
        
        for (String role : roles) {
            if (role == null || role.trim().isEmpty()) {
                throw new IllegalArgumentException("Role cannot be null or empty");
            }
        }
    }
    
    // =============================================
    // ERROR HANDLING
    // =============================================
    
    /**
     * Handle provider-specific exceptions and convert to common exceptions
     */
    protected RuntimeException handleProviderException(String operation, Exception e) {
        logger.error("Provider {}: {} operation failed", getProviderName(), operation, e);
        
        // Convert provider-specific exceptions to common exceptions
        if (e instanceof IllegalArgumentException) {
            return (IllegalArgumentException) e;
        }
        
        return new RuntimeException("IAM operation '" + operation + "' failed: " + e.getMessage(), e);
    }
    
    /**
     * Create user-friendly error message
     */
    protected String createErrorMessage(String operation, String details) {
        return String.format("IAM operation '%s' failed: %s", operation, details);
    }
}
