package com.saasstarterkit.organisation.domain;

public enum OrganizationTier {
    STARTER("Starter", 5, 10, 100, "Basic features for small teams"),
    PROFESSIONAL("Professional", 25, 100, 1000, "Advanced features for growing businesses"),
    ENTERPRISE("Enterprise", 1000, 1000, 10000, "Full features for large organizations"),
    CUSTOM("Custom", -1, -1, -1, "Custom configuration for special requirements");

    private final String displayName;
    private final int maxUsers;
    private final int storageLimitGB;
    private final int apiRateLimit;
    private final String description;

    OrganizationTier(String displayName, int maxUsers, int storageLimitGB, int apiRateLimit, String description) {
        this.displayName = displayName;
        this.maxUsers = maxUsers;
        this.storageLimitGB = storageLimitGB;
        this.apiRateLimit = apiRateLimit;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public int getStorageLimitGB() {
        return storageLimitGB;
    }

    public int getApiRateLimit() {
        return apiRateLimit;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlimitedUsers() {
        return maxUsers == -1;
    }

    public boolean isUnlimitedStorage() {
        return storageLimitGB == -1;
    }

    public boolean isUnlimitedApiRate() {
        return apiRateLimit == -1;
    }

    public boolean canSupportUsers(int userCount) {
        return isUnlimitedUsers() || userCount <= maxUsers;
    }

    public boolean canSupportStorage(int storageGB) {
        return isUnlimitedStorage() || storageGB <= storageLimitGB;
    }

    public boolean canSupportApiRate(int rate) {
        return isUnlimitedApiRate() || rate <= apiRateLimit;
    }

    public static OrganizationTier fromName(String name) {
        for (OrganizationTier tier : values()) {
            if (tier.displayName.equalsIgnoreCase(name) || tier.name().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return STARTER; // Default fallback
    }

    public OrganizationTier upgrade() {
        switch (this) {
            case STARTER:
                return PROFESSIONAL;
            case PROFESSIONAL:
                return ENTERPRISE;
            case ENTERPRISE:
            case CUSTOM:
                return this; // Already at highest tier
            default:
                return STARTER;
        }
    }

    public OrganizationTier downgrade() {
        switch (this) {
            case ENTERPRISE:
                return PROFESSIONAL;
            case PROFESSIONAL:
                return STARTER;
            case STARTER:
            case CUSTOM:
                return this; // Already at lowest tier
            default:
                return STARTER;
        }
    }
}
