package com.saasstarterkit.user.service;

import com.saasstarterkit.user.entity.Tenant;
import com.saasstarterkit.user.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
    
    public Optional<Tenant> getTenantBySlug(String slug) {
        return tenantRepository.findActiveTenantBySlug(slug);
    }
    
    public Optional<Tenant> getTenantById(UUID id) {
        return tenantRepository.findById(id);
    }
    
    @Transactional
    public Tenant createTenant(String name, String slug, String domain, String keycloakRealmId, String keycloakClientId) {
        // Check if tenant already exists
        if (tenantRepository.existsBySlug(slug)) {
            throw new RuntimeException("Tenant with slug " + slug + " already exists");
        }
        
        // Create tenant record
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID()); // Generate ID manually
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setDomain(domain);
        tenant.setKeycloakRealmId(keycloakRealmId);
        tenant.setKeycloakClientId(keycloakClientId);
        tenant.setStatus("active");
        tenant.setSubscriptionStatus("trial");
        tenant.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        tenant.setSettings("{}"); // Ensure proper JSON format
        
        // Use native query to handle JSONB properly
        String insertSql = "INSERT INTO tenant_management.tenants (id, name, slug, domain, keycloak_realm_id, keycloak_client_id, status, subscription_status, trial_ends_at, settings, created_at, updated_at) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)";
        
        jdbcTemplate.update(insertSql,
            tenant.getId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getDomain(),
            tenant.getKeycloakRealmId(),
            tenant.getKeycloakClientId(),
            tenant.getStatus(),
            tenant.getSubscriptionStatus(),
            tenant.getTrialEndsAt(),
            tenant.getSettings(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
        
        // Create tenant schema with the organization name
        createTenantSchema(slug, name);
        
        return tenant;
    }
    
    @Transactional
    public void createTenantSchema(String tenantSlug, String organizationName) {
        // Sanitize schema name - replace hyphens and other invalid characters with underscores
        String sanitizedSlug = tenantSlug.replaceAll("[^a-zA-Z0-9_]", "_");
        String schemaName = "tenant_" + sanitizedSlug;
        
        // Create schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        
        // Create tenant-specific tables
        createTenantTables(schemaName);
        
        // Insert default organization with the actual organization name
        jdbcTemplate.update(
            "INSERT INTO " + schemaName + ".organizations (name, slug, description) " +
            "VALUES (?, 'default', 'Default organization for this tenant') " +
            "ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name",
            organizationName
        );
    }
    
    private void createTenantTables(String schemaName) {
        // Users table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS " + schemaName + ".users (" +
            "id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), " +
            "keycloak_user_id VARCHAR(255) UNIQUE NOT NULL, " +
            "email VARCHAR(255) NOT NULL, " +
            "first_name VARCHAR(100), " +
            "last_name VARCHAR(100), " +
            "avatar_url TEXT, " +
            "bio TEXT, " +
            "timezone VARCHAR(50) DEFAULT 'UTC', " +
            "language VARCHAR(10) DEFAULT 'en', " +
            "preferences JSONB DEFAULT '{}', " +
            "status VARCHAR(50) DEFAULT 'active', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        // Organizations table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS " + schemaName + ".organizations (" +
            "id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), " +
            "name VARCHAR(255) NOT NULL, " +
            "slug VARCHAR(100) UNIQUE NOT NULL, " +
            "description TEXT, " +
            "logo_url TEXT, " +
            "settings JSONB DEFAULT '{}', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        // Add more tables as needed...
    }
    
    @Transactional
    public void deleteTenant(UUID tenantId) {
        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isPresent()) {
            Tenant tenant = tenantOpt.get();
            
            // Mark tenant as deleted
            tenant.setStatus("deleted");
            tenantRepository.save(tenant);
            
            // Optionally drop the schema (be careful with this in production)
            // dropTenantSchema(tenant.getSlug());
        }
    }
    
    @Transactional
    public void dropTenantSchema(String tenantSlug) {
        // Sanitize schema name - replace hyphens and other invalid characters with underscores
        String sanitizedSlug = tenantSlug.replaceAll("[^a-zA-Z0-9_]", "_");
        String schemaName = "tenant_" + sanitizedSlug;
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }
    
    public boolean tenantExists(String slug) {
        return tenantRepository.existsBySlug(slug);
    }
    
    public boolean isTenantActive(String slug) {
        return tenantRepository.findActiveTenantBySlug(slug).isPresent();
    }
}
