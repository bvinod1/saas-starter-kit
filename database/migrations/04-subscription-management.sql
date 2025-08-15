-- Migration: 04-subscription-management.sql
-- Description: Create subscription management tables for SaaS Starter Kit

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create subscription plans table
CREATE TABLE IF NOT EXISTS plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tier VARCHAR(50) NOT NULL CHECK (tier IN ('FREE', 'STARTER', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM')),
    active BOOLEAN NOT NULL DEFAULT true,
    base_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    billing_cycle VARCHAR(50) NOT NULL CHECK (billing_cycle IN ('MONTHLY', 'QUARTERLY', 'ANNUALLY', 'BIENNIALLY', 'TRIENNIALLY', 'WEEKLY', 'DAILY', 'ONE_TIME')),
    trial_days INTEGER,
    max_users INTEGER,
    max_storage_gb INTEGER,
    max_api_calls_per_month BIGINT,
    max_tenants INTEGER,
    features JSONB,
    limits JSONB,
    metadata JSONB,
    sort_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    external_subscription_id VARCHAR(255) NOT NULL UNIQUE,
    plan_id VARCHAR(255) NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE', 'TRIAL', 'PAST_DUE', 'CANCELLED', 'SUSPENDED', 'PENDING', 'EXPIRED', 'CANCELLING', 'REACTIVATING', 'GRACE_PERIOD', 'CHANGING_PLAN')),
    billing_cycle VARCHAR(50) NOT NULL CHECK (billing_cycle IN ('MONTHLY', 'QUARTERLY', 'ANNUALLY', 'BIENNIALLY', 'TRIENNIALLY', 'WEEKLY', 'DAILY', 'ONE_TIME')),
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    trial_start TIMESTAMP,
    trial_end TIMESTAMP,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    quantity INTEGER NOT NULL DEFAULT 1,
    max_users INTEGER,
    max_storage_gb INTEGER,
    features JSONB,
    metadata JSONB,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    next_billing_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(100) NOT NULL DEFAULT 'zoho',
    sync_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED', 'IN_PROGRESS', 'RETRY_SCHEDULED', 'MANUAL_INTERVENTION_REQUIRED')),
    last_sync_at TIMESTAMP
);

-- Create checkout sessions table
CREATE TABLE IF NOT EXISTS checkout_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id VARCHAR(255) NOT NULL UNIQUE,
    tenant_id VARCHAR(255) NOT NULL,
    plan_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'EXPIRED', 'CANCELLED', 'FAILED')),
    checkout_url TEXT NOT NULL,
    success_url TEXT,
    cancel_url TEXT,
    return_url TEXT,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    quantity INTEGER NOT NULL DEFAULT 1,
    billing_cycle VARCHAR(50) NOT NULL CHECK (billing_cycle IN ('MONTHLY', 'QUARTERLY', 'ANNUALLY', 'BIENNIALLY', 'TRIENNIALLY', 'WEEKLY', 'DAILY', 'ONE_TIME')),
    trial_days INTEGER,
    metadata JSONB,
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(100) NOT NULL DEFAULT 'zoho',
    external_session_id VARCHAR(255)
);

-- Create usage tracking table
CREATE TABLE IF NOT EXISTS usage_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    subscription_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
);

-- Create webhook events table
CREATE TABLE IF NOT EXISTS webhook_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    subscription_id VARCHAR(255),
    tenant_id VARCHAR(255),
    payload JSONB NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT false,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(external_subscription_id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_plans_plan_id ON plans(plan_id);
CREATE INDEX IF NOT EXISTS idx_plans_active ON plans(active);
CREATE INDEX IF NOT EXISTS idx_plans_tier ON plans(tier);
CREATE INDEX IF NOT EXISTS idx_plans_sort_order ON plans(sort_order);

CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_external_subscription_id ON subscriptions(external_subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_sync_status ON subscriptions(sync_status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_provider ON subscriptions(provider);
CREATE INDEX IF NOT EXISTS idx_subscriptions_current_period_end ON subscriptions(current_period_end);

CREATE INDEX IF NOT EXISTS idx_checkout_sessions_session_id ON checkout_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_tenant_id ON checkout_sessions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_status ON checkout_sessions(status);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_provider ON checkout_sessions(provider);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_expires_at ON checkout_sessions(expires_at);

CREATE INDEX IF NOT EXISTS idx_usage_tracking_tenant_id ON usage_tracking(tenant_id);
CREATE INDEX IF NOT EXISTS idx_usage_tracking_subscription_id ON usage_tracking(subscription_id);
CREATE INDEX IF NOT EXISTS idx_usage_tracking_action ON usage_tracking(action);
CREATE INDEX IF NOT EXISTS idx_usage_tracking_recorded_at ON usage_tracking(recorded_at);

CREATE INDEX IF NOT EXISTS idx_webhook_events_event_id ON webhook_events(event_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_provider ON webhook_events(provider);
CREATE INDEX IF NOT EXISTS idx_webhook_events_event_type ON webhook_events(event_type);
CREATE INDEX IF NOT EXISTS idx_webhook_events_subscription_id ON webhook_events(subscription_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_tenant_id ON webhook_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_processed ON webhook_events(processed);
CREATE INDEX IF NOT EXISTS idx_webhook_events_created_at ON webhook_events(created_at);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_status ON subscriptions(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_plan ON subscriptions(tenant_id, plan_id);
CREATE INDEX IF NOT EXISTS idx_usage_tracking_tenant_action_date ON usage_tracking(tenant_id, action, recorded_at);

-- Add foreign key constraints
ALTER TABLE subscriptions 
ADD CONSTRAINT fk_subscriptions_plan 
FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE RESTRICT;

ALTER TABLE checkout_sessions 
ADD CONSTRAINT fk_checkout_sessions_plan 
FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE RESTRICT;

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_plans_updated_at BEFORE UPDATE ON plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_checkout_sessions_updated_at BEFORE UPDATE ON checkout_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default plans
INSERT INTO plans (plan_id, name, description, tier, base_price, billing_cycle, trial_days, max_users, max_storage_gb, max_api_calls_per_month, features, limits, sort_order) VALUES
('free', 'Free Plan', 'Basic features for small teams', 'FREE', 0.00, 'MONTHLY', 0, 5, 1, 1000, '{"basic_analytics": true, "email_support": false, "api_access": false}', '{"users": 5, "storage_gb": 1, "api_calls_per_month": 1000}', 1),
('starter', 'Starter Plan', 'Essential features for growing businesses', 'STARTER', 29.99, 'MONTHLY', 14, 25, 10, 10000, '{"basic_analytics": true, "email_support": true, "api_access": true, "advanced_analytics": false}', '{"users": 25, "storage_gb": 10, "api_calls_per_month": 10000}', 2),
('professional', 'Professional Plan', 'Advanced features for established companies', 'PROFESSIONAL', 99.99, 'MONTHLY', 14, 100, 100, 100000, '{"basic_analytics": true, "email_support": true, "api_access": true, "advanced_analytics": true, "priority_support": true, "custom_integrations": false}', '{"users": 100, "storage_gb": 100, "api_calls_per_month": 100000}', 3),
('enterprise', 'Enterprise Plan', 'Full features for large organizations', 'ENTERPRISE', 299.99, 'MONTHLY', 30, 1000, 1000, 1000000, '{"basic_analytics": true, "email_support": true, "api_access": true, "advanced_analytics": true, "priority_support": true, "custom_integrations": true, "dedicated_support": true, "sla_guarantee": true}', '{"users": 1000, "storage_gb": 1000, "api_calls_per_month": 1000000}', 4)
ON CONFLICT (plan_id) DO NOTHING;

-- Create view for subscription summary
CREATE OR REPLACE VIEW subscription_summary AS
SELECT 
    s.id,
    s.tenant_id,
    s.external_subscription_id,
    s.plan_id,
    p.name as plan_name,
    s.status,
    s.billing_cycle,
    s.current_period_start,
    s.current_period_end,
    s.amount,
    s.currency,
    s.quantity,
    s.max_users,
    s.max_storage_gb,
    s.provider,
    s.sync_status,
    s.last_sync_at,
    s.created_at,
    s.updated_at
FROM subscriptions s
JOIN plans p ON s.plan_id = p.plan_id;

-- Create view for tenant subscription status
CREATE OR REPLACE VIEW tenant_subscription_status AS
SELECT 
    s.tenant_id,
    s.status as subscription_status,
    p.tier as plan_tier,
    p.name as plan_name,
    s.amount as monthly_amount,
    s.currency,
    s.current_period_end,
    s.trial_end,
    s.max_users,
    s.max_storage_gb,
    s.sync_status,
    s.last_sync_at
FROM subscriptions s
JOIN plans p ON s.plan_id = p.plan_id
WHERE s.status IN ('ACTIVE', 'TRIAL', 'PAST_DUE');

-- Grant permissions (adjust as needed for your database setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_app_user;
