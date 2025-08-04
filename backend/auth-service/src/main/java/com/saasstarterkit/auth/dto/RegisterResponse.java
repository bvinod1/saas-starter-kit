package com.saasstarterkit.auth.dto;

import java.time.LocalDateTime;

public class RegisterResponse {
    private boolean success;
    private String message;
    private UserData data;
    private LocalDateTime timestamp;

    public RegisterResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public RegisterResponse(boolean success, String message, UserData data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Static builder methods
    public static RegisterResponse success(String message, UserData data) {
        return new RegisterResponse(true, message, data);
    }

    public static RegisterResponse error(String message) {
        return new RegisterResponse(false, message, null);
    }

    // Inner class for user data
    public static class UserData {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String token;

        public UserData() {}

        public UserData(String id, String email, String firstName, String lastName, String token) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.token = token;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
} 