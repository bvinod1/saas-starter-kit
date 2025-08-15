package com.saasstarterkit.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.auth.dto.LoginRequest;
import com.saasstarterkit.auth.dto.RegisterRequest;
import com.saasstarterkit.auth.dto.RegisterResponse;
import com.saasstarterkit.auth.dto.TokenResponse;
import com.saasstarterkit.auth.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:saas-starter-kit}")
    private String realm;

    @Value("${keycloak.client-id:saas-backend}")
    private String clientId;

    @Value("${keycloak.client-secret:your-backend-secret-here}")
    private String clientSecret;

    // Admin credentials for user management
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KeycloakService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Register a new user in Keycloak
     */
    public RegisterResponse register(RegisterRequest request) {
        try {
            // Get admin token for user management
            String adminToken = getAdminToken();

            // 1. Create a new client for the organization
            String organizationClientId = createOrganizationClient(request.getOrganizationName(), adminToken);
            System.out.println("Created organization client: " + organizationClientId);

            // 2. Create user in the main realm with organization info in attributes
            String userId = createUserInRealm(request, adminToken, organizationClientId);

            // 3. Get user info and generate token
            Map<String, Object> userInfo = getUserInfoFromRealm(userId, adminToken);
            
            // 4. Generate token for the new user using the organization client
            TokenResponse tokenResponse = getUserTokenWithClient(request.getEmail(), request.getPassword(), organizationClientId);

            // Create response with client ID for tenant creation
            RegisterResponse.UserData userData = new RegisterResponse.UserData(
                userId,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                tokenResponse.getAccessToken()
            );

            // Store client ID and refresh token in response for tenant creation
            userData.setOrganizationClientId(organizationClientId);
            userData.setRefreshToken(tokenResponse.getRefreshToken());
            userData.setExpiresIn(tokenResponse.getExpiresIn());

            return RegisterResponse.success("User registered successfully", userData);

        } catch (Exception e) {
            throw new ValidationException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticate user with Keycloak
     */
    public RegisterResponse login(LoginRequest request) {
        try {
            // Get user token using the saas-backend client
            TokenResponse tokenResponse = getUserTokenWithClient(request.getEmail(), request.getPassword(), clientId);

            // Get user info from token
            Map<String, Object> userInfo = getUserInfoFromToken(tokenResponse.getAccessToken());

            // Create response
            RegisterResponse.UserData userData = new RegisterResponse.UserData(
                (String) userInfo.get("user_id"),
                request.getEmail(),
                (String) userInfo.get("given_name"),
                (String) userInfo.get("family_name"),
                tokenResponse.getAccessToken()
            );

            // Add refresh token and expiration info
            userData.setRefreshToken(tokenResponse.getRefreshToken());
            userData.setExpiresIn(tokenResponse.getExpiresIn());

            return RegisterResponse.success("Login successful", userData);

        } catch (Exception e) {
            throw new ValidationException("Invalid email or password");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public TokenResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseTokenResponse(jsonNode);
        } catch (Exception e) {
            throw new ValidationException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            JsonNode claims = decodeTokenClaims(token);
            if (claims != null && claims.has("exp")) {
                long expirationTime = claims.get("exp").asLong();
                long currentTime = Instant.now().getEpochSecond();
                return currentTime >= expirationTime;
            }
            return true; // If we can't decode, assume expired
        } catch (Exception e) {
            return true; // If there's an error, assume expired
        }
    }

    /**
     * Get token expiration time
     */
    public long getTokenExpirationTime(String token) {
        try {
            JsonNode claims = decodeTokenClaims(token);
            if (claims != null && claims.has("exp")) {
                return claims.get("exp").asLong();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Decode JWT token claims without verification (for expiration check)
     */
    private JsonNode decodeTokenClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = parts[1];
                // Add padding if needed
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }
                // Replace URL-safe characters
                payload = payload.replace('-', '+').replace('_', '/');
                
                // Decode base64
                java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
                String decodedPayload = new String(decoder.decode(payload));
                
                return objectMapper.readTree(decodedPayload);
            }
        } catch (Exception e) {
            // Ignore decoding errors
        }
        return null;
    }

    /**
     * Parse token response from Keycloak
     */
    private TokenResponse parseTokenResponse(JsonNode jsonNode) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken(jsonNode.get("access_token").asText());
        response.setRefreshToken(jsonNode.get("refresh_token").asText());
        response.setExpiresIn(jsonNode.get("expires_in").asLong());
        response.setRefreshExpiresIn(jsonNode.get("refresh_expires_in").asLong());
        response.setTokenType(jsonNode.get("token_type").asText());
        return response;
    }

    /**
     * Get admin token for Keycloak operations using admin credentials
     */
    public String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get admin token: " + e.getMessage(), e);
        }
    }

    /**
     * Create a client for the organization in the main realm
     */
    private String createOrganizationClient(String organizationName, String adminToken) {
        String clientsUrl = keycloakUrl + "/admin/realms/" + realm + "/clients";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        // Generate a unique client ID for the organization
        String orgClientId = generateClientId(organizationName);

        Map<String, Object> clientRepresentation = new HashMap<>();
        clientRepresentation.put("clientId", orgClientId);
        clientRepresentation.put("enabled", true);
        clientRepresentation.put("publicClient", false);
        clientRepresentation.put("redirectUris", new String[]{"http://localhost:3000/*"});
        clientRepresentation.put("webOrigins", new String[]{"http://localhost:3000"});
        clientRepresentation.put("standardFlowEnabled", true);
        clientRepresentation.put("directAccessGrantsEnabled", true);
        clientRepresentation.put("serviceAccountsEnabled", true);
        clientRepresentation.put("attributes", Map.of(
            "organization", organizationName,
            "organizationId", UUID.randomUUID().toString()
        ));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(clientRepresentation, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(clientsUrl, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Extract client ID from Location header
                String location = response.getHeaders().getFirst("Location");
                if (location != null) {
                    return location.substring(location.lastIndexOf("/") + 1);
                }
            }
            // If we can't get the ID from header, return the generated client ID
            return orgClientId;
        } catch (Exception e) {
            System.err.println("Failed to create client for organization " + organizationName + ": " + e.getMessage());
            // Return the generated client ID as fallback
            return orgClientId;
        }
    }

    /**
     * Generate a unique client ID for the organization
     */
    private String generateClientId(String organizationName) {
        // Convert organization name to a valid client ID (lowercase, no spaces, no special chars)
        String clientId = organizationName.toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        
        // Ensure it's not too long and unique
        if (clientId.length() > 50) {
            clientId = clientId.substring(0, 50);
        }
        
        // Add a unique suffix
        return clientId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Create user in the main realm
     */
    private String createUserInRealm(RegisterRequest request, String adminToken, String organizationClientId) {
        String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userRepresentation = new HashMap<>();
        userRepresentation.put("username", request.getEmail());
        userRepresentation.put("email", request.getEmail());
        userRepresentation.put("firstName", request.getFirstName());
        userRepresentation.put("lastName", request.getLastName());
        userRepresentation.put("enabled", true);
        userRepresentation.put("emailVerified", true);
        userRepresentation.put("attributes", Map.of(
            "id", UUID.randomUUID().toString(),
            "organization", request.getOrganizationName(),
            "organizationId", UUID.randomUUID().toString(),
            "organizationClientId", organizationClientId
        ));

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", request.getPassword());
        credentials.put("temporary", false);

        userRepresentation.put("credentials", new Object[]{credentials});

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userRepresentation, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(usersUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            // Extract user ID from Location header
            String location = response.getHeaders().getFirst("Location");
            if (location != null) {
                return location.substring(location.lastIndexOf("/") + 1);
            }
        }

        throw new RuntimeException("Failed to create user in realm " + realm);
    }

    /**
     * Get user token from Keycloak using a specific client
     */
    private TokenResponse getUserTokenWithClient(String email, String password, String clientId) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        // Note: For now, we'll use the saas-backend client secret as a fallback
        // In production, each organization client should have its own secret
        body.add("client_secret", clientSecret);
        body.add("username", email);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseTokenResponse(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user token with client " + clientId, e);
        }
    }

    /**
     * Get user info from admin token
     */
    private Map<String, Object> getUserInfo(String userId, String adminToken) {
        String userUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userUrl, HttpMethod.GET, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", jsonNode.get("attributes").get("id").get(0).asText());
            userInfo.put("given_name", jsonNode.get("firstName").asText());
            userInfo.put("family_name", jsonNode.get("lastName").asText());
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user info", e);
        }
    }

    /**
     * Get user info from token
     */
    private Map<String, Object> getUserInfoFromToken(String token) {
        String userInfoUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", jsonNode.get("user_id").asText());
            userInfo.put("given_name", jsonNode.get("given_name").asText());
            userInfo.put("family_name", jsonNode.get("family_name").asText());
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user info from token", e);
        }
    }

    /**
     * Get user info from the main realm
     */
    private Map<String, Object> getUserInfoFromRealm(String userId, String adminToken) {
        String userUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userUrl, HttpMethod.GET, request, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", jsonNode.get("attributes").get("id").get(0).asText());
            userInfo.put("given_name", jsonNode.get("firstName").asText());
            userInfo.put("family_name", jsonNode.get("lastName").asText());
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user info from realm " + realm, e);
        }
    }

    /**
     * Validate token with Keycloak
     */
    public boolean validateToken(String token) {
        try {
            String introspectUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", token);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(introspectUrl, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("active").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }
} 