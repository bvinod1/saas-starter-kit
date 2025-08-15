package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.dto.IAMUserDTO;
import com.saasstarterkit.user.dto.IAMUserInfoDTO;
import com.saasstarterkit.user.dto.UserProfileDTO;
import com.saasstarterkit.user.entity.User;
import com.saasstarterkit.user.entity.TenantUser;
import com.saasstarterkit.user.service.UserService;
import com.saasstarterkit.user.service.TenantUserService;
import com.saasstarterkit.user.service.LoggingService;
import com.saasstarterkit.user.service.TracingService;
import com.saasstarterkit.user.config.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TenantUserService tenantUserService;
    
    @Autowired
    private LoggingService loggingService;
    
    @Autowired
    private TracingService tracingService;
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        String correlationId = java.util.UUID.randomUUID().toString();
        
        // Extract tenant context from request or current context
        String tenantId = (String) request.get("tenantId");
        String organizationId = request.get("organizationId") != null ? request.get("organizationId").toString() : null;
        
        // Set tenant context for logging
        if (tenantId != null) {
            loggingService.setTenantContext(tenantId, "Tenant-" + tenantId, organizationId);
        }
        
        Map<String, String> context = Map.of(
            "correlationId", correlationId,
            "operation", "createUser",
            "tenantId", tenantId != null ? tenantId : "unknown",
            "organizationId", organizationId != null ? organizationId : "unknown"
        );
        
        // Create OpenTelemetry span for the operation
        return tracingService.executeWithSpan("user.create", context, () -> {
            try {
                loggingService.logInfo("Starting user creation process", context);
                
                String keycloakUserId = (String) request.get("keycloakUserId");
                String email = (String) request.get("email");
                String firstName = (String) request.get("firstName");
                String lastName = (String) request.get("lastName");
                String[] roles = (String[]) request.get("roles");

                if (keycloakUserId == null || email == null || firstName == null || lastName == null) {
                    loggingService.logWarn("User creation failed - missing required fields", context);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "keycloakUserId, email, firstName, and lastName are required");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

                // Add business context to span
                Map<String, Object> userData = Map.of(
                    "email", email,
                    "firstName", firstName,
                    "lastName", lastName,
                    "keycloakUserId", keycloakUserId
                );
                
                tracingService.addBusinessContext(tenantId, keycloakUserId, "USER_CREATE", userData);

                // Log data access event for compliance
                loggingService.logDataAccessEvent(
                    "USER_PROFILE",
                    "CREATE",
                    keycloakUserId,
                    tenantId,
                    "PII_MEDIUM",
                    userData
                );

                // Create user profile with tracing
                User user = tracingService.executeWithSpan("user.createProfile", Map.of(
                    "userId", keycloakUserId,
                    "tenantId", tenantId,
                    "operation", "createProfile"
                ), () -> userService.createUserProfile(keycloakUserId, email, firstName, lastName));
                
                // Assign roles with tracing
                if (roles != null) {
                    for (String role : roles) {
                        tracingService.executeWithSpan("user.assignRole", Map.of(
                            "userId", keycloakUserId,
                            "role", role,
                            "tenantId", tenantId
                        ), () -> {
                            userService.assignUserRoles(keycloakUserId, List.of(role));
                            return null;
                        });
                    }
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", user);
                response.put("message", "User created successfully");
                
                long duration = System.currentTimeMillis() - startTime;
                loggingService.logPerformanceMetric("createUser", duration, context);
                
                // Add performance metrics to span
                tracingService.addPerformanceMetrics("createUser", duration, "SUCCESS");
                
                // Log business event with compliance level
                String complianceLevel = determineComplianceLevel(tenantId);
                loggingService.logBusinessEvent("USER_CREATED", keycloakUserId, tenantId, 
                    userData, complianceLevel);
                
                // Log tenant isolation event
                loggingService.logTenantIsolationEvent(
                    "USER_CREATED",
                    tenantId,
                    "User profile created and isolated to tenant",
                    context
                );
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                loggingService.logError("User creation failed", e, context);
                loggingService.logPerformanceMetric("createUser", duration, context);
                
                // Add error to span
                tracingService.addError(e);
                tracingService.addPerformanceMetrics("createUser", duration, "ERROR");
                
                // Log security event for failed user creation
                loggingService.logSecurityEvent(
                    "USER_CREATION_FAILED",
                    "system",
                    getClientIpAddress(),
                    "Exception during user creation: " + e.getMessage(),
                    determineComplianceLevel(tenantId),
                    context
                );
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        });
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody IAMUserDTO userDTO) {
        try {
            IAMUserDTO createdUser = userService.createCompleteUser(userDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", createdUser.getUserid());
            response.put("applicationUserId", createdUser.getApplicationUserId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String usernameOrEmail = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (usernameOrEmail == null || password == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username and password are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            String token = userService.authenticateUser(usernameOrEmail, password);
            if (token != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("authenticated", true);
                response.put("token", token);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid credentials");
                return ResponseEntity.status(401).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            UserProfileDTO completeUser = userService.getCompleteUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User retrieved successfully");
            response.put("data", completeUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            // First find the user profile by email (username is typically email)
            return userService.findByEmail(username)
                    .map(user -> {
                        // Get complete user info from IAM
                        UserProfileDTO completeUser = userService.getCompleteUser(user.getKeycloakUserId());
                        Map<String, Object> response = new HashMap<>();
                        response.put("message", "User profile retrieved successfully");
                        response.put("data", completeUser);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody User profileUpdate) {
        try {
            // Update only the application profile, not IAM data
            User updatedProfile = userService.updateUserProfile(profileUpdate);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User profile updated successfully");
            response.put("data", updatedProfile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            boolean deleted = userService.deleteCompleteUser(userId);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to delete user");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            // This would need to be implemented to get all users from IAM
            Map<String, String> error = new HashMap<>();
            error.put("error", "Getting all users not implemented yet. Use tenant-specific endpoints.");
            return ResponseEntity.status(501).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get users: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<?> getUsersByTenantId(@PathVariable String tenantId) {
        try {
            List<User> users = userService.getUsersByTenantId(tenantId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users retrieved successfully");
            response.put("data", users);
            response.put("tenantId", tenantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get users: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            // Find user by email first
            return userService.findByEmail(email)
                    .map(user -> {
                        // Trigger password reset email via IAM
                        String result = userService.triggerEmail(
                            user.getKeycloakUserId(), 
                            List.of(com.saasstarterkit.user.service.interfaces.IIAMProvider.MailAction.RESET_PASSWORD)
                        );
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Password reset email sent");
                        response.put("result", result);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send reset email: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String newPassword = request.get("newPassword");
        
        if (userId == null || newPassword == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User ID and new password are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            boolean reset = userService.resetPassword(userId, newPassword, false);
            if (reset) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to reset password");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Get current user from IAM
            IAMUserInfoDTO iamUser = userService.getCurrentUser();
            if (iamUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found in current tenant");
                return ResponseEntity.notFound().build();
            }
            
            // Get complete user profile
            UserProfileDTO currentUser = userService.getCompleteUser(iamUser.getUserid());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", currentUser);
            response.put("tenant", currentTenant);
            response.put("message", "User profile retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user profile: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/tenants")
    public ResponseEntity<?> getUserTenants() {
        try {
            List<Map<String, Object>> userTenants = tenantUserService.getUserTenants();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userTenants);
            response.put("message", "User tenants retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user tenants: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "User Service");
        response.put("tenant", TenantContext.getCurrentTenant());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Determine compliance level based on tenant configuration
     */
    private String determineComplianceLevel(String tenantId) {
        if (tenantId == null) {
            return "BASIC";
        }
        
        // Example logic - in production, this would come from tenant configuration
        if (tenantId.startsWith("enterprise_")) {
            return "ENTERPRISE";
        } else if (tenantId.startsWith("healthcare_")) {
            return "HIPAA";
        } else if (tenantId.startsWith("financial_")) {
            return "SOX_PCI";
        }
        
        return "BASIC";
    }
    
    /**
     * Get client IP address from request context
     */
    private String getClientIpAddress() {
        // This would typically come from the request context
        // For now, return a placeholder
        return "unknown";
    }
} 