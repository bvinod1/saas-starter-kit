package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.config.TenantContext;
import com.saasstarterkit.user.dto.AdminUserDTO;
import com.saasstarterkit.user.dto.CreateUserRequest;
import com.saasstarterkit.user.dto.UpdateUserRequest;
import com.saasstarterkit.user.dto.UserRoleDTO;
import com.saasstarterkit.user.entity.TenantUser;
import com.saasstarterkit.user.entity.TenantOrganization;
import com.saasstarterkit.user.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // User Management Endpoints

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<AdminUserDTO> users = adminService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users,
                "tenant", TenantContext.getCurrentTenant()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch users: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            AdminUserDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch user: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            AdminUserDTO user = adminService.createUser(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user,
                "message", "User created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create user: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        try {
            AdminUserDTO user = adminService.updateUser(userId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user,
                "message", "User updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update user: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete user: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            adminService.activateUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User activated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to activate user: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            adminService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to deactivate user: " + e.getMessage()
            ));
        }
    }

    // Role Management Endpoints

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<UserRoleDTO> roles = adminService.getAllRoles();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch roles: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable Long userId) {
        try {
            List<UserRoleDTO> roles = adminService.getUserRoles(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch user roles: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String roleName = request.get("roleName");
            adminService.assignRoleToUser(userId, roleName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role assigned successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to assign role: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            adminService.removeRoleFromUser(userId, roleName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role removed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to remove role: " + e.getMessage()
            ));
        }
    }

    // Organization Management Endpoints

    @GetMapping("/organizations")
    public ResponseEntity<?> getAllOrganizations() {
        try {
            List<TenantOrganization> organizations = adminService.getAllOrganizations();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", organizations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch organizations: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{userId}/organizations/{organizationId}")
    public ResponseEntity<?> addUserToOrganization(@PathVariable Long userId, @PathVariable Long organizationId) {
        try {
            adminService.addUserToOrganization(userId, organizationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User added to organization successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to add user to organization: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/users/{userId}/organizations/{organizationId}")
    public ResponseEntity<?> removeUserFromOrganization(@PathVariable Long userId, @PathVariable Long organizationId) {
        try {
            adminService.removeUserFromOrganization(userId, organizationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User removed from organization successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to remove user from organization: " + e.getMessage()
            ));
        }
    }

    // Dashboard Statistics

    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch dashboard stats: " + e.getMessage()
            ));
        }
    }
}
