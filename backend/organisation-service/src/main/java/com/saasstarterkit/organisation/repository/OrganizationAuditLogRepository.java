package com.saasstarterkit.organisation.repository;

import com.saasstarterkit.organisation.domain.OrganizationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationAuditLogRepository extends JpaRepository<OrganizationAuditLog, UUID> {
    
    List<OrganizationAuditLog> findByOrganizationId(UUID organizationId);
    
    Page<OrganizationAuditLog> findByOrganizationId(UUID organizationId, Pageable pageable);
    
    List<OrganizationAuditLog> findByOrganizationIdAndAction(UUID organizationId, com.saasstarterkit.organisation.domain.AuditAction action);
    
    List<OrganizationAuditLog> findByOrganizationIdAndTimestampBetween(
        UUID organizationId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    List<OrganizationAuditLog> findByUserId(UUID userId);
    
    List<OrganizationAuditLog> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    
    @Query("SELECT al FROM OrganizationAuditLog al WHERE al.organization.id = :organizationId AND al.timestamp >= :since")
    List<OrganizationAuditLog> findRecentAuditLogs(@Param("organizationId") UUID organizationId, @Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM OrganizationAuditLog al WHERE al.organization.id = :organizationId ORDER BY al.timestamp DESC")
    Page<OrganizationAuditLog> findAuditLogsByOrganizationOrderedByTimestamp(
        @Param("organizationId") UUID organizationId, 
        Pageable pageable
    );
    
    @Query("SELECT COUNT(al) FROM OrganizationAuditLog al WHERE al.organization.id = :organizationId AND al.action = :action")
    long countAuditLogsByAction(@Param("organizationId") UUID organizationId, @Param("action") com.saasstarterkit.organisation.domain.AuditAction action);
}
