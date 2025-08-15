package com.saasstarterkit.organizationalmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_metrics", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @NotNull(message = "Metric name is required")
    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @NotNull(message = "Metric value is required")
    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "metric_unit")
    private String metricUnit;

    @Column(name = "metric_category")
    private String metricCategory;

    @NotNull(message = "Recorded at is required")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OrganizationMetric() {}

    public OrganizationMetric(UUID organizationId, String metricName, BigDecimal metricValue, String metricUnit, String metricCategory) {
        this.organizationId = organizationId;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricUnit = metricUnit;
        this.metricCategory = metricCategory;
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }

    public String getMetricUnit() { return metricUnit; }
    public void setMetricUnit(String metricUnit) { this.metricUnit = metricUnit; }

    public String getMetricCategory() { return metricCategory; }
    public void setMetricCategory(String metricCategory) { this.metricCategory = metricCategory; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "OrganizationMetric{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", metricName='" + metricName + '\'' +
                ", metricValue=" + metricValue +
                ", metricUnit='" + metricUnit + '\'' +
                ", metricCategory='" + metricCategory + '\'' +
                ", recordedAt=" + recordedAt +
                '}';
    }
}
