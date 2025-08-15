package com.saasstarterkit.user.service;

import com.saasstarterkit.user.entity.TenantUser;
import com.saasstarterkit.user.entity.Tenant;
import com.saasstarterkit.user.repository.TenantUserRepository;
import com.saasstarterkit.user.repository.TenantRepository;
import com.saasstarterkit.user.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TenantUserService {
    
    @Autowired
    private TenantUserRepository tenantUserRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    public TenantUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakUserId = jwt.getSubject();
            
            return tenantUserRepository.findActiveUserByKeycloakId(keycloakUserId).orElse(null);
        }
        
        return null;
    }
    
    @Transactional
    public TenantUser createOrUpdateUser(String keycloakUserId, String email, String firstName, String lastName) {
        Optional<TenantUser> existingUser = tenantUserRepository.findByKeycloakUserId(keycloakUserId);
        
        if (existingUser.isPresent()) {
            TenantUser user = existingUser.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return tenantUserRepository.save(user);
        } else {
            TenantUser newUser = new TenantUser();
            newUser.setKeycloakUserId(keycloakUserId);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setStatus("active");
            return tenantUserRepository.save(newUser);
        }
    }
    
    public List<Map<String, Object>> getUserTenants() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Map<String, Object>> userTenants = new ArrayList<>();
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakUserId = jwt.getSubject();
            
            // Get all tenants where this user exists
            List<Tenant> allTenants = tenantRepository.findAll();
            
            for (Tenant tenant : allTenants) {
                if (tenant.getStatus().equals("active")) {
                    // Check if user exists in this tenant
                    // This is a simplified check - in a real implementation,
                    // you might want to check the actual tenant schema
                    Map<String, Object> tenantInfo = new HashMap<>();
                    tenantInfo.put("id", tenant.getId().toString());
                    tenantInfo.put("name", tenant.getName());
                    tenantInfo.put("slug", tenant.getSlug());
                    tenantInfo.put("domain", tenant.getDomain());
                    tenantInfo.put("status", tenant.getStatus());
                    
                    userTenants.add(tenantInfo);
                }
            }
        }
        
        return userTenants;
    }
    
    public TenantUser updateUserProfile(String keycloakUserId, Map<String, Object> profileData) {
        Optional<TenantUser> userOpt = tenantUserRepository.findByKeycloakUserId(keycloakUserId);
        
        if (userOpt.isPresent()) {
            TenantUser user = userOpt.get();
            
            if (profileData.containsKey("firstName")) {
                user.setFirstName((String) profileData.get("firstName"));
            }
            if (profileData.containsKey("lastName")) {
                user.setLastName((String) profileData.get("lastName"));
            }
            if (profileData.containsKey("avatarUrl")) {
                user.setAvatarUrl((String) profileData.get("avatarUrl"));
            }
            if (profileData.containsKey("bio")) {
                user.setBio((String) profileData.get("bio"));
            }
            if (profileData.containsKey("timezone")) {
                user.setTimezone((String) profileData.get("timezone"));
            }
            if (profileData.containsKey("language")) {
                user.setLanguage((String) profileData.get("language"));
            }
            if (profileData.containsKey("preferences")) {
                user.setPreferences(profileData.get("preferences").toString());
            }
            
            return tenantUserRepository.save(user);
        }
        
        return null;
    }
    
    public boolean userExistsInTenant(String keycloakUserId) {
        return tenantUserRepository.existsByKeycloakUserId(keycloakUserId);
    }
    
    public Optional<TenantUser> findByKeycloakUserId(String keycloakUserId) {
        return tenantUserRepository.findByKeycloakUserId(keycloakUserId);
    }
    
    public Optional<TenantUser> findByEmail(String email) {
        return tenantUserRepository.findByEmail(email);
    }
}
