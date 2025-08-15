package com.saasstarterkit.organisation.domain;

public enum MemberStatus {
    ACTIVE("Active", "Active member with full access"),
    INVITED("Invited", "Invited but not yet accepted"),
    PENDING("Pending", "Pending approval"),
    SUSPENDED("Suspended", "Temporarily suspended"),
    INACTIVE("Inactive", "Inactive member"),
    REMOVED("Removed", "Removed from organization"),
    EXPIRED("Expired", "Invitation expired");

    private final String displayName;
    private final String description;

    MemberStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInvited() {
        return this == INVITED;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean isRemoved() {
        return this == REMOVED;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }

    public boolean canAccessOrganization() {
        return this == ACTIVE;
    }

    public boolean needsAction() {
        return this == INVITED || this == PENDING;
    }

    public boolean isTerminal() {
        return this == REMOVED || this == EXPIRED;
    }

    public static MemberStatus fromName(String name) {
        for (MemberStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(name) || status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return INVITED; // Default fallback
    }
}
