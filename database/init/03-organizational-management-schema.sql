-- =============================================================================
-- Organizational Management Service Database Schema
-- =============================================================================
-- This script creates the database schema for the organizational management service
-- which handles multi-tenant SaaS platform organizational management and governance
-- =============================================================================

-- Create schema for organizational management
CREATE SCHEMA IF NOT EXISTS organizational_management;

-- =============================================================================
-- ORGANIZATIONS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    domain VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    logo_url TEXT,
    website_url TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    tier VARCHAR(50) NOT NULL DEFAULT 'STARTER',
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    subscription_plan_id UUID,
    trial_ends_at TIMESTAMP,
    subscription_ends_at TIMESTAMP,
    max_users INTEGER,
    current_users INTEGER DEFAULT 0,
    storage_limit_gb INTEGER,
    current_storage_gb INTEGER DEFAULT 0,
    api_rate_limit INTEGER,
    settings JSONB DEFAULT '{}',
    compliance_settings JSONB DEFAULT '{}',
    billing_info JSONB DEFAULT '{}',
    onboarding_completed BOOLEAN DEFAULT FALSE,
    onboarding_step VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for organizations table
CREATE INDEX idx_organizations_slug ON organizational_management.organizations(slug);
CREATE INDEX idx_organizations_domain ON organizational_management.organizations(domain);
CREATE INDEX idx_organizations_status ON organizational_management.organizations(status);
CREATE INDEX idx_organizations_tier ON organizational_management.organizations(tier);
CREATE INDEX idx_organizations_subscription_status ON organizational_management.organizations(subscription_status);
CREATE INDEX idx_organizations_created_at ON organizational_management.organizations(created_at);
CREATE INDEX idx_organizations_deleted_at ON organizational_management.organizations(deleted_at);

-- =============================================================================
-- ORGANIZATION MEMBERS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    permissions JSONB DEFAULT '{}',
    invited_by UUID,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    last_active_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    access_level VARCHAR(50),
    department VARCHAR(100),
    job_title VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for organization_members table
CREATE INDEX idx_organization_members_user_id ON organizational_management.organization_members(user_id);
CREATE INDEX idx_organization_members_organization_id ON organizational_management.organization_members(organization_id);
CREATE INDEX idx_organization_members_role ON organizational_management.organization_members(role);
CREATE INDEX idx_organization_members_status ON organizational_management.organization_members(status);
CREATE INDEX idx_organization_members_invited_at ON organizational_management.organization_members(invited_at);
CREATE INDEX idx_organization_members_accepted_at ON organizational_management.organization_members(accepted_at);
CREATE INDEX idx_organization_members_last_active_at ON organizational_management.organization_members(last_active_at);
CREATE INDEX idx_organization_members_deleted_at ON organizational_management.organization_members(deleted_at);

-- Create unique constraint for user-organization combination
CREATE UNIQUE INDEX idx_organization_members_user_org_unique 
ON organizational_management.organization_members(user_id, organization_id) 
WHERE deleted_at IS NULL;

-- =============================================================================
-- ORGANIZATION AUDIT LOGS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    resource_name VARCHAR(255),
    details JSONB DEFAULT '{}',
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    compliance_category VARCHAR(100),
    risk_level VARCHAR(50) DEFAULT 'LOW',
    tags TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for organization_audit_logs table
CREATE INDEX idx_org_audit_logs_organization_id ON organizational_management.organization_audit_logs(organization_id);
CREATE INDEX idx_org_audit_logs_user_id ON organizational_management.organization_audit_logs(user_id);
CREATE INDEX idx_org_audit_logs_action ON organizational_management.organization_audit_logs(action);
CREATE INDEX idx_org_audit_logs_resource_type ON organizational_management.organization_audit_logs(resource_type);
CREATE INDEX idx_org_audit_logs_created_at ON organizational_management.organization_audit_logs(created_at);
CREATE INDEX idx_org_audit_logs_risk_level ON organizational_management.organization_audit_logs(risk_level);
CREATE INDEX idx_org_audit_logs_compliance_category ON organizational_management.organization_audit_logs(compliance_category);

-- =============================================================================
-- MEMBER AUDIT LOGS TABLE
-- =============================================================================
CREATE TABLE organizational_management.member_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES organizational_management.organization_members(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    resource_name VARCHAR(255),
    details JSONB DEFAULT '{}',
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    compliance_category VARCHAR(100),
    risk_level VARCHAR(50) DEFAULT 'LOW',
    tags TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for member_audit_logs table
CREATE INDEX idx_member_audit_logs_member_id ON organizational_management.member_audit_logs(member_id);
CREATE INDEX idx_member_audit_logs_user_id ON organizational_management.member_audit_logs(user_id);
CREATE INDEX idx_member_audit_logs_action ON organizational_management.member_audit_logs(action);
CREATE INDEX idx_member_audit_logs_resource_type ON organizational_management.member_audit_logs(resource_type);
CREATE INDEX idx_member_audit_logs_created_at ON organizational_management.member_audit_logs(created_at);
CREATE INDEX idx_member_audit_logs_risk_level ON organizational_management.member_audit_logs(risk_level);
CREATE INDEX idx_member_audit_logs_compliance_category ON organizational_management.member_audit_logs(compliance_category);

-- =============================================================================
-- ORGANIZATION POLICIES TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    policy_type VARCHAR(100) NOT NULL,
    policy_data JSONB NOT NULL DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for organization_policies table
CREATE INDEX idx_organization_policies_organization_id ON organizational_management.organization_policies(organization_id);
CREATE INDEX idx_organization_policies_policy_type ON organizational_management.organization_policies(policy_type);
CREATE INDEX idx_organization_policies_is_active ON organizational_management.organization_policies(is_active);
CREATE INDEX idx_organization_policies_priority ON organizational_management.organization_policies(priority);
CREATE INDEX idx_organization_policies_deleted_at ON organizational_management.organization_policies(deleted_at);

-- =============================================================================
-- ORGANIZATION COMPLIANCE RECORDS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_compliance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    compliance_standard VARCHAR(100) NOT NULL,
    compliance_status VARCHAR(50) NOT NULL,
    assessment_date TIMESTAMP NOT NULL,
    next_assessment_date TIMESTAMP,
    compliance_score DECIMAL(5,2),
    findings JSONB DEFAULT '[]',
    remediation_plan JSONB DEFAULT '{}',
    auditor_id UUID,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for organization_compliance_records table
CREATE INDEX idx_org_compliance_org_id ON organizational_management.organization_compliance_records(organization_id);
CREATE INDEX idx_org_compliance_standard ON organizational_management.organization_compliance_records(compliance_standard);
CREATE INDEX idx_org_compliance_status ON organizational_management.organization_compliance_records(compliance_status);
CREATE INDEX idx_org_compliance_assessment_date ON organizational_management.organization_compliance_records(assessment_date);
CREATE INDEX idx_org_compliance_next_assessment_date ON organizational_management.organization_compliance_records(next_assessment_date);

-- =============================================================================
-- ORGANIZATION INTEGRATIONS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_integrations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    integration_type VARCHAR(100) NOT NULL,
    integration_name VARCHAR(255) NOT NULL,
    integration_config JSONB NOT NULL DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    error_message TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for organization_integrations table
CREATE INDEX idx_org_integrations_organization_id ON organizational_management.organization_integrations(organization_id);
CREATE INDEX idx_org_integrations_type ON organizational_management.organization_integrations(integration_type);
CREATE INDEX idx_org_integrations_is_active ON organizational_management.organization_integrations(is_active);
CREATE INDEX idx_org_integrations_sync_status ON organizational_management.organization_integrations(sync_status);
CREATE INDEX idx_org_integrations_deleted_at ON organizational_management.organization_integrations(deleted_at);

-- =============================================================================
-- ORGANIZATION METRICS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(50),
    metric_category VARCHAR(100),
    recorded_at TIMESTAMP NOT NULL,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for organization_metrics table
CREATE INDEX idx_org_metrics_organization_id ON organizational_management.organization_metrics(organization_id);
CREATE INDEX idx_org_metrics_name ON organizational_management.organization_metrics(metric_name);
CREATE INDEX idx_org_metrics_category ON organizational_management.organization_metrics(metric_category);
CREATE INDEX idx_org_metrics_recorded_at ON organizational_management.organization_metrics(recorded_at);

-- =============================================================================
-- ORGANIZATION ALERTS TABLE
-- =============================================================================
CREATE TABLE organizational_management.organization_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizational_management.organizations(id) ON DELETE CASCADE,
    alert_type VARCHAR(100) NOT NULL,
    alert_severity VARCHAR(50) NOT NULL,
    alert_title VARCHAR(255) NOT NULL,
    alert_message TEXT NOT NULL,
    alert_data JSONB DEFAULT '{}',
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by UUID,
    acknowledged_at TIMESTAMP,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_by UUID,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for organization_alerts table
CREATE INDEX idx_org_alerts_organization_id ON organizational_management.organization_alerts(organization_id);
CREATE INDEX idx_org_alerts_type ON organizational_management.organization_alerts(alert_type);
CREATE INDEX idx_org_alerts_severity ON organizational_management.organization_alerts(alert_severity);
CREATE INDEX idx_org_alerts_is_acknowledged ON organizational_management.organization_alerts(is_acknowledged);
CREATE INDEX idx_org_alerts_is_resolved ON organizational_management.organization_alerts(is_resolved);
CREATE INDEX idx_org_alerts_created_at ON organizational_management.organization_alerts(created_at);

-- =============================================================================
-- TRIGGERS FOR UPDATED_AT TIMESTAMPS
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_organizations_updated_at 
    BEFORE UPDATE ON organizational_management.organizations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organization_members_updated_at 
    BEFORE UPDATE ON organizational_management.organization_members 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organization_policies_updated_at 
    BEFORE UPDATE ON organizational_management.organization_policies 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organization_compliance_records_updated_at 
    BEFORE UPDATE ON organizational_management.organization_compliance_records 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organization_integrations_updated_at 
    BEFORE UPDATE ON organizational_management.organization_integrations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organization_alerts_updated_at 
    BEFORE UPDATE ON organizational_management.organization_alerts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- INITIAL DATA INSERTION
-- =============================================================================

-- Insert default organization policies
INSERT INTO organizational_management.organization_policies (organization_id, name, description, policy_type, policy_data, priority, created_by) VALUES
-- This will be populated when organizations are created
-- For now, we'll create a template policy that can be copied
('00000000-0000-0000-0000-000000000000', 'Default Security Policy', 'Default security policy for new organizations', 'SECURITY', '{"password_requirements": {"min_length": 8, "require_special_chars": true, "require_numbers": true}, "session_timeout": 3600, "max_login_attempts": 5}', 1, '00000000-0000-0000-0000-000000000000'),
('00000000-0000-0000-0000-000000000000', 'Default Data Retention Policy', 'Default data retention policy for new organizations', 'DATA_RETENTION', '{"user_data_retention_days": 2555, "audit_log_retention_days": 2555, "backup_retention_days": 365}', 2, '00000000-0000-0000-0000-000000000000'),
('00000000-0000-0000-0000-000000000000', 'Default Access Control Policy', 'Default access control policy for new organizations', 'ACCESS_CONTROL', '{"default_role": "MEMBER", "require_approval": false, "session_timeout": 3600}', 3, '00000000-0000-0000-0000-000000000000');

-- =============================================================================
-- COMMENTS
-- =============================================================================

COMMENT ON SCHEMA organizational_management IS 'Schema for organizational management service in multi-tenant SaaS platform';
COMMENT ON TABLE organizational_management.organizations IS 'Main organizations/tenants table';
COMMENT ON TABLE organizational_management.organization_members IS 'Organization members with roles and permissions';
COMMENT ON TABLE organizational_management.organization_audit_logs IS 'Audit logs for organization-level activities';
COMMENT ON TABLE organizational_management.member_audit_logs IS 'Audit logs for member-level activities';
COMMENT ON TABLE organizational_management.organization_policies IS 'Organization-specific policies and rules';
COMMENT ON TABLE organizational_management.organization_compliance_records IS 'Compliance records and assessments';
COMMENT ON TABLE organizational_management.organization_integrations IS 'External system integrations';
COMMENT ON TABLE organizational_management.organization_metrics IS 'Organization performance and usage metrics';
COMMENT ON TABLE organizational_management.organization_alerts IS 'System alerts and notifications';

-- =============================================================================
-- SCHEMA CREATION COMPLETE
-- =============================================================================
