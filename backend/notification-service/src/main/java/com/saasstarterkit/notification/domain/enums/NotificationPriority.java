package com.saasstarterkit.notification.domain.enums;

/**
 * NotificationPriority - Defines the priority levels for notifications
 * 
 * This enum represents the different priority levels that can be assigned
 * to notifications to determine their urgency and delivery order.
 */
public enum NotificationPriority {
    
    /**
     * Low priority - non-urgent notifications
     */
    LOW("low"),
    
    /**
     * Normal priority - standard notifications
     */
    NORMAL("normal"),
    
    /**
     * High priority - important notifications
     */
    HIGH("high"),
    
    /**
     * Urgent priority - critical notifications
     */
    URGENT("urgent"),
    
    /**
     * Critical priority - emergency notifications
     */
    CRITICAL("critical"),
    
    /**
     * Info priority - informational notifications
     */
    INFO("info"),
    
    /**
     * Warning priority - warning notifications
     */
    WARNING("warning"),
    
    /**
     * Error priority - error notifications
     */
    ERROR("error"),
    
    /**
     * Success priority - success notifications
     */
    SUCCESS("success"),
    
    /**
     * Debug priority - debug notifications
     */
    DEBUG("debug"),
    
    /**
     * Trace priority - trace notifications
     */
    TRACE("trace"),
    
    /**
     * Fatal priority - fatal error notifications
     */
    FATAL("fatal"),
    
    /**
     * Custom priority - custom priority level
     */
    CUSTOM("custom");
    
    private final String value;
    
    NotificationPriority(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NotificationPriority fromValue(String value) {
        for (NotificationPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationPriority: " + value);
    }
    
    /**
     * Get the numeric priority level (higher = more urgent)
     */
    public int getNumericLevel() {
        switch (this) {
            case TRACE: return 1;
            case DEBUG: return 2;
            case INFO: return 3;
            case LOW: return 4;
            case NORMAL: return 5;
            case SUCCESS: return 6;
            case WARNING: return 7;
            case HIGH: return 8;
            case ERROR: return 9;
            case URGENT: return 10;
            case CRITICAL: return 11;
            case FATAL: return 12;
            case CUSTOM: return 13;
            default: return 5;
        }
    }
    
    /**
     * Check if this priority is urgent
     */
    public boolean isUrgent() {
        return this == URGENT || this == CRITICAL || this == FATAL;
    }
    
    /**
     * Check if this priority is high
     */
    public boolean isHigh() {
        return this == HIGH || this == URGENT || this == CRITICAL || this == FATAL;
    }
    
    /**
     * Check if this priority is low
     */
    public boolean isLow() {
        return this == LOW || this == INFO || this == DEBUG || this == TRACE;
    }
    
    /**
     * Check if this priority requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == CRITICAL || this == FATAL;
    }
    
    /**
     * Check if this priority can be delayed
     */
    public boolean canBeDelayed() {
        return this == LOW || this == INFO || this == DEBUG || this == TRACE;
    }
    
    /**
     * Check if this priority should bypass rate limiting
     */
    public boolean bypassesRateLimiting() {
        return this == CRITICAL || this == FATAL;
    }
    
    /**
     * Check if this priority should trigger alerts
     */
    public boolean triggersAlerts() {
        return this == ERROR || this == URGENT || this == CRITICAL || this == FATAL;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
