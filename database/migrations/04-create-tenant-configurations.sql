-- Migration: Create tenant configurations table
-- Version: 4
-- Description: Creates table for storing tenant-specific configuration settings with versioning and audit trails

-- Create tenant_configurations table
CREATE TABLE IF NOT EXISTS tenant_configurations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    environment VARCHAR(100) NOT NULL DEFAULT 'default',
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_encrypted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    metadata JSONB
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_tenant_config_tenant_id ON tenant_configurations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_config_key ON tenant_configurations(config_key);
CREATE INDEX IF NOT EXISTS idx_tenant_config_environment ON tenant_configurations(environment);
CREATE INDEX IF NOT EXISTS idx_tenant_config_active ON tenant_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_tenant_config_tenant_key_env ON tenant_configurations(tenant_id, config_key, environment);
CREATE INDEX IF NOT EXISTS idx_tenant_config_created_at ON tenant_configurations(created_at);
CREATE INDEX IF NOT EXISTS idx_tenant_config_updated_at ON tenant_configurations(updated_at);
CREATE INDEX IF NOT EXISTS idx_tenant_config_created_by ON tenant_configurations(created_by);
CREATE INDEX IF NOT EXISTS idx_tenant_config_type ON tenant_configurations(config_type);

-- Create unique constraint for active configurations
CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_config_active 
ON tenant_configurations(tenant_id, config_key, environment) 
WHERE is_active = true;

-- Create partial index for global configurations (no tenant_id)
CREATE INDEX IF NOT EXISTS idx_tenant_config_global 
ON tenant_configurations(config_key, environment, is_active) 
WHERE tenant_id IS NULL OR tenant_id = '';

-- Add comments for documentation
COMMENT ON TABLE tenant_configurations IS 'Stores tenant-specific configuration settings with versioning and audit trails';
COMMENT ON COLUMN tenant_configurations.tenant_id IS 'Tenant identifier (null for global configs)';
COMMENT ON COLUMN tenant_configurations.config_key IS 'Configuration key name';
COMMENT ON COLUMN tenant_configurations.config_value IS 'Configuration value (encrypted if is_encrypted=true)';
COMMENT ON COLUMN tenant_configurations.config_type IS 'Type of configuration (STRING, INTEGER, BOOLEAN, JSON, ENCRYPTED, FEATURE_FLAG)';
COMMENT ON COLUMN tenant_configurations.environment IS 'Environment name (dev, test, prod, default)';
COMMENT ON COLUMN tenant_configurations.version IS 'Version number for configuration changes';
COMMENT ON COLUMN tenant_configurations.is_active IS 'Whether this configuration version is currently active';
COMMENT ON COLUMN tenant_configurations.is_encrypted IS 'Whether the config_value is encrypted';
COMMENT ON COLUMN tenant_configurations.created_at IS 'Timestamp when configuration was created';
COMMENT ON COLUMN tenant_configurations.updated_at IS 'Timestamp when configuration was last updated';
COMMENT ON COLUMN tenant_configurations.created_by IS 'User ID who created the configuration';
COMMENT ON COLUMN tenant_configurations.updated_by IS 'User ID who last updated the configuration';
COMMENT ON COLUMN tenant_configurations.metadata IS 'Additional metadata as JSON (change reason, tags, etc.)';

-- Insert some default global configurations
INSERT INTO tenant_configurations (tenant_id, config_key, config_value, config_type, environment, created_by, metadata) VALUES
(NULL, 'app.name', 'SaaS Starter Kit', 'STRING', 'default', 'system', '{"description": "Application name", "category": "branding"}'),
(NULL, 'app.version', '1.0.0', 'STRING', 'default', 'system', '{"description": "Application version", "category": "system"}'),
(NULL, 'feature.multi_tenancy', 'true', 'BOOLEAN', 'default', 'system', '{"description": "Enable multi-tenancy", "category": "features"}'),
(NULL, 'feature.advanced_logging', 'true', 'BOOLEAN', 'default', 'system', '{"description": "Enable advanced logging", "category": "features"}'),
(NULL, 'feature.opentelemetry', 'true', 'BOOLEAN', 'default', 'system', '{"description": "Enable OpenTelemetry tracing", "category": "features"}'),
(NULL, 'logging.level.root', 'INFO', 'STRING', 'default', 'system', '{"description": "Root logging level", "category": "logging"}'),
(NULL, 'logging.format', 'json', 'STRING', 'default', 'system', '{"description": "Log format", "category": "logging"}'),
(NULL, 'security.session.timeout', '3600', 'INTEGER', 'default', 'system', '{"description": "Session timeout in seconds", "category": "security"}'),
(NULL, 'security.password.min_length', '8', 'INTEGER', 'default', 'system', '{"description": "Minimum password length", "category": "security"}'),
(NULL, 'notification.email.enabled', 'true', 'BOOLEAN', 'default', 'system', '{"description": "Enable email notifications", "category": "notifications"}'),
(NULL, 'notification.sms.enabled', 'false', 'BOOLEAN', 'default', 'system', '{"description": "Enable SMS notifications", "category": "notifications"}'),
(NULL, 'billing.currency', 'USD', 'STRING', 'default', 'system', '{"description": "Default billing currency", "category": "billing"}'),
(NULL, 'billing.tax_rate', '0.0', 'STRING', 'default', 'system', '{"description": "Default tax rate", "category": "billing"}');

-- Insert environment-specific overrides
INSERT INTO tenant_configurations (tenant_id, config_key, config_value, config_type, environment, created_by, metadata) VALUES
(NULL, 'logging.level.root', 'DEBUG', 'STRING', 'dev', 'system', '{"description": "Development logging level", "category": "logging"}'),
(NULL, 'logging.level.root', 'WARN', 'STRING', 'prod', 'system', '{"description": "Production logging level", "category": "logging"}'),
(NULL, 'security.session.timeout', '1800', 'INTEGER', 'prod', 'system', '{"description": "Production session timeout", "category": "security"}'),
(NULL, 'feature.debug_mode', 'true', 'BOOLEAN', 'dev', 'system', '{"description": "Enable debug mode in development", "category": "features"}'),
(NULL, 'feature.debug_mode', 'false', 'BOOLEAN', 'prod', 'system', '{"description": "Disable debug mode in production", "category": "features"}');

-- Create view for active configurations
CREATE OR REPLACE VIEW active_tenant_configs AS
SELECT 
    tc.tenant_id,
    tc.config_key,
    tc.config_value,
    tc.config_type,
    tc.environment,
    tc.version,
    tc.created_at,
    tc.created_by,
    tc.metadata
FROM tenant_configurations tc
WHERE tc.is_active = true
ORDER BY tc.tenant_id, tc.config_key, tc.environment;

-- Create view for configuration audit trail
CREATE OR REPLACE VIEW config_audit_trail AS
SELECT 
    tc.tenant_id,
    tc.config_key,
    tc.config_value,
    tc.config_type,
    tc.environment,
    tc.version,
    tc.is_active,
    tc.created_at,
    tc.created_by,
    tc.updated_at,
    tc.updated_by,
    tc.metadata,
    CASE 
        WHEN tc.is_active THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END as status
FROM tenant_configurations tc
ORDER BY tc.tenant_id, tc.config_key, tc.environment, tc.version DESC;

-- Grant permissions (adjust based on your security model)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON tenant_configurations TO your_app_user;
-- GRANT SELECT ON active_tenant_configs TO your_app_user;
-- GRANT SELECT ON config_audit_trail TO your_app_user;
