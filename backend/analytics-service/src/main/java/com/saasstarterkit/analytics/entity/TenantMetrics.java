package com.saasstarterkit.analytics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_metrics", schema = "analytics", indexes = {
    @Index(name = "idx_tenant_metrics_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_tenant_metrics_date", columnList = "metric_date"),
    @Index(name = "idx_tenant_metrics_type", columnList = "metric_type")
})
@EntityListeners(AuditingEntityListener.class)
public class TenantMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @NotBlank
    @Column(name = "metric_type", nullable = false)
    private String metricType;
    
    @NotNull
    @Column(name = "metric_date", nullable = false)
    private LocalDateTime metricDate;
    
    @Column(name = "metric_value", precision = 19, scale = 4)
    private BigDecimal metricValue;
    
    @Column(name = "metric_count")
    private Long metricCount;
    
    @Column(name = "metric_string")
    private String metricString;
    
    @Column(name = "metric_json", columnDefinition = "jsonb")
    private String metricJson;
    
    @Column(name = "dimension_1")
    private String dimension1;
    
    @Column(name = "dimension_2")
    private String dimension2;
    
    @Column(name = "dimension_3")
    private String dimension3;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TenantMetrics() {}
    
    public TenantMetrics(String tenantId, String metricType, LocalDateTime metricDate) {
        this.tenantId = tenantId;
        this.metricType = metricType;
        this.metricDate = metricDate;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    
    public LocalDateTime getMetricDate() { return metricDate; }
    public void setMetricDate(LocalDateTime metricDate) { this.metricDate = metricDate; }
    
    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
    
    public Long getMetricCount() { return metricCount; }
    public void setMetricCount(Long metricCount) { this.metricCount = metricCount; }
    
    public String getMetricString() { return metricString; }
    public void setMetricString(String metricString) { this.metricString = metricString; }
    
    public String getMetricJson() { return metricJson; }
    public void setMetricJson(String metricJson) { this.metricJson = metricJson; }
    
    public String getDimension1() { return dimension1; }
    public void setDimension1(String dimension1) { this.dimension1 = dimension1; }
    
    public String getDimension2() { return dimension2; }
    public void setDimension2(String dimension2) { this.dimension2 = dimension2; }
    
    public String getDimension3() { return dimension3; }
    public void setDimension3(String dimension3) { this.dimension3 = dimension3; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
