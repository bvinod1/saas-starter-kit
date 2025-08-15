package com.saasstarterkit.user.repository;

import com.saasstarterkit.user.entity.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {
    
    // Find active configuration by tenant and key
    Optional<TenantConfiguration> findByTenantIdAndConfigKeyAndIsActiveTrue(String tenantId, String configKey);
    
    // Find active configuration by tenant, key, and environment
    Optional<TenantConfiguration> findByTenantIdAndConfigKeyAndEnvironmentAndIsActiveTrue(
        String tenantId, String configKey, String environment);
    
    // Find all active configurations for a tenant
    List<TenantConfiguration> findByTenantIdAndIsActiveTrue(String tenantId);
    
    // Find all active configurations for a tenant in specific environment
    List<TenantConfiguration> findByTenantIdAndEnvironmentAndIsActiveTrue(String tenantId, String environment);
    
    // Find configurations by key across all tenants (for global configs)
    List<TenantConfiguration> findByConfigKeyAndIsActiveTrue();
    
    // Find configurations by key and environment across all tenants
    List<TenantConfiguration> findByConfigKeyAndEnvironmentAndIsActiveTrue(String configKey, String environment);
    
    // Find latest version of a configuration
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId " +
           "AND tc.configKey = :configKey AND tc.environment = :environment " +
           "ORDER BY tc.version DESC")
    Optional<TenantConfiguration> findLatestVersion(
        @Param("tenantId") String tenantId, 
        @Param("configKey") String configKey, 
        @Param("environment") String environment);
    
    // Find all versions of a configuration for audit purposes
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId " +
           "AND tc.configKey = :configKey AND tc.environment = :environment " +
           "ORDER BY tc.version DESC")
    List<TenantConfiguration> findAllVersions(
        @Param("tenantId") String tenantId, 
        @Param("configKey") String configKey, 
        @Param("environment") String environment);
    
    // Find configurations by type
    List<TenantConfiguration> findByTenantIdAndConfigTypeAndIsActiveTrue(String tenantId, TenantConfiguration.ConfigType configType);
    
    // Find feature flags for a tenant
    List<TenantConfiguration> findByTenantIdAndConfigTypeAndIsActiveTrue(String tenantId, TenantConfiguration.ConfigType configType);
    
    // Check if configuration exists
    boolean existsByTenantIdAndConfigKeyAndEnvironmentAndIsActiveTrue(String tenantId, String configKey, String environment);
    
    // Count active configurations for a tenant
    long countByTenantIdAndIsActiveTrue(String tenantId);
    
    // Find configurations created/updated by specific user
    List<TenantConfiguration> findByCreatedByOrUpdatedBy(String createdBy, String updatedBy);
    
    // Find configurations modified after a specific date
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.updatedAt >= :since")
    List<TenantConfiguration> findModifiedSince(@Param("since") java.time.LocalDateTime since);
}
