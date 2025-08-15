package com.saasstarterkit.organisation.domain;

public enum RiskLevel {
    CRITICAL("Critical", "Immediate action required", 100),
    HIGH("High", "High priority action required", 80),
    MEDIUM("Medium", "Action required within reasonable time", 60),
    LOW("Low", "Monitor and review", 40),
    INFO("Info", "Informational only", 20);

    private final String displayName;
    private final String description;
    private final int numericValue;

    RiskLevel(String displayName, String description, int numericValue) {
        this.displayName = displayName;
        this.description = description;
        this.numericValue = numericValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getNumericValue() {
        return numericValue;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isHigh() {
        return this == HIGH || this == CRITICAL;
    }

    public boolean isMedium() {
        return this == MEDIUM || isHigh();
    }

    public boolean isLow() {
        return this == LOW || isMedium();
    }

    public boolean isInfo() {
        return this == INFO || isLow();
    }

    public boolean requiresImmediateAction() {
        return this == CRITICAL || this == HIGH;
    }

    public boolean requiresAction() {
        return this == CRITICAL || this == HIGH || this == MEDIUM;
    }

    public boolean isMonitorOnly() {
        return this == LOW || this == INFO;
    }

    public static RiskLevel fromNumericValue(int value) {
        for (RiskLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        return INFO; // Default fallback
    }

    public static RiskLevel fromName(String name) {
        for (RiskLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(name) || level.name().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return INFO; // Default fallback
    }

    public RiskLevel escalate() {
        switch (this) {
            case INFO:
                return LOW;
            case LOW:
                return MEDIUM;
            case MEDIUM:
                return HIGH;
            case HIGH:
                return CRITICAL;
            case CRITICAL:
                return this; // Already at highest level
            default:
                return INFO;
        }
    }

    public RiskLevel deescalate() {
        switch (this) {
            case CRITICAL:
                return HIGH;
            case HIGH:
                return MEDIUM;
            case MEDIUM:
                return LOW;
            case LOW:
                return INFO;
            case INFO:
                return this; // Already at lowest level
            default:
                return INFO;
        }
    }
}
