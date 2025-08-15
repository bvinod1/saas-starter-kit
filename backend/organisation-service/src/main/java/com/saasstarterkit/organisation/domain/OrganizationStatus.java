package com.saasstarterkit.organisation.domain;

public enum OrganizationStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    PENDING_ACTIVATION("Pending Activation"),
    ONBOARDING("Onboarding"),
    TRIAL_EXPIRED("Trial Expired"),
    SUBSCRIPTION_EXPIRED("Subscription Expired"),
    PAYMENT_FAILED("Payment Failed"),
    COMPLIANCE_VIOLATION("Compliance Violation"),
    TERMINATED("Terminated"),
    ARCHIVED("Archived");

    private final String displayName;

    OrganizationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVE || this == ONBOARDING;
    }

    public boolean isSuspended() {
        return this == SUSPENDED || this == PAYMENT_FAILED || this == COMPLIANCE_VIOLATION;
    }

    public boolean isTerminated() {
        return this == TERMINATED || this == ARCHIVED;
    }

    public boolean canAccessServices() {
        return this == ACTIVE || this == ONBOARDING;
    }

    public boolean requiresAttention() {
        return this == PAYMENT_FAILED || this == COMPLIANCE_VIOLATION || this == TRIAL_EXPIRED;
    }
}
