package com.saasstarterkit.organisation.domain;

public enum SubscriptionStatus {
    TRIAL("Trial", "Free trial period"),
    ACTIVE("Active", "Active paid subscription"),
    PAST_DUE("Past Due", "Payment overdue"),
    CANCELED("Canceled", "Subscription canceled"),
    UNPAID("Unpaid", "Payment failed"),
    PENDING("Pending", "Awaiting activation"),
    SUSPENDED("Suspended", "Temporarily suspended"),
    EXPIRED("Expired", "Subscription expired"),
    GRACE_PERIOD("Grace Period", "In grace period after expiration");

    private final String displayName;
    private final String description;

    SubscriptionStatus(String displayName, String description) {
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
        return this == ACTIVE || this == TRIAL || this == GRACE_PERIOD;
    }

    public boolean isTrial() {
        return this == TRIAL;
    }

    public boolean isPaid() {
        return this == ACTIVE;
    }

    public boolean requiresPayment() {
        return this == PAST_DUE || this == UNPAID || this == EXPIRED;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean canAccessServices() {
        return isActive() && !isSuspended();
    }

    public boolean needsAttention() {
        return this == PAST_DUE || this == UNPAID || this == EXPIRED;
    }

    public static SubscriptionStatus fromName(String name) {
        for (SubscriptionStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(name) || status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return TRIAL; // Default fallback
    }
}
