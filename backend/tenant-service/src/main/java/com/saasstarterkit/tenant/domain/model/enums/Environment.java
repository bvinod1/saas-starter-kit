package com.saasstarterkit.tenant.domain.model.enums;

/**
 * Environment - Defines the environment for configuration values
 * 
 * This enum represents the different environments where configuration
 * values can be applied in the tenant configuration system.
 */
public enum Environment {
    
    /**
     * Development environment
     */
    DEVELOPMENT("development"),
    
    /**
     * Testing environment
     */
    TESTING("testing"),
    
    /**
     * Staging environment
     */
    STAGING("staging"),
    
    /**
     * Production environment
     */
    PRODUCTION("production"),
    
    /**
     * UAT (User Acceptance Testing) environment
     */
    UAT("uat"),
    
    /**
     * QA (Quality Assurance) environment
     */
    QA("qa"),
    
    /**
     * Pre-production environment
     */
    PRE_PROD("pre_prod"),
    
    /**
     * Sandbox environment
     */
    SANDBOX("sandbox"),
    
    /**
     * Demo environment
     */
    DEMO("demo"),
    
    /**
     * Training environment
     */
    TRAINING("training"),
    
    /**
     * Disaster recovery environment
     */
    DR("dr"),
    
    /**
     * Hot standby environment
     */
    HOT_STANDBY("hot_standby"),
    
    /**
     * Warm standby environment
     */
    WARM_STANDBY("warm_standby"),
    
    /**
     * Cold standby environment
     */
    COLD_STANDBY("cold_standby"),
    
    /**
     * Load testing environment
     */
    LOAD_TEST("load_test"),
    
    /**
     * Performance testing environment
     */
    PERF_TEST("perf_test"),
    
    /**
     * Security testing environment
     */
    SECURITY_TEST("security_test"),
    
    /**
     * Integration testing environment
     */
    INTEGRATION_TEST("integration_test"),
    
    /**
     * Unit testing environment
     */
    UNIT_TEST("unit_test"),
    
    /**
     * End-to-end testing environment
     */
    E2E_TEST("e2e_test"),
    
    /**
     * Custom environment
     */
    CUSTOM("custom");
    
    private final String value;
    
    Environment(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static Environment fromValue(String value) {
        for (Environment env : values()) {
            if (env.value.equalsIgnoreCase(value)) {
                return env;
            }
        }
        throw new IllegalArgumentException("Unknown Environment: " + value);
    }
    
    /**
     * Check if this environment is a production-like environment
     */
    public boolean isProductionLike() {
        return this == PRODUCTION || this == PRE_PROD || this == HOT_STANDBY;
    }
    
    /**
     * Check if this environment is a testing environment
     */
    public boolean isTesting() {
        return this == TESTING || this == UAT || this == QA || 
               this == LOAD_TEST || this == PERF_TEST || this == SECURITY_TEST ||
               this == INTEGRATION_TEST || this == UNIT_TEST || this == E2E_TEST;
    }
    
    /**
     * Check if this environment is a development environment
     */
    public boolean isDevelopment() {
        return this == DEVELOPMENT || this == SANDBOX || this == DEMO || this == TRAINING;
    }
    
    /**
     * Check if this environment is a standby environment
     */
    public boolean isStandby() {
        return this == HOT_STANDBY || this == WARM_STANDBY || this == COLD_STANDBY;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
