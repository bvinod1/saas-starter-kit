package com.saasstarterkit.billing.repository;

import com.saasstarterkit.billing.domain.model.CheckoutSession;
import com.saasstarterkit.billing.domain.model.CheckoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, UUID> {
    
    Optional<CheckoutSession> findByExternalSessionId(String externalSessionId);
    
    List<CheckoutSession> findByTenantId(String tenantId);
    
    List<CheckoutSession> findByStatus(CheckoutStatus status);
    
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.tenantId = :tenantId AND cs.status = 'PENDING'")
    List<CheckoutSession> findPendingSessionsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.createdAt <= :expiryDate AND cs.status = 'PENDING'")
    List<CheckoutSession> findExpiredPendingSessions(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.tenantId = :tenantId ORDER BY cs.createdAt DESC")
    List<CheckoutSession> findByTenantIdOrdered(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(cs) FROM CheckoutSession cs WHERE cs.tenantId = :tenantId AND cs.status = 'COMPLETED'")
    long countCompletedSessionsByTenant(@Param("tenantId") String tenantId);
    
    boolean existsByExternalSessionId(String externalSessionId);
    
    boolean existsByTenantIdAndStatus(String tenantId, CheckoutStatus status);
}
