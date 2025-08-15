package com.saasstarterkit.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity to manage data subject rights requests (GDPR Articles 15-22)
 */
@Entity
@Table(name = "data_subject_requests", indexes = {
    @Index(name = "idx_request_user_id", columnList = "user_id"),
    @Index(name = "idx_request_type", columnList = "request_type"),
    @Index(name = "idx_request_status", columnList = "status"),
    @Index(name = "idx_request_created_at", columnList = "created_at")
})
public class DataSubjectRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private DataSubjectRequestType requestType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DataSubjectRequestStatus status;
    
    @Column(name = "request_description", nullable = false)
    private String requestDescription;
    
    @Column(name = "verification_method", nullable = false)
    private String verificationMethod; // email, phone, id_document, etc.
    
    @Column(name = "verification_completed")
    private Boolean verificationCompleted = false;
    
    @Column(name = "verification_completed_at")
    private LocalDateTime verificationCompletedAt;
    
    @Column(name = "requested_data_scope", columnDefinition = "jsonb")
    private Map<String, Object> requestedDataScope;
    
    @Column(name = "processing_reason")
    private String processingReason;
    
    @Column(name = "legal_basis")
    private String legalBasis;
    
    @Column(name = "data_retention_period")
    private String dataRetentionPeriod;
    
    @Column(name = "third_party_recipients", columnDefinition = "jsonb")
    private Map<String, Object> thirdPartyRecipients;
    
    @Column(name = "automated_decision_making")
    private Boolean automatedDecisionMaking = false;
    
    @Column(name = "data_portability_format")
    private String dataPortabilityFormat; // JSON, CSV, XML, etc.
    
    @Column(name = "estimated_completion_date")
    private LocalDateTime estimatedCompletionDate;
    
    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;
    
    @Column(name = "response_data", columnDefinition = "jsonb")
    private Map<String, Object> responseData;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "appeal_deadline")
    private LocalDateTime appealDeadline;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DataSubjectRequestStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public DataSubjectRequest() {}
    
    public DataSubjectRequest(UUID userId, String tenantId, DataSubjectRequestType requestType, String requestDescription) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.requestType = requestType;
        this.requestDescription = requestDescription;
        this.status = DataSubjectRequestStatus.PENDING;
        this.verificationMethod = "email";
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public DataSubjectRequestType getRequestType() { return requestType; }
    public void setRequestType(DataSubjectRequestType requestType) { this.requestType = requestType; }
    
    public DataSubjectRequestStatus getStatus() { return status; }
    public void setStatus(DataSubjectRequestStatus status) { this.status = status; }
    
    public String getRequestDescription() { return requestDescription; }
    public void setRequestDescription(String requestDescription) { this.requestDescription = requestDescription; }
    
    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }
    
    public Boolean getVerificationCompleted() { return verificationCompleted; }
    public void setVerificationCompleted(Boolean verificationCompleted) { this.verificationCompleted = verificationCompleted; }
    
    public LocalDateTime getVerificationCompletedAt() { return verificationCompletedAt; }
    public void setVerificationCompletedAt(LocalDateTime verificationCompletedAt) { this.verificationCompletedAt = verificationCompletedAt; }
    
    public Map<String, Object> getRequestedDataScope() { return requestedDataScope; }
    public void setRequestedDataScope(Map<String, Object> requestedDataScope) { this.requestedDataScope = requestedDataScope; }
    
    public String getProcessingReason() { return processingReason; }
    public void setProcessingReason(String processingReason) { this.processingReason = processingReason; }
    
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    
    public String getDataRetentionPeriod() { return dataRetentionPeriod; }
    public void setDataRetentionPeriod(String dataRetentionPeriod) { this.dataRetentionPeriod = dataRetentionPeriod; }
    
    public Map<String, Object> getThirdPartyRecipients() { return thirdPartyRecipients; }
    public void setThirdPartyRecipients(Map<String, Object> thirdPartyRecipients) { this.thirdPartyRecipients = thirdPartyRecipients; }
    
    public Boolean getAutomatedDecisionMaking() { return automatedDecisionMaking; }
    public void setAutomatedDecisionMaking(Boolean automatedDecisionMaking) { this.automatedDecisionMaking = automatedDecisionMaking; }
    
    public String getDataPortabilityFormat() { return dataPortabilityFormat; }
    public void setDataPortabilityFormat(String dataPortabilityFormat) { this.dataPortabilityFormat = dataPortabilityFormat; }
    
    public LocalDateTime getEstimatedCompletionDate() { return estimatedCompletionDate; }
    public void setEstimatedCompletionDate(LocalDateTime estimatedCompletionDate) { this.estimatedCompletionDate = estimatedCompletionDate; }
    
    public LocalDateTime getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(LocalDateTime actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    
    public Map<String, Object> getResponseData() { return responseData; }
    public void setResponseData(Map<String, Object> responseData) { this.responseData = responseData; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public LocalDateTime getAppealDeadline() { return appealDeadline; }
    public void setAppealDeadline(LocalDateTime appealDeadline) { this.appealDeadline = appealDeadline; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business Logic Methods
    public boolean isPending() {
        return DataSubjectRequestStatus.PENDING.equals(status);
    }
    
    public boolean isInProgress() {
        return DataSubjectRequestStatus.IN_PROGRESS.equals(status);
    }
    
    public boolean isCompleted() {
        return DataSubjectRequestStatus.COMPLETED.equals(status);
    }
    
    public boolean isRejected() {
        return DataSubjectRequestStatus.REJECTED.equals(status);
    }
    
    public boolean requiresVerification() {
        return !Boolean.TRUE.equals(verificationCompleted);
    }
    
    public boolean isOverdue() {
        if (estimatedCompletionDate == null) return false;
        return LocalDateTime.now().isAfter(estimatedCompletionDate);
    }
    
    public void markVerificationCompleted() {
        this.verificationCompleted = true;
        this.verificationCompletedAt = LocalDateTime.now();
    }
    
    public void markInProgress() {
        this.status = DataSubjectRequestStatus.IN_PROGRESS;
    }
    
    public void markCompleted() {
        this.status = DataSubjectRequestStatus.COMPLETED;
        this.actualCompletionDate = LocalDateTime.now();
    }
    
    public void reject(String reason) {
        this.status = DataSubjectRequestStatus.REJECTED;
        this.rejectionReason = reason;
        this.appealDeadline = LocalDateTime.now().plusDays(30); // 30 days to appeal
    }
}
