package com.saasstarterkit.organisation.domain;

public enum AuditAction {
    // Organization Management
    ORGANIZATION_CREATED("Organization Created", "Organization was created"),
    ORGANIZATION_UPDATED("Organization Updated", "Organization details were modified"),
    ORGANIZATION_DELETED("Organization Deleted", "Organization was deleted"),
    ORGANIZATION_SUSPENDED("Organization Suspended", "Organization was suspended"),
    ORGANIZATION_ACTIVATED("Organization Activated", "Organization was activated"),
    ORGANIZATION_ARCHIVED("Organization Archived", "Organization was archived"),

    // Member Management
    MEMBER_ADDED("Member Added", "New member was added to organization"),
    MEMBER_REMOVED("Member Removed", "Member was removed from organization"),
    MEMBER_INVITED("Member Invited", "Member was invited to organization"),
    MEMBER_ACCEPTED("Member Accepted", "Member accepted invitation"),
    MEMBER_DECLINED("Member Declined", "Member declined invitation"),
    MEMBER_ROLE_CHANGED("Member Role Changed", "Member role was modified"),
    MEMBER_STATUS_CHANGED("Member Status Changed", "Member status was modified"),
    MEMBER_PERMISSIONS_CHANGED("Member Permissions Changed", "Member permissions were modified"),

    // Subscription & Billing
    SUBSCRIPTION_CREATED("Subscription Created", "New subscription was created"),
    SUBSCRIPTION_UPDATED("Subscription Updated", "Subscription was modified"),
    SUBSCRIPTION_CANCELED("Subscription Canceled", "Subscription was canceled"),
    SUBSCRIPTION_RENEWED("Subscription Renewed", "Subscription was renewed"),
    SUBSCRIPTION_UPGRADED("Subscription Upgraded", "Subscription was upgraded"),
    SUBSCRIPTION_DOWNGRADED("Subscription Downgraded", "Subscription was downgraded"),
    PAYMENT_PROCESSED("Payment Processed", "Payment was processed"),
    PAYMENT_FAILED("Payment Failed", "Payment processing failed"),
    INVOICE_GENERATED("Invoice Generated", "Invoice was generated"),
    INVOICE_PAID("Invoice Paid", "Invoice was paid"),

    // Security & Access
    LOGIN_SUCCESS("Login Success", "User successfully logged in"),
    LOGIN_FAILED("Login Failed", "User login attempt failed"),
    LOGOUT("Logout", "User logged out"),
    PASSWORD_CHANGED("Password Changed", "User password was changed"),
    PASSWORD_RESET("Password Reset", "User password was reset"),
    TWO_FACTOR_ENABLED("Two-Factor Enabled", "Two-factor authentication was enabled"),
    TWO_FACTOR_DISABLED("Two-Factor Disabled", "Two-factor authentication was disabled"),
    ACCESS_DENIED("Access Denied", "Access to resource was denied"),
    PERMISSION_CHANGED("Permission Changed", "User permissions were modified"),
    ROLE_CHANGED("Role Changed", "User role was modified"),

    // Data & Compliance
    DATA_EXPORTED("Data Exported", "Data was exported"),
    DATA_IMPORTED("Data Imported", "Data was imported"),
    DATA_DELETED("Data Deleted", "Data was deleted"),
    DATA_ANONYMIZED("Data Anonymized", "Data was anonymized"),
    COMPLIANCE_CHECK("Compliance Check", "Compliance check was performed"),
    AUDIT_REPORT("Audit Report", "Audit report was generated"),
    DATA_RETENTION("Data Retention", "Data retention policy was applied"),

    // System & Configuration
    SETTINGS_CHANGED("Settings Changed", "System settings were modified"),
    CONFIGURATION_UPDATED("Configuration Updated", "System configuration was updated"),
    FEATURE_TOGGLED("Feature Toggled", "Feature was enabled/disabled"),
    INTEGRATION_ADDED("Integration Added", "New integration was added"),
    INTEGRATION_REMOVED("Integration Removed", "Integration was removed"),
    API_KEY_GENERATED("API Key Generated", "New API key was generated"),
    API_KEY_REVOKED("API Key Revoked", "API key was revoked"),

    // Monitoring & Alerts
    ALERT_TRIGGERED("Alert Triggered", "System alert was triggered"),
    ALERT_ACKNOWLEDGED("Alert Acknowledged", "Alert was acknowledged"),
    METRIC_RECORDED("Metric Recorded", "System metric was recorded"),
    PERFORMANCE_ISSUE("Performance Issue", "Performance issue was detected"),
    SECURITY_THREAT("Security Threat", "Security threat was detected"),

    // Custom Actions
    CUSTOM_ACTION("Custom Action", "Custom action was performed");

    private final String displayName;
    private final String description;

    AuditAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSecurityEvent() {
        return this == LOGIN_FAILED || this == ACCESS_DENIED || this == PERMISSION_CHANGED ||
               this == ROLE_CHANGED || this == PASSWORD_CHANGED || this == PASSWORD_RESET ||
               this == TWO_FACTOR_ENABLED || this == TWO_FACTOR_DISABLED || this == API_KEY_GENERATED ||
               this == API_KEY_REVOKED || this == SECURITY_THREAT;
    }

    public boolean isComplianceEvent() {
        return this == DATA_EXPORTED || this == DATA_IMPORTED || this == DATA_DELETED ||
               this == DATA_ANONYMIZED || this == COMPLIANCE_CHECK || this == AUDIT_REPORT ||
               this == DATA_RETENTION;
    }

    public boolean isFinancialEvent() {
        return this == SUBSCRIPTION_CREATED || this == SUBSCRIPTION_UPDATED || this == SUBSCRIPTION_CANCELED ||
               this == SUBSCRIPTION_RENEWED || this == SUBSCRIPTION_UPGRADED || this == SUBSCRIPTION_DOWNGRADED ||
               this == PAYMENT_PROCESSED || this == PAYMENT_FAILED || this == INVOICE_GENERATED ||
               this == INVOICE_PAID;
    }

    public boolean isHighRisk() {
        return this == ORGANIZATION_DELETED || this == MEMBER_REMOVED || this == ACCESS_DENIED ||
               this == SECURITY_THREAT || this == DATA_DELETED || this == API_KEY_REVOKED;
    }

    public static AuditAction fromName(String name) {
        for (AuditAction action : values()) {
            if (action.displayName.equalsIgnoreCase(name) || action.name().equalsIgnoreCase(name)) {
                return action;
            }
        }
        return CUSTOM_ACTION; // Default fallback
    }
}
