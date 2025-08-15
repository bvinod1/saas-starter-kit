package com.saasstarterkit.tenant.repository;

import com.saasstarterkit.tenant.domain.model.TenantConfiguration;
import com.saasstarterkit.tenant.domain.model.enums.ConfigurationType;
import com.saasstarterkit.tenant.domain.model.enums.Environment;
import com.saasstarterkit.tenant.domain.model.enums.ServiceTier;
import com.saasstarterkit.tenant.domain.model.enums.ConfigurationScope;
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
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, UUID> {
    
    List<TenantConfiguration> findByTenantId(String tenantId);
    
    Optional<TenantConfiguration> findByTenantIdAndConfigurationKey(String tenantId, String configurationKey);
    
    List<TenantConfiguration> findByTenantIdAndEnvironment(String tenantId, Environment environment);
    
    List<TenantConfiguration> findByTenantIdAndServiceTier(String tenantId, ServiceTier serviceTier);
    
    List<TenantConfiguration> findByTenantIdAndConfigurationType(String tenantId, ConfigurationType configurationType);
    
    List<TenantConfiguration> findByTenantIdAndScope(String tenantId, ConfigurationScope scope);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.active = true")
    List<TenantConfiguration> findActiveConfigurationsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.featureFlag = true")
    List<TenantConfiguration> findFeatureFlagsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.sensitive = true")
    List<TenantConfiguration> findSensitiveConfigurationsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.encrypted = true")
    List<TenantConfiguration> findEncryptedConfigurationsByTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId ORDER BY tc.version DESC")
    Page<TenantConfiguration> findByTenantIdOrderedByVersion(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.configurationKey LIKE %:searchTerm%")
    List<TenantConfiguration> findByTenantIdAndKeyContaining(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
    
    boolean existsByTenantIdAndConfigurationKey(String tenantId, String configurationKey);
    
    long countByTenantId(String tenantId);
}
