package com.saasstarterkit.organisation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class MemberAddRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private String role;

    private String department;
    private String jobTitle;
    private String accessLevel;

    // Constructors
    public MemberAddRequest() {}

    public MemberAddRequest(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    @Override
    public String toString() {
        return "MemberAddRequest{" +
                "userId=" + userId +
                ", role='" + role + '\'' +
                ", department='" + department + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                '}';
    }
}
