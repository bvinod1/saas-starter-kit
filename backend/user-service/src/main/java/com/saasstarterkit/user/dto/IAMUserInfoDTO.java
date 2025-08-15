/*******************************************************************************
 *
 * Copyright (c) 2020-21 Mindsprint Limited
 *
 * All information contained herein is, and remains the property of Mindsprint
 * Limited. The intellectual and technical concepts contained herein are
 * proprietary to Mindsprint and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from Mindsprint
 * Limited
 *
 *******************************************************************************/
package com.saasstarterkit.user.dto;

import java.util.List;
import java.util.Map;

public class IAMUserInfoDTO {
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String userid;
    private Boolean enabled = true;
    private Boolean isTemporaryPassword = false;
    private List<String> roles;
    private Map<String, List<String>> attributes;
    
    // Getters
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserid() { return userid; }
    public Boolean getEnabled() { return enabled; }
    public Boolean getIsTemporaryPassword() { return isTemporaryPassword; }
    public List<String> getRoles() { return roles; }
    public Map<String, List<String>> getAttributes() { return attributes; }
    
    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUserid(String userid) { this.userid = userid; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setIsTemporaryPassword(Boolean isTemporaryPassword) { this.isTemporaryPassword = isTemporaryPassword; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setAttributes(Map<String, List<String>> attributes) { this.attributes = attributes; }
}
