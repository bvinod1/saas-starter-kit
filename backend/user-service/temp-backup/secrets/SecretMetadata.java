package com.saasstarterkit.user.service.secrets;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Metadata about a secret stored in the secret manager.
 */
public class SecretMetadata {
    
    private final String secretKey;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastModified;
    private final String createdBy;
    private final String lastModifiedBy;
    private final Map<String, String> tags;
    private final String description;
    private final SecretType secretType;
    private final boolean isEncrypted;
    private final String encryptionAlgorithm;
    private final LocalDateTime expirationDate;
    private final boolean isExpired;
    
    public enum SecretType {
        PASSWORD,
        API_KEY,
        TOKEN,
        CERTIFICATE,
        PRIVATE_KEY,
        DATABASE_CREDENTIALS,
        ENCRYPTION_KEY,
        OTHER
    }
    
    private SecretMetadata(Builder builder) {
        this.secretKey = builder.secretKey;
        this.createdAt = builder.createdAt;
        this.lastModified = builder.lastModified;
        this.createdBy = builder.createdBy;
        this.lastModifiedBy = builder.lastModifiedBy;
        this.tags = builder.tags;
        this.description = builder.description;
        this.secretType = builder.secretType;
        this.isEncrypted = builder.isEncrypted;
        this.encryptionAlgorithm = builder.encryptionAlgorithm;
        this.expirationDate = builder.expirationDate;
        this.isExpired = builder.isExpired;
    }
    
    // Getters
    public String getSecretKey() { return secretKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    public String getCreatedBy() { return createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public Map<String, String> getTags() { return tags; }
    public String getDescription() { return description; }
    public SecretType getSecretType() { return secretType; }
    public boolean isEncrypted() { return isEncrypted; }
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public boolean isExpired() { return isExpired; }
    
    // Builder pattern
    public static class Builder {
        private String secretKey;
        private LocalDateTime createdAt;
        private LocalDateTime lastModified;
        private String createdBy;
        private String lastModifiedBy;
        private Map<String, String> tags;
        private String description;
        private SecretType secretType = SecretType.OTHER;
        private boolean isEncrypted = false;
        private String encryptionAlgorithm;
        private LocalDateTime expirationDate;
        private boolean isExpired = false;
        
        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder lastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }
        
        public Builder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder secretType(SecretType secretType) {
            this.secretType = secretType;
            return this;
        }
        
        public Builder isEncrypted(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
            return this;
        }
        
        public Builder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }
        
        public Builder expirationDate(LocalDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }
        
        public Builder isExpired(boolean isExpired) {
            this.isExpired = isExpired;
            return this;
        }
        
        public SecretMetadata build() {
            return new SecretMetadata(this);
        }
    }
    
    @Override
    public String toString() {
        return "SecretMetadata{" +
                "secretKey='" + secretKey + '\'' +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                ", createdBy='" + createdBy + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", description='" + description + '\'' +
                ", secretType=" + secretType +
                ", isEncrypted=" + isEncrypted +
                ", isExpired=" + isExpired +
                '}';
    }
}
