package com.saasstarterkit.tenant.repository;

import com.saasstarterkit.tenant.domain.model.ConfigurationHistory;
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
public interface ConfigurationHistoryRepository extends JpaRepository<ConfigurationHistory, UUID> {
    
    List<ConfigurationHistory> findByTenantId(String tenantId);
    
    List<ConfigurationHistory> findByConfigurationId(UUID configurationId);
    
    List<ConfigurationHistory> findByTenantIdAndConfigurationKey(String tenantId, String configurationKey);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.tenantId = :tenantId ORDER BY ch.changedAt DESC")
    Page<ConfigurationHistory> findByTenantIdOrderedByDate(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.configurationId = :configurationId ORDER BY ch.changedAt DESC")
    List<ConfigurationHistory> findByConfigurationIdOrderedByDate(@Param("configurationId") UUID configurationId);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.tenantId = :tenantId AND ch.changedAt >= :startDate")
    List<ConfigurationHistory> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.tenantId = :tenantId AND ch.changedBy = :changedBy")
    List<ConfigurationHistory> findByTenantIdAndChangedBy(@Param("tenantId") String tenantId, @Param("changedBy") String changedBy);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.tenantId = :tenantId AND ch.changeType = :changeType")
    List<ConfigurationHistory> findByTenantIdAndChangeType(@Param("tenantId") String tenantId, @Param("changeType") String changeType);
    
    long countByTenantId(String tenantId);
    
    long countByConfigurationId(UUID configurationId);
}
