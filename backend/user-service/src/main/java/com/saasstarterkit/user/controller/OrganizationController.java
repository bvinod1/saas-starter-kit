package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.entity.TenantOrganization;
import com.saasstarterkit.user.service.OrganizationService;
import com.saasstarterkit.user.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    @GetMapping
    public ResponseEntity<?> getAllOrganizations() {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<TenantOrganization> organizations = organizationService.getAllOrganizations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", organizations);
            response.put("tenant", currentTenant);
            response.put("message", "Organizations retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get organizations: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long id) {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            return organizationService.getOrganizationById(id)
                    .map(org -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("data", org);
                        response.put("tenant", currentTenant);
                        response.put("message", "Organization retrieved successfully");
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get organization: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody Map<String, String> request) {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            String name = request.get("name");
            String slug = request.get("slug");
            String description = request.get("description");
            
            if (name == null || slug == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Name and slug are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            TenantOrganization organization = organizationService.createOrganization(name, slug, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", organization);
            response.put("tenant", currentTenant);
            response.put("message", "Organization created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create organization: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganization(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            TenantOrganization organization = organizationService.updateOrganization(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", organization);
            response.put("tenant", currentTenant);
            response.put("message", "Organization updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update organization: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long id) {
        try {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No tenant context found");
                return ResponseEntity.badRequest().body(error);
            }
            
            organizationService.deleteOrganization(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tenant", currentTenant);
            response.put("message", "Organization deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete organization: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
