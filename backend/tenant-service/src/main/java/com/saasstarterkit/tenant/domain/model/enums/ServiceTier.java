package com.saasstarterkit.tenant.domain.model.enums;

/**
 * ServiceTier - Defines the service tier for configuration values
 * 
 * This enum represents the different service tiers where configuration
 * values can be applied in the tenant configuration system.
 */
public enum ServiceTier {
    
    /**
     * Basic service tier
     */
    BASIC("basic"),
    
    /**
     * Standard service tier
     */
    STANDARD("standard"),
    
    /**
     * Premium service tier
     */
    PREMIUM("premium"),
    
    /**
     * Enterprise service tier
     */
    ENTERPRISE("enterprise"),
    
    /**
     * Starter service tier
     */
    STARTER("starter"),
    
    /**
     * Professional service tier
     */
    PROFESSIONAL("professional"),
    
    /**
     * Business service tier
     */
    BUSINESS("business"),
    
    /**
     * Ultimate service tier
     */
    ULTIMATE("ultimate"),
    
    /**
     * Free service tier
     */
    FREE("free"),
    
    /**
     * Trial service tier
     */
    TRIAL("trial"),
    
    /**
     * Pay-as-you-go service tier
     */
    PAYG("payg"),
    
    /**
     * Reserved instance service tier
     */
    RESERVED("reserved"),
    
    /**
     * Spot instance service tier
     */
    SPOT("spot"),
    
    /**
     * Dedicated service tier
     */
    DEDICATED("dedicated"),
    
    /**
     * Shared service tier
     */
    SHARED("shared"),
    
    /**
     * Multi-tenant service tier
     */
    MULTI_TENANT("multi_tenant"),
    
    /**
     * Single-tenant service tier
     */
    SINGLE_TENANT("single_tenant"),
    
    /**
     * Development service tier
     */
    DEVELOPMENT("development"),
    
    /**
     * Testing service tier
     */
    TESTING("testing"),
    
    /**
     * Production service tier
     */
    PRODUCTION("production"),
    
    /**
     * Custom service tier
     */
    CUSTOM("custom");
    
    private final String value;
    
    ServiceTier(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ServiceTier fromValue(String value) {
        for (ServiceTier tier : values()) {
            if (tier.value.equalsIgnoreCase(value)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Unknown ServiceTier: " + value);
    }
    
    /**
     * Check if this tier is a paid tier
     */
    public boolean isPaid() {
        return this != FREE && this != TRIAL;
    }
    
    /**
     * Check if this tier is a premium tier
     */
    public boolean isPremium() {
        return this == PREMIUM || this == ENTERPRISE || this == ULTIMATE || 
               this == BUSINESS || this == PROFESSIONAL;
    }
    
    /**
     * Check if this tier is a basic tier
     */
    public boolean isBasic() {
        return this == BASIC || this == STARTER || this == FREE || this == TRIAL;
    }
    
    /**
     * Check if this tier is enterprise-grade
     */
    public boolean isEnterprise() {
        return this == ENTERPRISE || this == ULTIMATE || this == DEDICATED;
    }
    
    /**
     * Check if this tier supports multi-tenancy
     */
    public boolean supportsMultiTenancy() {
        return this == MULTI_TENANT || this == SHARED || this == BASIC || 
               this == STANDARD || this == PREMIUM || this == ENTERPRISE;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
