package com.saasstarterkit.user.service;

import com.saasstarterkit.user.entity.TenantOrganization;
import com.saasstarterkit.user.repository.TenantOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {
    
    @Autowired
    private TenantOrganizationRepository organizationRepository;
    
    public List<TenantOrganization> getAllOrganizations() {
        return organizationRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<TenantOrganization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }
    
    public Optional<TenantOrganization> getOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(slug);
    }
    
    @Transactional
    public TenantOrganization createOrganization(String name, String slug, String description) {
        // Check if organization with this slug already exists
        if (organizationRepository.existsBySlug(slug)) {
            throw new RuntimeException("Organization with slug '" + slug + "' already exists");
        }
        
        TenantOrganization organization = new TenantOrganization();
        organization.setName(name);
        organization.setSlug(slug);
        organization.setDescription(description);
        
        return organizationRepository.save(organization);
    }
    
    @Transactional
    public TenantOrganization updateOrganization(Long id, java.util.Map<String, String> request) {
        Optional<TenantOrganization> orgOpt = organizationRepository.findById(id);
        
        if (orgOpt.isPresent()) {
            TenantOrganization organization = orgOpt.get();
            
            if (request.containsKey("name")) {
                organization.setName(request.get("name"));
            }
            if (request.containsKey("slug")) {
                String newSlug = request.get("slug");
                // Check if the new slug is different and already exists
                if (!newSlug.equals(organization.getSlug()) && organizationRepository.existsBySlug(newSlug)) {
                    throw new RuntimeException("Organization with slug '" + newSlug + "' already exists");
                }
                organization.setSlug(newSlug);
            }
            if (request.containsKey("description")) {
                organization.setDescription(request.get("description"));
            }
            if (request.containsKey("logoUrl")) {
                organization.setLogoUrl(request.get("logoUrl"));
            }
            if (request.containsKey("settings")) {
                organization.setSettings(request.get("settings"));
            }
            
            return organizationRepository.save(organization);
        } else {
            throw new RuntimeException("Organization not found with id: " + id);
        }
    }
    
    @Transactional
    public void deleteOrganization(Long id) {
        Optional<TenantOrganization> orgOpt = organizationRepository.findById(id);
        
        if (orgOpt.isPresent()) {
            TenantOrganization organization = orgOpt.get();
            
            // Check if this is the default organization (should not be deleted)
            if ("default".equals(organization.getSlug())) {
                throw new RuntimeException("Cannot delete the default organization");
            }
            
            organizationRepository.delete(organization);
        } else {
            throw new RuntimeException("Organization not found with id: " + id);
        }
    }
    
    public boolean organizationExists(String slug) {
        return organizationRepository.existsBySlug(slug);
    }
}
