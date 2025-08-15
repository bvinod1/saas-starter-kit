/**
 * 
 */
package com.saasstarterkit.user.dto;

public class UserModel {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean enabled;
    
    // Default constructor
    public UserModel() {}
    
    // Constructor with all fields
    public UserModel(String email, String firstName, String lastName, String username, Boolean enabled) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.enabled = enabled;
    }
    
    // Getters
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public Boolean getEnabled() { return enabled; }
    
    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
