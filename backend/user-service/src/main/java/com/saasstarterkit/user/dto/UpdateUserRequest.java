package com.saasstarterkit.user.dto;

import java.util.List;

public class UpdateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String position;
    private String status;
    private List<String> roles;
    private List<Long> organizationIds;

    // Constructors
    public UpdateUserRequest() {}

    public UpdateUserRequest(String email, String firstName, String lastName, String phone, 
                           String department, String position, String status, 
                           List<String> roles, List<Long> organizationIds) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.status = status;
        this.roles = roles;
        this.organizationIds = organizationIds;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Long> getOrganizationIds() {
        return organizationIds;
    }

    public void setOrganizationIds(List<Long> organizationIds) {
        this.organizationIds = organizationIds;
    }
}
