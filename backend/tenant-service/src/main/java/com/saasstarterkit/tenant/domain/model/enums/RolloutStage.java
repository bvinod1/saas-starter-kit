package com.saasstarterkit.tenant.domain.model.enums;

/**
 * RolloutStage - Defines the stage of configuration rollout
 * 
 * This enum represents the different stages of configuration rollout
 * in the tenant configuration system.
 */
public enum RolloutStage {
    
    /**
     * Draft stage - configuration is being prepared
     */
    DRAFT("draft"),
    
    /**
     * Review stage - configuration is under review
     */
    REVIEW("review"),
    
    /**
     * Approved stage - configuration has been approved
     */
    APPROVED("approved"),
    
    /**
     * Staged stage - configuration is staged for deployment
     */
    STAGED("staged"),
    
    /**
     * Testing stage - configuration is being tested
     */
    TESTING("testing"),
    
    /**
     * Canary stage - configuration is deployed to a small subset
     */
    CANARY("canary"),
    
    /**
     * Beta stage - configuration is deployed to beta users
     */
    BETA("beta"),
    
    /**
     * Production stage - configuration is deployed to production
     */
    PRODUCTION("production"),
    
    /**
     * Rollback stage - configuration is being rolled back
     */
    ROLLBACK("rollback"),
    
    /**
     * Deprecated stage - configuration is deprecated
     */
    DEPRECATED("deprecated"),
    
    /**
     * Archived stage - configuration is archived
     */
    ARCHIVED("archived"),
    
    /**
     * Development stage - configuration is in development
     */
    DEVELOPMENT("development"),
    
    /**
     * Integration stage - configuration is being integrated
     */
    INTEGRATION("integration"),
    
    /**
     * Validation stage - configuration is being validated
     */
    VALIDATION("validation"),
    
    /**
     * Deployment stage - configuration is being deployed
     */
    DEPLOYMENT("deployment"),
    
    /**
     * Monitoring stage - configuration is being monitored
     */
    MONITORING("monitoring"),
    
    /**
     * Maintenance stage - configuration is under maintenance
     */
    MAINTENANCE("maintenance"),
    
    /**
     * Emergency stage - configuration is in emergency mode
     */
    EMERGENCY("emergency"),
    
    /**
     * Custom stage - custom rollout stage
     */
    CUSTOM("custom");
    
    private final String value;
    
    RolloutStage(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static RolloutStage fromValue(String value) {
        for (RolloutStage stage : values()) {
            if (stage.value.equalsIgnoreCase(value)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("Unknown RolloutStage: " + value);
    }
    
    /**
     * Check if this stage is a pre-production stage
     */
    public boolean isPreProduction() {
        return this == DRAFT || this == REVIEW || this == APPROVED || 
               this == STAGED || this == TESTING || this == CANARY || 
               this == BETA || this == DEVELOPMENT || this == INTEGRATION ||
               this == VALIDATION;
    }
    
    /**
     * Check if this stage is a production stage
     */
    public boolean isProduction() {
        return this == PRODUCTION || this == MONITORING || this == MAINTENANCE;
    }
    
    /**
     * Check if this stage is a post-production stage
     */
    public boolean isPostProduction() {
        return this == ROLLBACK || this == DEPRECATED || this == ARCHIVED;
    }
    
    /**
     * Check if this stage allows modifications
     */
    public boolean allowsModifications() {
        return this == DRAFT || this == REVIEW || this == DEVELOPMENT || 
               this == INTEGRATION || this == VALIDATION;
    }
    
    /**
     * Check if this stage requires approval
     */
    public boolean requiresApproval() {
        return this == REVIEW || this == APPROVED || this == STAGED;
    }
    
    /**
     * Check if this stage is active
     */
    public boolean isActive() {
        return this == PRODUCTION || this == MONITORING || this == MAINTENANCE ||
               this == CANARY || this == BETA;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
