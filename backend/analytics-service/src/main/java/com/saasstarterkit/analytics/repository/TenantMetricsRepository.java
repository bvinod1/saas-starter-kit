package com.saasstarterkit.analytics.repository;

import com.saasstarterkit.analytics.entity.TenantMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TenantMetricsRepository extends JpaRepository<TenantMetrics, UUID> {
    
    List<TenantMetrics> findByTenantId(String tenantId);
    
    List<TenantMetrics> findByTenantIdAndMetricType(String tenantId, String metricType);
    
    List<TenantMetrics> findByTenantIdAndMetricDateBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT tm FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricType = :metricType AND tm.metricDate >= :startDate")
    List<TenantMetrics> findByTenantIdAndMetricTypeAndDateRange(@Param("tenantId") String tenantId, @Param("metricType") String metricType, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT tm FROM TenantMetrics tm WHERE tm.tenantId = :tenantId ORDER BY tm.metricDate DESC")
    Page<TenantMetrics> findByTenantIdOrderedByDate(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT tm FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricType = :metricType ORDER BY tm.metricDate DESC")
    List<TenantMetrics> findByTenantIdAndMetricTypeOrderedByDate(@Param("tenantId") String tenantId, @Param("metricType") String metricType);
    
    @Query("SELECT tm.metricType, AVG(tm.metricValue) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricValue IS NOT NULL GROUP BY tm.metricType")
    List<Object[]> getAverageMetricsByTypeForTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tm.metricType, SUM(tm.metricCount) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricCount IS NOT NULL GROUP BY tm.metricType")
    List<Object[]> getTotalCountsByTypeForTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tm.metricType, MAX(tm.metricValue) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricValue IS NOT NULL GROUP BY tm.metricType")
    List<Object[]> getMaxMetricsByTypeForTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tm.metricType, MIN(tm.metricValue) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricValue IS NOT NULL GROUP BY tm.metricType")
    List<Object[]> getMinMetricsByTypeForTenant(@Param("tenantId") String tenantId);
    
    @Query("SELECT tm FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricValue >= :minValue")
    List<TenantMetrics> findByTenantIdAndMinValue(@Param("tenantId") String tenantId, @Param("minValue") BigDecimal minValue);
    
    @Query("SELECT tm FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricValue <= :maxValue")
    List<TenantMetrics> findByTenantIdAndMaxValue(@Param("tenantId") String tenantId, @Param("maxValue") BigDecimal maxValue);
    
    @Query("SELECT COUNT(tm) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricDate >= :startDate")
    long countByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(tm) FROM TenantMetrics tm WHERE tm.tenantId = :tenantId AND tm.metricType = :metricType")
    long countByTenantIdAndMetricType(@Param("tenantId") String tenantId, @Param("metricType") String metricType);
    
    boolean existsByTenantIdAndMetricTypeAndMetricDate(String tenantId, String metricType, LocalDateTime metricDate);
    
    long countByTenantId(String tenantId);
}
