package com.saasstarterkit.auth.service;

import com.saasstarterkit.auth.dto.LoginRequest;
import com.saasstarterkit.auth.dto.RegisterRequest;
import com.saasstarterkit.auth.dto.RegisterResponse;
import com.saasstarterkit.auth.dto.TokenResponse;
import com.saasstarterkit.auth.exception.UserAlreadyExistsException;
import com.saasstarterkit.auth.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate request
        validateRegistrationRequest(request);

        try {
            // 1. Create user in Keycloak (this also creates the organization client)
            RegisterResponse keycloakResponse = keycloakService.register(request);
            String keycloakUserId = keycloakResponse.getData().getId();
            String organizationClientId = keycloakResponse.getData().getOrganizationClientId();
            
            System.out.println("Successfully created user in Keycloak: " + keycloakUserId);
            System.out.println("Organization client ID: " + organizationClientId);

            // 2. Create tenant in PostgreSQL using the organization client ID
            try {
                createTenantInDatabase(request, keycloakUserId, organizationClientId);
                System.out.println("Successfully created tenant in database");
            } catch (Exception e) {
                System.err.println("Warning: Failed to create tenant in database: " + e.getMessage());
                // Don't fail registration if tenant creation fails
            }

            // Return the Keycloak response
            return keycloakResponse;

        } catch (Exception e) {
            throw new ValidationException("Registration failed: " + e.getMessage());
        }
    }

    private void createTenantInDatabase(RegisterRequest request, String keycloakUserId, String organizationClientId) {
        try {
            // Generate tenant slug from organization name
            String tenantSlug = generateTenantSlug(request.getOrganizationName());
            
            // Create tenant using the database function
            String sql = "SELECT create_tenant(?, ?, ?, ?)";
            
            System.out.println("Creating tenant with:");
            System.out.println("  - Name: " + request.getOrganizationName());
            System.out.println("  - Slug: " + tenantSlug);
            System.out.println("  - Realm: saas-starter-kit");
            System.out.println("  - Client: " + organizationClientId);
            
            // Call the create_tenant function
            UUID tenantId = jdbcTemplate.queryForObject(sql, UUID.class, 
                request.getOrganizationName(), 
                tenantSlug, 
                "saas-starter-kit", 
                organizationClientId
            );
            
            System.out.println("Created tenant with ID: " + tenantId);
            
        } catch (Exception e) {
            System.err.println("Error creating tenant in database: " + e.getMessage());
            throw e;
        }
    }

    private String generateTenantSlug(String organizationName) {
        // Convert organization name to a valid slug
        String slug = organizationName.toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        
        // Ensure it's not too long and add uniqueness
        if (slug.length() > 50) {
            slug = slug.substring(0, 50);
        }
        
        return slug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password and confirm password do not match");
        }

        // Additional validation can be added here
        if (request.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
    }

    @Transactional
    public RegisterResponse login(LoginRequest request) {
        try {
            return keycloakService.login(request);
        } catch (Exception e) {
            throw new ValidationException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public TokenResponse refreshToken(String refreshToken) {
        try {
            return keycloakService.refreshToken(refreshToken);
        } catch (Exception e) {
            throw new ValidationException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Validate token with Keycloak
     */
    public boolean validateToken(String token) {
        try {
            return keycloakService.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return keycloakService.isTokenExpired(token);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get token expiration time
     */
    public long getTokenExpirationTime(String token) {
        try {
            return keycloakService.getTokenExpirationTime(token);
        } catch (Exception e) {
            return 0;
        }
    }
} 