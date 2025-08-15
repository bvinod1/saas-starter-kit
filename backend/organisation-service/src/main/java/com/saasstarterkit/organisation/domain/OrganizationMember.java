package com.saasstarterkit.organisation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organization_members", schema = "organizational_management")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Organization ID is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "permissions", columnDefinition = "jsonb")
    private String permissions;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Column(name = "access_level")
    private String accessLevel;

    @Column(name = "department")
    private String department;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "notes")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MemberAuditLog> auditLogs = new HashSet<>();

    // Constructors
    public OrganizationMember() {}

    public OrganizationMember(UUID userId, Organization organization, MemberRole role) {
        this.userId = userId;
        this.organization = organization;
        this.role = role;
        this.invitedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public UUID getInvitedBy() { return invitedBy; }
    public void setInvitedBy(UUID invitedBy) { this.invitedBy = invitedBy; }

    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Set<MemberAuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(Set<MemberAuditLog> auditLogs) { this.auditLogs = auditLogs; }

    // Business Methods
    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(this.status);
    }

    public boolean isOwner() {
        return MemberRole.OWNER.equals(this.role);
    }

    public boolean isAdmin() {
        return MemberRole.ADMIN.equals(this.role) || MemberRole.OWNER.equals(this.role);
    }

    public boolean isMember() {
        return MemberRole.MEMBER.equals(this.role);
    }

    public boolean isInvited() {
        return this.acceptedAt == null && this.invitedAt != null;
    }

    public boolean isAccepted() {
        return this.acceptedAt != null;
    }

    public void acceptInvitation() {
        this.status = MemberStatus.ACTIVE;
        this.acceptedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }

    public void updateLastActivity() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public boolean hasPermission(String permission) {
        // Implementation would check the permissions JSON field
        // For now, return true for admins/owners
        return isAdmin();
    }

    @Override
    public String toString() {
        return "OrganizationMember{" +
                "id=" + id +
                ", userId=" + userId +
                ", organizationId=" + (organization != null ? organization.getId() : "null") +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
