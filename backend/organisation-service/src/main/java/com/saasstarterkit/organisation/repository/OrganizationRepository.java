package com.saasstarterkit.organisation.repository;

import com.saasstarterkit.organisation.domain.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    Optional<Organization> findBySlug(String slug);
    
    Optional<Organization> findByDomain(String domain);
    
    Optional<Organization> findByContactEmail(String email);
    
    List<Organization> findByStatus(com.saasstarterkit.organisation.domain.OrganizationStatus status);
    
    List<Organization> findByTier(com.saasstarterkit.organisation.domain.OrganizationTier tier);
    
    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = :subscriptionStatus")
    List<Organization> findBySubscriptionStatus(@Param("subscriptionStatus") com.saasstarterkit.organisation.domain.SubscriptionStatus status);
    
    @Query("SELECT o FROM Organization o WHERE o.onboardingCompleted = :completed")
    List<Organization> findByOnboardingCompleted(@Param("completed") boolean completed);
    
    @Query("SELECT o FROM Organization o WHERE o.name LIKE %:searchTerm% OR o.slug LIKE %:searchTerm%")
    Page<Organization> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT o FROM Organization o WHERE o.trialEndsAt <= :date")
    List<Organization> findOrganizationsWithExpiredTrial(@Param("date") java.time.LocalDateTime date);
    
    boolean existsBySlug(String slug);
    
    boolean existsByDomain(String domain);
    
    boolean existsByContactEmail(String email);
}
