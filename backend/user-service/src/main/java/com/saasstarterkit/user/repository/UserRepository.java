package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository that follows IAM best practices by only querying application-specific user data.
 * All identity-related queries should be delegated to the IAM provider.
 * 
 * @author SaaS Starter Kit Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user profile by Keycloak user ID (IAM system reference)
     */
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Check if user profile exists by Keycloak user ID
     */
    boolean existsByKeycloakUserId(String keycloakUserId);
    
    /**
     * Find user profile by email address
     * Note: Email is duplicated from IAM for convenience and indexing
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user profile exists by email address
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all user profiles for a specific tenant
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    List<User> findByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find all user profiles for a specific organization
     */
    @Query("SELECT u FROM User u WHERE u.organizationId = :organizationId")
    List<User> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    /**
     * Find all active user profiles
     */
    @Query("SELECT u FROM User u WHERE u.status = 'active'")
    List<User> findActiveUsers();
    
    /**
     * Find all inactive user profiles
     */
    @Query("SELECT u FROM User u WHERE u.status != 'active'")
    List<User> findInactiveUsers();
    
    /**
     * Find user profiles by status
     */
    List<User> findByStatus(String status);
    
    /**
     * Find user profiles by language preference
     */
    List<User> findByLanguage(String language);
    
    /**
     * Find user profiles by timezone
     */
    List<User> findByTimezone(String timezone);
    
    // Note: All deprecated methods have been removed as they reference fields
    // that no longer exist in the User entity after the IAM refactoring
} 