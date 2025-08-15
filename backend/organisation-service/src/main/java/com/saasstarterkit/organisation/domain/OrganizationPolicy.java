package com.saasstarterkit.organizationalmanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_policies", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @NotNull(message = "Policy name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull(message = "Policy type is required")
    @Column(name = "policy_type", nullable = false)
    private String policyType;

    @NotNull(message = "Policy data is required")
    @Column(name = "policy_data", columnDefinition = "jsonb", nullable = false)
    private String policyData;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @NotNull(message = "Created by is required")
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Constructors
    public OrganizationPolicy() {}

    public OrganizationPolicy(UUID organizationId, String name, String policyType, String policyData) {
        this.organizationId = organizationId;
        this.name = name;
        this.policyType = policyType;
        this.policyData = policyData;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public String getPolicyData() { return policyData; }
    public void setPolicyData(String policyData) { this.policyData = policyData; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // Business Methods
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public String toString() {
        return "OrganizationPolicy{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", name='" + name + '\'' +
                ", policyType='" + policyType + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
