package com.saasstarterkit.user.exception;

/**
 * Exception thrown when compliance-related operations fail.
 */
public class ComplianceException extends RuntimeException {
    
    private final String complianceType;
    private final String tenantId;
    
    public ComplianceException(String message) {
        super(message);
        this.complianceType = "GENERAL";
        this.tenantId = null;
    }
    
    public ComplianceException(String message, String complianceType) {
        super(message);
        this.complianceType = complianceType;
        this.tenantId = null;
    }
    
    public ComplianceException(String message, String complianceType, String tenantId) {
        super(message);
        this.complianceType = complianceType;
        this.tenantId = tenantId;
    }
    
    public ComplianceException(String message, Throwable cause) {
        super(message, cause);
        this.complianceType = "GENERAL";
        this.tenantId = null;
    }
    
    public ComplianceException(String message, String complianceType, String tenantId, Throwable cause) {
        super(message, cause);
        this.complianceType = complianceType;
        this.tenantId = tenantId;
    }
    
    public String getComplianceType() {
        return complianceType;
    }
    
    public String getTenantId() {
        return tenantId;
    }
}
