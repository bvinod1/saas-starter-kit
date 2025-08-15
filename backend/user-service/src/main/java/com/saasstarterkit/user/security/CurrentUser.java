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
package com.saasstarterkit.user.security;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mohankumar.D
 *
 */
public class CurrentUser {

    private String userId;
    private String username;
    private String email;
    private List<String> roles;
    private Map<String, Object> attribute;
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
    public Map<String, Object> getAttribute() { return attribute; }
    
    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setAttribute(Map<String, Object> attribute) { this.attribute = attribute; }
}
