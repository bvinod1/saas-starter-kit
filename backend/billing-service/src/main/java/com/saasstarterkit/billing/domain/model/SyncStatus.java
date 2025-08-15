package com.saasstarterkit.billing.domain.model;

/**
 * Represents the synchronization status with external subscription providers
 */
public enum SyncStatus {
    /**
     * Pending synchronization
     */
    PENDING,
    
    /**
     * Successfully synchronized
     */
    SYNCED,
    
    /**
     * Synchronization failed
     */
    FAILED,
    
    /**
     * Synchronization in progress
     */
    IN_PROGRESS,
    
    /**
     * Synchronization retry scheduled
     */
    RETRY_SCHEDULED,
    
    /**
     * Manual intervention required
     */
    MANUAL_INTERVENTION_REQUIRED
}
