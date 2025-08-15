package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.entity.Tenant;
import com.saasstarterkit.user.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllTenants() {
        List<Tenant> tenants = tenantService.getAllTenants();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tenants);
        response.put("message", "Tenants retrieved successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getTenantBySlug(@PathVariable String slug) {
        return tenantService.getTenantBySlug(slug)
                .map(tenant -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", tenant);
                    response.put("message", "Tenant retrieved successfully");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTenant(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String slug = request.get("slug");
            String domain = request.get("domain");
            String keycloakRealmId = request.get("keycloakRealmId");
            String keycloakClientId = request.get("keycloakClientId");

            if (name == null || slug == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Name and slug are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Tenant tenant = tenantService.createTenant(name, slug, domain, keycloakRealmId, keycloakClientId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tenant);
            response.put("message", "Tenant created successfully with organization: " + name);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTenant(@PathVariable UUID id) {
        try {
            tenantService.deleteTenant(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tenant deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/check/{slug}")
    public ResponseEntity<Map<String, Object>> checkTenantExists(@PathVariable String slug) {
        boolean exists = tenantService.tenantExists(slug);
        boolean isActive = tenantService.isTenantActive(slug);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "exists", exists,
            "active", isActive
        ));
        response.put("message", "Tenant check completed");
        
        return ResponseEntity.ok(response);
    }
}
