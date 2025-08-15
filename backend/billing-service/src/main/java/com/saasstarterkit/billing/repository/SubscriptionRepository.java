package com.saasstarterkit.billing.repository;

import com.saasstarterkit.billing.domain.model.Subscription;
import com.saasstarterkit.billing.domain.model.SubscriptionStatus;
import com.saasstarterkit.billing.domain.model.BillingCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByExternalSubscriptionId(String externalSubscriptionId);
    
    List<Subscription> findByTenantId(String tenantId);
    
    Optional<Subscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    List<Subscription> findByBillingCycle(BillingCycle billingCycle);
    
    @Query("SELECT s FROM Subscription s WHERE s.currentPeriodEnd <= :date AND s.status = 'ACTIVE'")
    List<Subscription> findExpiringSubscriptions(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.trialEnd <= :date AND s.status = 'TRIAL'")
    List<Subscription> findExpiringTrials(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    Page<Subscription> findByTenantIdOrdered(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.tenantId = :tenantId AND s.status = 'ACTIVE'")
    long countActiveSubscriptionsByTenant(@Param("tenantId") String tenantId);
    
    boolean existsByExternalSubscriptionId(String externalSubscriptionId);
    
    boolean existsByTenantIdAndStatus(String tenantId, SubscriptionStatus status);
}
