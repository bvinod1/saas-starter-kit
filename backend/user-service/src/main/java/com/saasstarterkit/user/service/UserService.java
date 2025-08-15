package com.saasstarterkit.user.service;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.entity.User;
import com.saasstarterkit.user.entity.Role;
import com.saasstarterkit.user.repository.UserRepository;
import com.saasstarterkit.user.repository.RoleRepository;
import com.saasstarterkit.user.service.interfaces.IIAMProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

/**
 * UserService that follows IAM best practices by delegating all identity-related
 * operations to the IAM provider and only managing application-specific user data.
 * 
 * @author SaaS Starter Kit Team
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private IIAMProvider iamProvider;
    
    // =============================================
    // APPLICATION-SPECIFIC USER DATA MANAGEMENT
    // =============================================
    
    /**
     * Create application-specific user profile data.
     * This method only handles business logic and application data,
     * not authentication or identity management.
     */
    public User createUserProfile(String keycloakUserId, String email, String firstName, String lastName) {
        // Check if user profile already exists
        if (userRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new RuntimeException("User profile already exists for IAM user: " + keycloakUserId);
        }
        
        // Create application-specific user profile
        User user = new User();
        user.setKeycloakUserId(keycloakUserId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAvatarUrl(null);
        user.setBio(null);
        user.setLanguage("en");
        user.setTimezone("UTC");
        user.setPreferences("{}");
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Get user profile by Keycloak user ID
     */
    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        return userRepository.findByKeycloakUserId(keycloakUserId);
    }
    
    /**
     * Get user profile by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Update user profile (application-specific data only)
     */
    public User updateUserProfile(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Delete user profile (application-specific data only)
     */
    public void deleteUserProfile(Long userId) {
        userRepository.deleteById(userId);
    }
    
    /**
     * Get all user profiles for a tenant
     */
    public List<User> getUsersByTenantId(String tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
    
    /**
     * Get all user profiles for an organization
     */
    public List<User> getUsersByOrganizationId(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }
    
    // =============================================
    // IAM-INTEGRATED USER OPERATIONS
    // =============================================
    
    /**
     * Create a complete user (IAM + application profile)
     * This method demonstrates the proper separation of concerns
     */
    public IAMUserDTO createCompleteUser(IAMUserDTO iamUserDTO) {
        // 1. Create user in IAM system (Keycloak/Auth0/Okta)
        IAMUserDTO createdIAMUser = iamProvider.createUser(iamUserDTO);
        
        // 2. Create application-specific user profile
        User userProfile = createUserProfile(
            createdIAMUser.getUserid(),
            createdIAMUser.getEmail(),
            createdIAMUser.getFirstName(),
            createdIAMUser.getLastName()
        );
        
        // 3. Set the application user ID in the IAM user DTO for reference
        createdIAMUser.setApplicationUserId(userProfile.getId());
        
        return createdIAMUser;
    }
    
    /**
     * Get complete user information (IAM + application profile)
     */
    public UserProfileDTO getCompleteUser(String iamUserId) {
        // 1. Get IAM user information
        IAMUserInfoDTO iamUser = iamProvider.getUser(iamUserId);
        
        // 2. Get application-specific user profile
        Optional<User> userProfile = findByKeycloakUserId(iamUserId);
        
        // 3. Combine both into a comprehensive user profile
        UserProfileDTO completeUser = new UserProfileDTO();
        completeUser.setIamUser(iamUser);
        completeUser.setApplicationProfile(userProfile.orElse(null));
        
        return completeUser;
    }
    
    /**
     * Update complete user information
     */
    public UserProfileDTO updateCompleteUser(String iamUserId, IAMUpdateUserDTO iamUpdate, User profileUpdate) {
        // 1. Update IAM user information
        IAMUpdateUserDTO updatedIAMUser = iamProvider.updateUser(iamUpdate);
        
        // 2. Update application-specific user profile
        User updatedProfile = null;
        if (profileUpdate != null) {
            Optional<User> existingProfile = findByKeycloakUserId(iamUserId);
            if (existingProfile.isPresent()) {
                User profile = existingProfile.get();
                // Update only non-null fields
                if (profileUpdate.getAvatarUrl() != null) profile.setAvatarUrl(profileUpdate.getAvatarUrl());
                if (profileUpdate.getBio() != null) profile.setBio(profileUpdate.getBio());
                if (profileUpdate.getLanguage() != null) profile.setLanguage(profileUpdate.getLanguage());
                if (profileUpdate.getTimezone() != null) profile.setTimezone(profileUpdate.getTimezone());
                if (profileUpdate.getPreferences() != null) profile.setPreferences(profileUpdate.getPreferences());
                
                updatedProfile = updateUserProfile(profile);
            }
        }
        
        // 3. Return updated complete user
        UserProfileDTO completeUser = new UserProfileDTO();
        completeUser.setIamUser(iamProvider.getUser(iamUserId));
        completeUser.setApplicationProfile(updatedProfile);
        
        return completeUser;
    }
    
    /**
     * Delete complete user (IAM + application profile)
     */
    public boolean deleteCompleteUser(String iamUserId) {
        try {
            // 1. Delete application-specific user profile
            Optional<User> userProfile = findByKeycloakUserId(iamUserId);
            if (userProfile.isPresent()) {
                deleteUserProfile(userProfile.get().getId());
            }
            
            // 2. Delete user from IAM system
            return iamProvider.deleteUser(iamUserId);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete complete user: " + e.getMessage(), e);
        }
    }
    
    // =============================================
    // ROLE MANAGEMENT (DELEGATED TO IAM)
    // =============================================
    
    /**
     * Get user roles from IAM system
     */
    public List<String> getUserRoles(String iamUserId) {
        return iamProvider.getUserRoles(iamUserId);
    }
    
    /**
     * Assign roles to user via IAM system
     */
    public boolean assignUserRoles(String iamUserId, List<String> roles) {
        return iamProvider.assignRoles(iamUserId, roles);
    }
    
    /**
     * Remove roles from user via IAM system
     */
    public boolean removeUserRoles(String iamUserId, List<String> roles) {
        return iamProvider.removeRoles(iamUserId, roles);
    }
    
    /**
     * Get all available roles from IAM system
     */
    public List<String> getAllRoles() {
        return iamProvider.getRoles();
    }
    
    // =============================================
    // AUTHENTICATION (DELEGATED TO IAM)
    // =============================================
    
    /**
     * Authenticate user via IAM system
     */
    public String authenticateUser(String username, String password) {
        return iamProvider.authenticateUser(username, password);
    }
    
    /**
     * Validate token via IAM system
     */
    public boolean validateToken(String token) {
        return iamProvider.validateToken(token);
    }
    
    /**
     * Reset password via IAM system
     */
    public boolean resetPassword(String iamUserId, String newPassword, boolean temporary) {
        return iamProvider.resetPassword(iamUserId, newPassword, temporary);
    }
    
    // =============================================
    // USER ATTRIBUTES (DELEGATED TO IAM)
    // =============================================
    
    /**
     * Get user attributes from IAM system
     */
    public Map<String, List<String>> getUserAttributes(String iamUserId) {
        return iamProvider.getUserAttributes(iamUserId);
    }
    
    /**
     * Update user attributes via IAM system
     */
    public boolean updateUserAttributes(String iamUserId, Map<String, List<String>> attributes) {
        return iamProvider.updateUserAttributes(iamUserId, attributes);
    }
    
    // =============================================
    // EMAIL ACTIONS (DELEGATED TO IAM)
    // =============================================
    
    /**
     * Trigger email actions via IAM system
     */
    public String triggerEmail(String iamUserId, List<IIAMProvider.MailAction> emailActions) {
        return iamProvider.triggerEmail(iamUserId, emailActions);
    }
    
    // =============================================
    // CURRENT USER CONTEXT (DELEGATED TO IAM)
    // =============================================
    
    /**
     * Get current authenticated user from IAM system
     */
    public IAMUserInfoDTO getCurrentUser() {
        return iamProvider.getUser(iamProvider.getCurrentUser().getUserId());
    }
    
    /**
     * Get current user roles from IAM system
     */
    public List<String> getCurrentUserRoles() {
        return iamProvider.getCurrentUserRoles();
    }
    
    // =============================================
    // LEGACY METHODS (DEPRECATED - FOR MIGRATION)
    // =============================================
    
    /**
     * @deprecated Use createCompleteUser() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public User createUser(User user) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use createCompleteUser() instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use getCompleteUser() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public Optional<User> findByUsername(String username) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use getCompleteUser() instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use getCompleteUser() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use getCompleteUser() instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use updateCompleteUser() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public User updateUser(User user) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use updateCompleteUser() instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use getAllRoles() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public List<User> getAllUsers() {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use resetPassword() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public String generatePasswordResetToken(String email) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use validateToken() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public boolean validatePasswordResetToken(String token) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use resetPassword() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public void resetPassword(String token, String newPassword) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use triggerEmail() instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public String generateEmailVerificationToken(String email) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    
    /**
     * @deprecated Use IAM provider methods instead
     * This method is kept for backward compatibility during migration
     */
    @Deprecated
    public boolean verifyEmail(String token) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use IAM provider methods instead to follow IAM best practices."
        );
    }
    

} 