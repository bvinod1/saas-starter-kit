package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {
    
    Optional<TenantUser> findByEmail(String email);
    
    Optional<TenantUser> findByKeycloakUserId(String keycloakUserId);
    
    @Query("SELECT COUNT(u) FROM TenantUser u WHERE u.status = ?1")
    long countByStatus(String status);
    
    @Query("SELECT u FROM TenantUser u WHERE u.keycloakUserId = ?1 AND u.status = 'active'")
    Optional<TenantUser> findActiveUserByKeycloakId(String keycloakUserId);
    
    boolean existsByKeycloakUserId(String keycloakUserId);
}
