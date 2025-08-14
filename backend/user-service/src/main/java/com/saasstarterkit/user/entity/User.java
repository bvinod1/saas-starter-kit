package com.saasstarterkit.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * User entity that follows IAM best practices by only storing application-specific user data.
 * All identity, authentication, and authorization data is managed by the IAM system.
 * 
 * This entity stores:
 * - Application-specific user preferences and settings
 * - Business logic related user data
 * - User profile information not related to authentication
 * 
 * This entity does NOT store:
 * - Passwords or authentication credentials
 * - User roles or permissions
 * - Authentication tokens or sessions
 * - MFA settings or security configurations
 * 
 * @author SaaS Starter Kit Team
 */
@Entity
@Table(name = "users", schema = "auth")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user ID in the IAM system (Keycloak, Auth0, Okta, etc.)
     * This is the foreign key that links application data to IAM identity data.
     */
    @NotBlank
    @Column(name = "keycloak_user_id", unique = true, nullable = false)
    private String keycloakUserId;

    /**
     * User's email address (duplicated from IAM for convenience and indexing)
     * The source of truth for email is the IAM system.
     */
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(unique = true)
    private String email;

    /**
     * User's first name (duplicated from IAM for convenience and indexing)
     * The source of truth for first name is the IAM system.
     */
    @NotBlank
    @Size(max = 50)
    private String firstName;

    /**
     * User's last name (duplicated from IAM for convenience and indexing)
     * The source of truth for last name is the IAM system.
     */
    @NotBlank
    @Size(max = 50)
    private String lastName;

    /**
     * URL to user's avatar/profile picture
     * This is application-specific data not managed by IAM.
     */
    private String avatarUrl;

    /**
     * User's biographical information
     * This is application-specific data not managed by IAM.
     */
    @Size(max = 500)
    private String bio;

    /**
     * User's preferred language
     * This is application-specific data not managed by IAM.
     */
    @Size(max = 10)
    private String language;

    /**
     * User's preferred timezone
     * This is application-specific data not managed by IAM.
     */
    @Size(max = 50)
    private String timezone;

    /**
     * User's preferences stored as JSON
     * This is application-specific data not managed by IAM.
     */
    @Column(columnDefinition = "TEXT")
    private String preferences;
    
    /**
     * User's notification preferences
     * This is application-specific data not managed by IAM.
     */
    @Column(name = "notification_preferences", columnDefinition = "JSONB")
    private String notificationPreferences;

    /**
     * User's status in the application
     * This is application-specific data not managed by IAM.
     */
    @Size(max = 50)
    private String status;

    /**
     * Tenant ID for multi-tenancy support
     * This is application-specific data not managed by IAM.
     */
    @Column(name = "tenant_id")
    private String tenantId;

    /**
     * Organization ID for organizational structure
     * This is application-specific data not managed by IAM.
     */
    @Column(name = "organization_id")
    private Long organizationId;

    /**
     * Timestamp when the user profile was created
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user profile was last updated
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // =============================================
    // CONSTRUCTORS
    // =============================================

    /**
     * Default constructor required by JPA
     */
    public User() {}

    /**
     * Constructor with required fields
     */
    public User(String keycloakUserId, String email, String firstName, String lastName) {
        this.keycloakUserId = keycloakUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = "en";
        this.timezone = "UTC";
        this.preferences = "{}";
        this.notificationPreferences = "{\"email_enabled\":true,\"sms_enabled\":true,\"push_enabled\":true,\"in_app_enabled\":true,\"preferred_language\":\"en\",\"timezone\":\"UTC\"}";
        this.status = "active";
    }

    // =============================================
    // GETTERS
    // =============================================

    public Long getId() { return id; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBio() { return bio; }
    public String getLanguage() { return language; }
    public String getTimezone() { return timezone; }
    public String getPreferences() { return preferences; }
    public String getNotificationPreferences() { return notificationPreferences; }
    public String getStatus() { return status; }
    public String getTenantId() { return tenantId; }
    public Long getOrganizationId() { return organizationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // =============================================
    // SETTERS
    // =============================================

    public void setId(Long id) { this.id = id; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setLanguage(String language) { this.language = language; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    public void setNotificationPreferences(String notificationPreferences) { this.notificationPreferences = notificationPreferences; }
    public void setStatus(String status) { this.status = status; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // =============================================
    // HELPER METHODS
    // =============================================

    /**
     * Get the user's full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return null;
    }

    /**
     * Check if the user profile is active
     */
    public boolean isActive() {
        return "active".equals(status);
    }

    /**
     * Check if the user profile is inactive
     */
    public boolean isInactive() {
        return !isActive();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", keycloakUserId='" + keycloakUserId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status='" + status + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", organizationId=" + organizationId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        return keycloakUserId != null ? keycloakUserId.equals(user.keycloakUserId) : user.keycloakUserId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (keycloakUserId != null ? keycloakUserId.hashCode() : 0);
        return result;
    }
} 