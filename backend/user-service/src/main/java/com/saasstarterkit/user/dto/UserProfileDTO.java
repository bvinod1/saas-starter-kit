package com.saasstarterkit.user.dto;

import com.saasstarterkit.user.entity.User;

/**
 * DTO that combines IAM user information with application-specific user profile data.
 * This demonstrates the proper separation of concerns:
 * - IAM data: identity, authentication, roles, permissions
 * - Application data: business logic, preferences, custom attributes
 * 
 * @author SaaS Starter Kit Team
 */
public class UserProfileDTO {
    
    private IAMUserInfoDTO iamUser;
    private User applicationProfile;
    
    // Default constructor
    public UserProfileDTO() {}
    
    // Constructor with both IAM and application data
    public UserProfileDTO(IAMUserInfoDTO iamUser, User applicationProfile) {
        this.iamUser = iamUser;
        this.applicationProfile = applicationProfile;
    }
    
    // Getters
    public IAMUserInfoDTO getIamUser() {
        return iamUser;
    }
    
    public User getApplicationProfile() {
        return applicationProfile;
    }
    
    // Setters
    public void setIamUser(IAMUserInfoDTO iamUser) {
        this.iamUser = iamUser;
    }
    
    public void setApplicationProfile(User applicationProfile) {
        this.applicationProfile = applicationProfile;
    }
    
    // Helper methods
    
    /**
     * Get the user ID from IAM system
     */
    public String getIamUserId() {
        return iamUser != null ? iamUser.getUserid() : null;
    }
    
    /**
     * Get the application user ID
     */
    public Long getApplicationUserId() {
        return applicationProfile != null ? applicationProfile.getId() : null;
    }
    
    /**
     * Get the user's email (from IAM system)
     */
    public String getEmail() {
        return iamUser != null ? iamUser.getEmail() : null;
    }
    
    /**
     * Get the user's username (from IAM system)
     */
    public String getUsername() {
        return iamUser != null ? iamUser.getUsername() : null;
    }
    
    /**
     * Get the user's first name (from IAM system)
     */
    public String getFirstName() {
        return iamUser != null ? iamUser.getFirstName() : null;
    }
    
    /**
     * Get the user's last name (from IAM system)
     */
    public String getLastName() {
        return iamUser != null ? iamUser.getLastName() : null;
    }
    
    /**
     * Get the user's full name
     */
    public String getFullName() {
        if (iamUser != null) {
            String firstName = iamUser.getFirstName();
            String lastName = iamUser.getLastName();
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (firstName != null) {
                return firstName;
            } else if (lastName != null) {
                return lastName;
            }
        }
        return null;
    }
    
    /**
     * Get the user's roles (from IAM system)
     */
    public java.util.List<String> getRoles() {
        return iamUser != null ? iamUser.getRoles() : null;
    }
    
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String roleName) {
        if (iamUser != null && iamUser.getRoles() != null) {
            return iamUser.getRoles().contains(roleName);
        }
        return false;
    }
    
    /**
     * Check if user is enabled (from IAM system)
     */
    public boolean isEnabled() {
        return iamUser != null && iamUser.getEnabled();
    }
    
    /**
     * Get the user's avatar URL (from application profile)
     */
    public String getAvatarUrl() {
        return applicationProfile != null ? applicationProfile.getAvatarUrl() : null;
    }
    
    /**
     * Get the user's bio (from application profile)
     */
    public String getBio() {
        return applicationProfile != null ? applicationProfile.getBio() : null;
    }
    
    /**
     * Get the user's language preference (from application profile)
     */
    public String getLanguage() {
        return applicationProfile != null ? applicationProfile.getLanguage() : null;
    }
    
    /**
     * Get the user's timezone (from application profile)
     */
    public String getTimezone() {
        return applicationProfile != null ? applicationProfile.getTimezone() : null;
    }
    
    /**
     * Get the user's preferences (from application profile)
     */
    public String getPreferences() {
        return applicationProfile != null ? applicationProfile.getPreferences() : null;
    }
    
    /**
     * Get the user's status (from application profile)
     */
    public String getStatus() {
        return applicationProfile != null ? applicationProfile.getStatus() : null;
    }
    
    /**
     * Check if the user profile is complete (has both IAM and application data)
     */
    public boolean isComplete() {
        return iamUser != null && applicationProfile != null;
    }
    
    /**
     * Check if the user profile has only IAM data
     */
    public boolean hasOnlyIAMData() {
        return iamUser != null && applicationProfile == null;
    }
    
    /**
     * Check if the user profile has only application data
     */
    public boolean hasOnlyApplicationData() {
        return iamUser == null && applicationProfile != null;
    }
    
    @Override
    public String toString() {
        return "UserProfileDTO{" +
                "iamUserId=" + getIamUserId() +
                ", applicationUserId=" + getApplicationUserId() +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", enabled=" + isEnabled() +
                ", roles=" + getRoles() +
                ", complete=" + isComplete() +
                '}';
    }
}
