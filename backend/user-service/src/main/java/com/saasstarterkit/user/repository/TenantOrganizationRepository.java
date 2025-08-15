package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.TenantOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantOrganizationRepository extends JpaRepository<TenantOrganization, Long> {
    
    Optional<TenantOrganization> findBySlug(String slug);
    
    @Query("SELECT o FROM TenantOrganization o WHERE o.slug = :slug")
    Optional<TenantOrganization> findBySlugIgnoreCase(@Param("slug") String slug);
    
    List<TenantOrganization> findAllByOrderByNameAsc();
    
    boolean existsBySlug(String slug);
}
