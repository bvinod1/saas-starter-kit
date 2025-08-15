-- Migration: GDPR and HIPAA Compliance Implementation
-- This script adds comprehensive GDPR and HIPAA compliance features to the user management system

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- GDPR COMPLIANCE TABLES
-- =============================================

-- Data Consent Management
CREATE TABLE IF NOT EXISTS data_consents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    consent_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    consent_text TEXT NOT NULL,
    consent_version VARCHAR(20) NOT NULL DEFAULT '1.0',
    ip_address INET,
    user_agent TEXT,
    consent_given_at TIMESTAMP,
    consent_withdrawn_at TIMESTAMP,
    consent_expires_at TIMESTAMP,
    legal_basis VARCHAR(100) NOT NULL DEFAULT 'consent',
    purpose VARCHAR(255) NOT NULL DEFAULT 'Service provision',
    data_categories JSONB,
    third_party_sharing BOOLEAN,
    retention_period_days INTEGER,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_data_consents_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_consent_status CHECK (status IN ('PENDING', 'ACTIVE', 'WITHDRAWN', 'EXPIRED', 'UNDER_REVIEW', 'REJECTED', 'SUSPENDED', 'ARCHIVED')),
    CONSTRAINT chk_consent_type CHECK (consent_type IN ('ESSENTIAL_SERVICES', 'MARKETING', 'ANALYTICS', 'THIRD_PARTY_INTEGRATIONS', 'PERSONALIZATION', 'RESEARCH_AND_DEVELOPMENT', 'CUSTOMER_SUPPORT', 'LEGAL_COMPLIANCE', 'DATA_PORTABILITY', 'AUTOMATED_DECISION_MAKING', 'HEALTH_DATA_PROCESSING', 'SENSITIVE_DATA_PROCESSING'))
);

-- Data Subject Rights Requests
CREATE TABLE IF NOT EXISTS data_subject_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    request_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    request_description TEXT NOT NULL,
    verification_method VARCHAR(50) NOT NULL DEFAULT 'email',
    verification_completed BOOLEAN DEFAULT FALSE,
    verification_completed_at TIMESTAMP,
    requested_data_scope JSONB,
    processing_reason TEXT,
    legal_basis VARCHAR(100),
    data_retention_period VARCHAR(100),
    third_party_recipients JSONB,
    automated_decision_making BOOLEAN DEFAULT FALSE,
    data_portability_format VARCHAR(20),
    estimated_completion_date TIMESTAMP,
    actual_completion_date TIMESTAMP,
    response_data JSONB,
    rejection_reason TEXT,
    appeal_deadline TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_data_subject_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'ON_HOLD', 'CANCELLED', 'UNDER_LEGAL_REVIEW', 'AWAITING_VERIFICATION', 'ESCALATED', 'UNDER_APPEAL', 'PARTIALLY_COMPLETED', 'ARCHIVED')),
    CONSTRAINT chk_request_type CHECK (request_type IN ('ACCESS', 'RECTIFICATION', 'ERASURE', 'RESTRICT_PROCESSING', 'DATA_PORTABILITY', 'OBJECT_TO_PROCESSING', 'WITHDRAW_CONSENT', 'LODGE_COMPLAINT', 'COMPENSATION', 'REPRESENTATION', 'HUMAN_INTERVENTION', 'EXPLANATION', 'NOTIFICATION', 'INFORMATION', 'VERIFICATION', 'APPEAL'))
);

-- =============================================
-- HIPAA COMPLIANCE TABLES
-- =============================================

-- Protected Health Information (PHI)
CREATE TABLE IF NOT EXISTS protected_health_information (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    phi_category VARCHAR(100) NOT NULL,
    phi_data TEXT NOT NULL, -- Encrypted PHI data
    is_encrypted BOOLEAN NOT NULL DEFAULT TRUE,
    encryption_algorithm VARCHAR(50),
    encryption_key_id VARCHAR(100),
    data_hash VARCHAR(64) NOT NULL, -- SHA-256 hash
    access_purpose VARCHAR(100) NOT NULL,
    legal_basis VARCHAR(100) NOT NULL,
    authorization_expires_at TIMESTAMP,
    minimum_necessary_standard BOOLEAN DEFAULT TRUE,
    data_retention_policy VARCHAR(100),
    retention_expires_at TIMESTAMP,
    audit_trail_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    breach_notification_required BOOLEAN DEFAULT TRUE,
    data_classification VARCHAR(50) NOT NULL,
    access_controls JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    
    CONSTRAINT fk_phi_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_phi_category CHECK (phi_category IN ('PATIENT_DEMOGRAPHICS', 'MEDICAL_HISTORY', 'CURRENT_CONDITIONS', 'TREATMENT_PLANS', 'MEDICATIONS', 'LAB_RESULTS', 'VITAL_SIGNS', 'MENTAL_HEALTH', 'SUBSTANCE_ABUSE', 'REPRODUCTIVE_HEALTH', 'GENETIC_INFORMATION', 'IMMUNIZATIONS', 'ALLERGIES', 'INSURANCE_BILLING', 'EMERGENCY_CONTACTS', 'ADVANCE_DIRECTIVES', 'CLINICAL_NOTES', 'RESEARCH_PARTICIPATION', 'QUALITY_IMPROVEMENT', 'PUBLIC_HEALTH', 'LEGAL_COMPLIANCE')),
    CONSTRAINT chk_data_classification CHECK (data_classification IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'RESTRICTED', 'CLASSIFIED', 'PERSONAL', 'SENSITIVE_PERSONAL', 'HEALTH_DATA', 'FINANCIAL_DATA', 'LEGAL_DATA', 'RESEARCH_DATA'))
);

-- =============================================
-- ENHANCED USER TABLE MODIFICATIONS
-- =============================================

-- Add GDPR compliance fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS gdpr_consent_given BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS gdpr_consent_date TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS gdpr_consent_version VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS marketing_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS analytics_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS third_party_sharing_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_retention_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_portability_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS automated_decision_consent BOOLEAN DEFAULT FALSE;

-- Add HIPAA compliance fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS hipaa_authorization_given BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS hipaa_authorization_date TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS hipaa_authorization_expires TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS hipaa_authorization_version VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS treatment_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS payment_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS operations_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS research_consent BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS marketing_health_consent BOOLEAN DEFAULT FALSE;

-- Add privacy control fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS privacy_level VARCHAR(50) DEFAULT 'STANDARD';
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_anonymization_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS pseudonymization_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_encryption_enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS audit_logging_enabled BOOLEAN DEFAULT TRUE;

-- Add data retention fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_retention_policy VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS retention_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS auto_deletion_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deletion_date TIMESTAMP;

-- Add access control fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS access_controls JSONB;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_based_access BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS multi_factor_required BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS session_timeout_minutes INTEGER DEFAULT 30;

-- Add audit fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_password_change TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;

-- Add data classification field to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS data_classification VARCHAR(50) DEFAULT 'PERSONAL';

-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================

-- Data Consents indexes
CREATE INDEX IF NOT EXISTS idx_data_consents_user_id ON data_consents(user_id);
CREATE INDEX IF NOT EXISTS idx_data_consents_tenant_id ON data_consents(tenant_id);
CREATE INDEX IF NOT EXISTS idx_data_consents_type ON data_consents(consent_type);
CREATE INDEX IF NOT EXISTS idx_data_consents_status ON data_consents(status);
CREATE INDEX IF NOT EXISTS idx_data_consents_created_at ON data_consents(created_at);
CREATE INDEX IF NOT EXISTS idx_data_consents_expires_at ON data_consents(consent_expires_at);

-- Data Subject Requests indexes
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_user_id ON data_subject_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_tenant_id ON data_subject_requests(tenant_id);
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_type ON data_subject_requests(request_type);
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_status ON data_subject_requests(status);
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_created_at ON data_subject_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_data_subject_requests_verification ON data_subject_requests(verification_completed);

-- PHI indexes
CREATE INDEX IF NOT EXISTS idx_phi_user_id ON protected_health_information(user_id);
CREATE INDEX IF NOT EXISTS idx_phi_tenant_id ON protected_health_information(tenant_id);
CREATE INDEX IF NOT EXISTS idx_phi_category ON protected_health_information(phi_category);
CREATE INDEX IF NOT EXISTS idx_phi_encrypted ON protected_health_information(is_encrypted);
CREATE INDEX IF NOT EXISTS idx_phi_created_at ON protected_health_information(created_at);
CREATE INDEX IF NOT EXISTS idx_phi_authorization_expires ON protected_health_information(authorization_expires_at);
CREATE INDEX IF NOT EXISTS idx_phi_retention_expires ON protected_health_information(retention_expires_at);

-- Enhanced user indexes
CREATE INDEX IF NOT EXISTS idx_users_gdpr_consent ON users(gdpr_consent_given);
CREATE INDEX IF NOT EXISTS idx_users_hipaa_auth ON users(hipaa_authorization_given);
CREATE INDEX IF NOT EXISTS idx_users_privacy_level ON users(privacy_level);
CREATE INDEX IF NOT EXISTS idx_users_data_classification ON users(data_classification);
CREATE INDEX IF NOT EXISTS idx_users_retention_expires ON users(retention_expires_at);
CREATE INDEX IF NOT EXISTS idx_users_deletion_date ON users(deletion_date);
CREATE INDEX IF NOT EXISTS idx_users_multi_factor ON users(multi_factor_required);
CREATE INDEX IF NOT EXISTS idx_users_account_locked ON users(account_locked_until);

-- =============================================
-- CONSTRAINTS AND VALIDATIONS
-- =============================================

-- Add constraints to users table
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_privacy_level CHECK (privacy_level IN ('MINIMUM', 'STANDARD', 'ENHANCED', 'MAXIMUM', 'CUSTOM', 'COMPLIANCE', 'HEALTHCARE', 'FINANCIAL', 'RESEARCH', 'GOVERNMENT'));
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_data_classification CHECK (data_classification IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'RESTRICTED', 'CLASSIFIED', 'PERSONAL', 'SENSITIVE_PERSONAL', 'HEALTH_DATA', 'FINANCIAL_DATA', 'LEGAL_DATA', 'RESEARCH_DATA'));
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_session_timeout CHECK (session_timeout_minutes >= 5 AND session_timeout_minutes <= 1440);

-- =============================================
-- FUNCTIONS AND TRIGGERS
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_data_consents_updated_at BEFORE UPDATE ON data_consents FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_data_subject_requests_updated_at BEFORE UPDATE ON data_subject_requests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_phi_updated_at BEFORE UPDATE ON protected_health_information FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to validate consent requirements
CREATE OR REPLACE FUNCTION validate_consent_requirements()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if user has given GDPR consent for marketing
    IF NEW.marketing_consent = TRUE AND NEW.gdpr_consent_given = FALSE THEN
        RAISE EXCEPTION 'Marketing consent requires GDPR consent';
    END IF;
    
    -- Check if user has given GDPR consent for analytics
    IF NEW.analytics_consent = TRUE AND NEW.gdpr_consent_given = FALSE THEN
        RAISE EXCEPTION 'Analytics consent requires GDPR consent';
    END IF;
    
    -- Check if user has given GDPR consent for third-party sharing
    IF NEW.third_party_sharing_consent = TRUE AND NEW.gdpr_consent_given = FALSE THEN
        RAISE EXCEPTION 'Third-party sharing consent requires GDPR consent';
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for consent validation
CREATE TRIGGER validate_user_consents BEFORE INSERT OR UPDATE ON users FOR EACH ROW EXECUTE FUNCTION validate_consent_requirements();

-- =============================================
-- VIEWS FOR COMPLIANCE REPORTING
-- =============================================

-- View for GDPR consent status
CREATE OR REPLACE VIEW gdpr_consent_status AS
SELECT 
    u.id,
    u.tenant_id,
    u.email,
    u.first_name,
    u.last_name,
    u.gdpr_consent_given,
    u.gdpr_consent_date,
    u.gdpr_consent_version,
    u.marketing_consent,
    u.analytics_consent,
    u.third_party_sharing_consent,
    u.data_retention_consent,
    u.data_portability_consent,
    u.automated_decision_consent,
    u.created_at,
    u.updated_at
FROM users u
WHERE u.gdpr_consent_given = TRUE;

-- View for HIPAA authorization status
CREATE OR REPLACE VIEW hipaa_authorization_status AS
SELECT 
    u.id,
    u.tenant_id,
    u.email,
    u.first_name,
    u.last_name,
    u.hipaa_authorization_given,
    u.hipaa_authorization_date,
    u.hipaa_authorization_expires,
    u.hipaa_authorization_version,
    u.treatment_consent,
    u.payment_consent,
    u.operations_consent,
    u.research_consent,
    u.marketing_health_consent,
    u.created_at,
    u.updated_at
FROM users u
WHERE u.hipaa_authorization_given = TRUE;

-- View for data subject requests summary
CREATE OR REPLACE VIEW data_subject_requests_summary AS
SELECT 
    tenant_id,
    request_type,
    status,
    COUNT(*) as request_count,
    AVG(EXTRACT(EPOCH FROM (actual_completion_date - created_at))/86400) as avg_completion_days
FROM data_subject_requests
GROUP BY tenant_id, request_type, status;

-- View for PHI access summary
CREATE OR REPLACE VIEW phi_access_summary AS
SELECT 
    tenant_id,
    phi_category,
    data_classification,
    COUNT(*) as record_count,
    COUNT(CASE WHEN is_encrypted = TRUE THEN 1 END) as encrypted_count,
    COUNT(CASE WHEN audit_trail_enabled = TRUE THEN 1 END) as audited_count
FROM protected_health_information
GROUP BY tenant_id, phi_category, data_classification;

-- =============================================
-- INITIAL DATA
-- =============================================

-- Insert default consent types
INSERT INTO data_consents (user_id, tenant_id, consent_type, status, consent_text, consent_version, legal_basis, purpose)
SELECT 
    u.id,
    u.tenant_id,
    'ESSENTIAL_SERVICES',
    'ACTIVE',
    'Essential services consent for basic application functionality',
    '1.0',
    'legitimate_interest',
    'Essential service provision'
FROM users u
WHERE u.gdpr_consent_given = TRUE
ON CONFLICT DO NOTHING;

-- =============================================
-- GRANTS AND PERMISSIONS
-- =============================================

-- Grant permissions to application user (adjust username as needed)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_app_user;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO your_app_user;

-- =============================================
-- MIGRATION COMPLETION
-- =============================================

-- Log migration completion
INSERT INTO schema_migrations (version, description, applied_at) 
VALUES ('05-gdpr-hipaa-compliance', 'GDPR and HIPAA compliance implementation', CURRENT_TIMESTAMP)
ON CONFLICT (version) DO NOTHING;

COMMENT ON TABLE data_consents IS 'GDPR consent management for user data processing';
COMMENT ON TABLE data_subject_requests IS 'GDPR data subject rights requests (Articles 15-22)';
COMMENT ON TABLE protected_health_information IS 'HIPAA Protected Health Information (PHI) management';
COMMENT ON VIEW gdpr_consent_status IS 'View for GDPR consent compliance reporting';
COMMENT ON VIEW hipaa_authorization_status IS 'View for HIPAA authorization compliance reporting';
COMMENT ON VIEW data_subject_requests_summary IS 'Summary view for data subject requests reporting';
COMMENT ON VIEW phi_access_summary IS 'Summary view for PHI access reporting';

-- Migration completed successfully
SELECT 'GDPR and HIPAA compliance migration completed successfully' as status;
