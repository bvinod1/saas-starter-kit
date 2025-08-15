package com.saasstarterkit.organisation.domain;

public enum MemberRole {
    OWNER("Owner", "Full control over the organization", 100),
    ADMIN("Admin", "Administrative access to organization settings", 80),
    MANAGER("Manager", "Manage team members and projects", 60),
    MEMBER("Member", "Standard member access", 40),
    GUEST("Guest", "Limited read-only access", 20),
    VIEWER("Viewer", "Read-only access to specific resources", 10);

    private final String displayName;
    private final String description;
    private final int permissionLevel;

    MemberRole(String displayName, String description, int permissionLevel) {
        this.displayName = displayName;
        this.description = description;
        this.permissionLevel = permissionLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public boolean isOwner() {
        return this == OWNER;
    }

    public boolean isAdmin() {
        return this == ADMIN || this == OWNER;
    }

    public boolean isManager() {
        return this == MANAGER || isAdmin();
    }

    public boolean isMember() {
        return this == MEMBER || isManager();
    }

    public boolean isGuest() {
        return this == GUEST || isMember();
    }

    public boolean isViewer() {
        return this == VIEWER || isGuest();
    }

    public boolean canManageUsers() {
        return isManager();
    }

    public boolean canManageOrganization() {
        return isAdmin();
    }

    public boolean canDeleteOrganization() {
        return isOwner();
    }

    public boolean canInviteUsers() {
        return isManager();
    }

    public boolean canViewBilling() {
        return isAdmin();
    }

    public boolean canManageBilling() {
        return isOwner();
    }

    public boolean canViewAnalytics() {
        return isMember();
    }

    public boolean canManageSettings() {
        return isAdmin();
    }

    public static MemberRole fromName(String name) {
        for (MemberRole role : values()) {
            if (role.displayName.equalsIgnoreCase(name) || role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return MEMBER; // Default fallback
    }

    public MemberRole upgrade() {
        switch (this) {
            case VIEWER:
                return GUEST;
            case GUEST:
                return MEMBER;
            case MEMBER:
                return MANAGER;
            case MANAGER:
                return ADMIN;
            case ADMIN:
                return OWNER;
            case OWNER:
                return this; // Already at highest level
            default:
                return MEMBER;
        }
    }

    public MemberRole downgrade() {
        switch (this) {
            case OWNER:
                return ADMIN;
            case ADMIN:
                return MANAGER;
            case MANAGER:
                return MEMBER;
            case MEMBER:
                return GUEST;
            case GUEST:
                return VIEWER;
            case VIEWER:
                return this; // Already at lowest level
            default:
                return MEMBER;
        }
    }
}
