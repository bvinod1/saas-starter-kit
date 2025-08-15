package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;

public class UserRoleDTO {
    private Long id;
    private String roleName;
    private String description;
    private String permissions;
    private LocalDateTime assignedAt;
    private String assignedBy;

    // Constructors
    public UserRoleDTO() {}

    public UserRoleDTO(Long id, String roleName, String description, String permissions, 
                      LocalDateTime assignedAt, String assignedBy) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }
}
