package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    Optional<Tenant> findBySlug(String slug);
    
    Optional<Tenant> findByKeycloakRealmId(String keycloakRealmId);
    
    Optional<Tenant> findByDomain(String domain);
    
    @Query("SELECT t FROM Tenant t WHERE t.slug = :slug AND t.status = 'active'")
    Optional<Tenant> findActiveTenantBySlug(@Param("slug") String slug);
    
    boolean existsBySlug(String slug);
    
    boolean existsByDomain(String domain);
}
