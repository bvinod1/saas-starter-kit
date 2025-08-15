-- Multi-Tenant Database Schema (One Schema Per Tenant)
-- This script sets up the infrastructure for schema-per-tenant multi-tenancy

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- TENANT MANAGEMENT SCHEMA
-- =============================================

-- Create a dedicated schema for tenant management
CREATE SCHEMA IF NOT EXISTS tenant_management;

-- Tenants table (master tenant registry)
CREATE TABLE tenant_management.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    domain VARCHAR(255),
    keycloak_realm_id VARCHAR(255) UNIQUE,
    keycloak_client_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'active', -- active, inactive, suspended, deleted
    subscription_plan_id UUID,
    subscription_status VARCHAR(50) DEFAULT 'trial',
    trial_ends_at TIMESTAMP,
    max_users INTEGER DEFAULT 10,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tenant database schemas registry
CREATE TABLE tenant_management.tenant_schemas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID REFERENCES tenant_management.tenants(id) ON DELETE CASCADE,
    schema_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'active', -- active, migrating, inactive
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, schema_name)
);

-- Tenant users (cross-tenant user registry)
CREATE TABLE tenant_management.tenant_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID REFERENCES tenant_management.tenants(id) ON DELETE CASCADE,
    keycloak_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    roles JSONB DEFAULT '[]',
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, keycloak_user_id)
);

-- =============================================
-- SHARED SCHEMA (for system-wide data)
-- =============================================

CREATE SCHEMA IF NOT EXISTS shared;

-- Subscription plans (shared across all tenants)
CREATE TABLE shared.subscription_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    billing_cycle VARCHAR(20) NOT NULL, -- monthly, yearly
    features JSONB DEFAULT '{}',
    max_users INTEGER DEFAULT 10,
    max_storage_gb INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    trial_days INTEGER DEFAULT 14,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System-wide notification templates
CREATE TABLE shared.notification_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- email, sms, push
    subject VARCHAR(255),
    content TEXT NOT NULL,
    variables JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TENANT SCHEMA TEMPLATE FUNCTION
-- =============================================

-- Function to create a new tenant schema
CREATE OR REPLACE FUNCTION create_tenant_schema(tenant_slug VARCHAR(100))
RETURNS VOID AS $$
DECLARE
    schema_name VARCHAR(100);
BEGIN
    -- Create schema name based on tenant slug (replace hyphens with underscores)
    schema_name := 'tenant_' || REPLACE(tenant_slug, '-', '_');
    
    -- Create the schema
    EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || schema_name;
    
    -- Create tenant-specific tables
    EXECUTE '
        CREATE TABLE ' || schema_name || '.users (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            keycloak_user_id VARCHAR(255) UNIQUE NOT NULL,
            email VARCHAR(255) NOT NULL,
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            avatar_url TEXT,
            bio TEXT,
            timezone VARCHAR(50) DEFAULT ''UTC'',
            language VARCHAR(10) DEFAULT ''en'',
            preferences JSONB DEFAULT ''{}'',
            status VARCHAR(50) DEFAULT ''active'',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.user_roles (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            user_id UUID REFERENCES ' || schema_name || '.users(id) ON DELETE CASCADE,
            role_name VARCHAR(100) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(user_id, role_name)
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.organizations (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            name VARCHAR(255) NOT NULL,
            slug VARCHAR(100) UNIQUE NOT NULL,
            description TEXT,
            logo_url TEXT,
            settings JSONB DEFAULT ''{}'',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.organization_members (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id) ON DELETE CASCADE,
            user_id UUID REFERENCES ' || schema_name || '.users(id) ON DELETE CASCADE,
            role VARCHAR(50) NOT NULL, -- owner, admin, member
            permissions JSONB DEFAULT ''{}'',
            joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(organization_id, user_id)
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.subscriptions (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id) ON DELETE CASCADE,
            plan_id UUID REFERENCES shared.subscription_plans(id),
            status VARCHAR(50) NOT NULL, -- active, canceled, past_due, unpaid
            current_period_start TIMESTAMP,
            current_period_end TIMESTAMP,
            cancel_at_period_end BOOLEAN DEFAULT false,
            canceled_at TIMESTAMP,
            trial_start TIMESTAMP,
            trial_end TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.invoices (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            subscription_id UUID REFERENCES ' || schema_name || '.subscriptions(id),
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id),
            amount DECIMAL(10,2) NOT NULL,
            currency VARCHAR(3) DEFAULT ''USD'',
            status VARCHAR(50) NOT NULL, -- draft, open, paid, void, uncollectible
            due_date TIMESTAMP,
            paid_at TIMESTAMP,
            invoice_number VARCHAR(100) UNIQUE,
            external_invoice_id VARCHAR(255),
            items JSONB DEFAULT ''[]'',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.notifications (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            user_id UUID REFERENCES ' || schema_name || '.users(id) ON DELETE CASCADE,
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id),
            template_id UUID REFERENCES shared.notification_templates(id),
            type VARCHAR(50) NOT NULL, -- email, sms, push, in_app
            title VARCHAR(255),
            message TEXT NOT NULL,
            data JSONB DEFAULT ''{}'',
            status VARCHAR(50) DEFAULT ''pending'', -- pending, sent, failed, read
            sent_at TIMESTAMP,
            read_at TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.user_activities (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            user_id UUID REFERENCES ' || schema_name || '.users(id) ON DELETE SET NULL,
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id) ON DELETE SET NULL,
            action VARCHAR(100) NOT NULL,
            resource_type VARCHAR(100),
            resource_id UUID,
            ip_address INET,
            user_agent TEXT,
            metadata JSONB DEFAULT ''{}'',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    EXECUTE '
        CREATE TABLE ' || schema_name || '.api_usage (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            user_id UUID REFERENCES ' || schema_name || '.users(id) ON DELETE SET NULL,
            organization_id UUID REFERENCES ' || schema_name || '.organizations(id) ON DELETE SET NULL,
            endpoint VARCHAR(255) NOT NULL,
            method VARCHAR(10) NOT NULL,
            status_code INTEGER,
            response_time INTEGER, -- in milliseconds
            ip_address INET,
            user_agent TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ';
    
    -- Create indexes for better performance
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_users_email ON ' || schema_name || '.users(email)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_users_keycloak_id ON ' || schema_name || '.users(keycloak_user_id)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_organizations_slug ON ' || schema_name || '.organizations(slug)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_subscriptions_org_id ON ' || schema_name || '.subscriptions(organization_id)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_notifications_user_id ON ' || schema_name || '.notifications(user_id)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_user_activities_user_id ON ' || schema_name || '.user_activities(user_id)';
    EXECUTE 'CREATE INDEX idx_' || schema_name || '_api_usage_user_id ON ' || schema_name || '.api_usage(user_id)';
    
    -- Create updated_at trigger function for this schema
    EXECUTE '
        CREATE OR REPLACE FUNCTION ' || schema_name || '.update_updated_at_column()
        RETURNS TRIGGER AS $func$
        BEGIN
            NEW.updated_at = CURRENT_TIMESTAMP;
            RETURN NEW;
        END;
        $func$ language ''plpgsql''
    ';
    
    -- Apply updated_at triggers
    EXECUTE 'CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON ' || schema_name || '.users FOR EACH ROW EXECUTE FUNCTION ' || schema_name || '.update_updated_at_column()';
    EXECUTE 'CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON ' || schema_name || '.organizations FOR EACH ROW EXECUTE FUNCTION ' || schema_name || '.update_updated_at_column()';
    EXECUTE 'CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON ' || schema_name || '.subscriptions FOR EACH ROW EXECUTE FUNCTION ' || schema_name || '.update_updated_at_column()';
    EXECUTE 'CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON ' || schema_name || '.invoices FOR EACH ROW EXECUTE FUNCTION ' || schema_name || '.update_updated_at_column()';
    
    -- Insert default organization for the tenant
    EXECUTE '
        INSERT INTO ' || schema_name || '.organizations (name, slug, description)
        VALUES (''Default Organization'', ''default'', ''Default organization for this tenant'')
    ';
    
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- TENANT MANAGEMENT FUNCTIONS
-- =============================================

-- Function to create a new tenant
CREATE OR REPLACE FUNCTION create_tenant(
    tenant_name VARCHAR(255),
    tenant_slug VARCHAR(100),
    keycloak_realm_id VARCHAR(255),
    keycloak_client_id VARCHAR(255),
    max_users INTEGER DEFAULT 10
)
RETURNS UUID AS $$
DECLARE
    tenant_id UUID;
BEGIN
    -- Insert tenant record
    INSERT INTO tenant_management.tenants (
        name, slug, keycloak_realm_id, keycloak_client_id, max_users
    ) VALUES (
        tenant_name, tenant_slug, keycloak_realm_id, keycloak_client_id, max_users
    ) RETURNING id INTO tenant_id;
    
    -- Create tenant schema
    PERFORM create_tenant_schema(tenant_slug);
    
    -- Record schema creation
    INSERT INTO tenant_management.tenant_schemas (tenant_id, schema_name)
    VALUES (tenant_id, 'tenant_' || tenant_slug);
    
    -- Insert default organization for the tenant with the actual organization name
    EXECUTE '
        INSERT INTO ' || 'tenant_' || REPLACE(tenant_slug, '-', '_') || '.organizations (name, slug, description)
        VALUES (''' || tenant_name || ''', ''default'', ''Default organization for this tenant'')
    ';
    
    RETURN tenant_id;
END;
$$ LANGUAGE plpgsql;

-- Function to get tenant schema name
CREATE OR REPLACE FUNCTION get_tenant_schema(tenant_slug VARCHAR(100))
RETURNS VARCHAR(100) AS $$
BEGIN
    RETURN 'tenant_' || REPLACE(tenant_slug, '-', '_');
END;
$$ LANGUAGE plpgsql;

-- Function to check if tenant exists
CREATE OR REPLACE FUNCTION tenant_exists(tenant_slug VARCHAR(100))
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM tenant_management.tenants 
        WHERE slug = tenant_slug AND status = 'active'
    );
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- INSERT DEFAULT DATA
-- =============================================

-- Insert default subscription plans
INSERT INTO shared.subscription_plans (name, description, price, currency, billing_cycle, features, max_users, max_storage_gb, trial_days) VALUES
('Starter', 'Perfect for small teams', 29.00, 'USD', 'monthly', '{"basic_analytics": true, "email_support": true}', 5, 10, 14),
('Professional', 'For growing businesses', 99.00, 'USD', 'monthly', '{"advanced_analytics": true, "priority_support": true, "api_access": true}', 25, 100, 14),
('Enterprise', 'For large organizations', 299.00, 'USD', 'monthly', '{"custom_analytics": true, "dedicated_support": true, "api_access": true, "sso": true}', -1, 1024, 30)
ON CONFLICT DO NOTHING;

-- Insert default notification templates
INSERT INTO shared.notification_templates (name, type, subject, content) VALUES
('welcome_email', 'email', 'Welcome to SaaS Starter Kit', 'Hello {{firstName}}, welcome to our platform!'),
('password_reset', 'email', 'Password Reset Request', 'Click here to reset your password: {{resetLink}}'),
('subscription_expired', 'email', 'Subscription Expired', 'Your subscription has expired. Please renew to continue using our services.'),
('welcome_sms', 'sms', NULL, 'Welcome to SaaS Starter Kit! Your account is now active.'),
('in_app_notification', 'in_app', NULL, '{{message}}')
ON CONFLICT DO NOTHING;

-- Create indexes for tenant management
CREATE INDEX idx_tenants_slug ON tenant_management.tenants(slug);
CREATE INDEX idx_tenants_status ON tenant_management.tenants(status);
CREATE INDEX idx_tenant_users_tenant_id ON tenant_management.tenant_users(tenant_id);
CREATE INDEX idx_tenant_users_keycloak_id ON tenant_management.tenant_users(keycloak_user_id);

-- Create updated_at trigger for tenant management tables
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenant_management.tenants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tenant_schemas_updated_at BEFORE UPDATE ON tenant_management.tenant_schemas FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tenant_users_updated_at BEFORE UPDATE ON tenant_management.tenant_users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_subscription_plans_updated_at BEFORE UPDATE ON shared.subscription_plans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notification_templates_updated_at BEFORE UPDATE ON shared.notification_templates FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
