package com.saasstarterkit.tenant.domain.model.enums;

/**
 * ConfigurationScope - Defines the scope of configuration values
 * 
 * This enum represents the different scopes where configuration
 * values can be applied in the tenant configuration system.
 */
public enum ConfigurationScope {
    
    /**
     * Global scope - applies to all tenants
     */
    GLOBAL("global"),
    
    /**
     * Tenant scope - applies to a specific tenant
     */
    TENANT("tenant"),
    
    /**
     * Organization scope - applies to a specific organization
     */
    ORGANIZATION("organization"),
    
    /**
     * User scope - applies to a specific user
     */
    USER("user"),
    
    /**
     * Role scope - applies to users with a specific role
     */
    ROLE("role"),
    
    /**
     * Group scope - applies to users in a specific group
     */
    GROUP("group"),
    
    /**
     * Department scope - applies to a specific department
     */
    DEPARTMENT("department"),
    
    /**
     * Project scope - applies to a specific project
     */
    PROJECT("project"),
    
    /**
     * Environment scope - applies to a specific environment
     */
    ENVIRONMENT("environment"),
    
    /**
     * Region scope - applies to a specific region
     */
    REGION("region"),
    
    /**
     * Data center scope - applies to a specific data center
     */
    DATA_CENTER("data_center"),
    
    /**
     * Service scope - applies to a specific service
     */
    SERVICE("service"),
    
    /**
     * Module scope - applies to a specific module
     */
    MODULE("module"),
    
    /**
     * Feature scope - applies to a specific feature
     */
    FEATURE("feature"),
    
    /**
     * API scope - applies to a specific API
     */
    API("api"),
    
    /**
     * Database scope - applies to a specific database
     */
    DATABASE("database"),
    
    /**
     * Queue scope - applies to a specific queue
     */
    QUEUE("queue"),
    
    /**
     * Cache scope - applies to a specific cache
     */
    CACHE("cache"),
    
    /**
     * Storage scope - applies to a specific storage
     */
    STORAGE("storage"),
    
    /**
     * Network scope - applies to a specific network
     */
    NETWORK("network"),
    
    /**
     * Security scope - applies to security settings
     */
    SECURITY("security"),
    
    /**
     * Compliance scope - applies to compliance settings
     */
    COMPLIANCE("compliance"),
    
    /**
     * Audit scope - applies to audit settings
     */
    AUDIT("audit"),
    
    /**
     * Monitoring scope - applies to monitoring settings
     */
    MONITORING("monitoring"),
    
    /**
     * Logging scope - applies to logging settings
     */
    LOGGING("logging"),
    
    /**
     * Notification scope - applies to notification settings
     */
    NOTIFICATION("notification"),
    
    /**
     * Integration scope - applies to integration settings
     */
    INTEGRATION("integration"),
    
    /**
     * Custom scope - applies to custom settings
     */
    CUSTOM("custom");
    
    private final String value;
    
    ConfigurationScope(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ConfigurationScope fromValue(String value) {
        for (ConfigurationScope scope : values()) {
            if (scope.value.equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown ConfigurationScope: " + value);
    }
    
    /**
     * Check if this scope is system-wide
     */
    public boolean isSystemWide() {
        return this == GLOBAL || this == ENVIRONMENT || this == REGION || 
               this == DATA_CENTER || this == SERVICE || this == MODULE;
    }
    
    /**
     * Check if this scope is user-specific
     */
    public boolean isUserSpecific() {
        return this == USER || this == ROLE || this == GROUP || this == DEPARTMENT;
    }
    
    /**
     * Check if this scope is tenant-specific
     */
    public boolean isTenantSpecific() {
        return this == TENANT || this == ORGANIZATION || this == PROJECT;
    }
    
    /**
     * Check if this scope is infrastructure-related
     */
    public boolean isInfrastructure() {
        return this == DATABASE || this == QUEUE || this == CACHE || 
               this == STORAGE || this == NETWORK || this == DATA_CENTER;
    }
    
    /**
     * Check if this scope is operational
     */
    public boolean isOperational() {
        return this == MONITORING || this == LOGGING || this == AUDIT || 
               this == SECURITY || this == COMPLIANCE;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
