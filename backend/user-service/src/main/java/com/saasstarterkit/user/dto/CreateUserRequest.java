package com.saasstarterkit.user.dto;

import java.util.List;

public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String position;
    private List<String> roles;
    private List<Long> organizationIds;
    private String password;

    // Constructors
    public CreateUserRequest() {}

    public CreateUserRequest(String email, String firstName, String lastName, String phone, 
                           String department, String position, List<String> roles, 
                           List<Long> organizationIds, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.roles = roles;
        this.organizationIds = organizationIds;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
