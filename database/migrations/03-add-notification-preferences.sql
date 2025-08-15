-- Migration: Add Notification Preferences and Tables
-- This script adds notification preferences to the user table and creates notification-related tables

-- =============================================
-- ADD NOTIFICATION PREFERENCES TO USER TABLE
-- =============================================

-- Add notification preferences column to users table
ALTER TABLE auth.users 
ADD COLUMN IF NOT EXISTS notification_preferences JSONB DEFAULT '{
  "email_enabled": true,
  "sms_enabled": true,
  "push_enabled": true,
  "in_app_enabled": true,
  "webhook_enabled": false,
  "preferred_language": "en",
  "fallback_language": "en",
  "timezone": "UTC",
  "digest_frequency": "DAILY",
  "max_notifications_per_day": 50,
  "urgent_notifications_always": true
}'::jsonb;

-- Add notification preferences column to tenant users table
ALTER TABLE tenant_management.tenant_users 
ADD COLUMN IF NOT EXISTS notification_preferences JSONB DEFAULT '{
  "email_enabled": true,
  "sms_enabled": true,
  "push_enabled": true,
  "in_app_enabled": true,
  "webhook_enabled": false,
  "preferred_language": "en",
  "fallback_language": "en",
  "timezone": "UTC",
  "digest_frequency": "DAILY",
  "max_notifications_per_day": 50,
  "urgent_notifications_always": true
}'::jsonb;

-- =============================================
-- CREATE NOTIFICATION TEMPLATES TABLE
-- =============================================

-- Create notification templates table in shared schema
CREATE TABLE IF NOT EXISTS shared.notification_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK')),
    subject VARCHAR(255),
    content TEXT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    variables JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    tenant_id VARCHAR(255),
    category VARCHAR(100),
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    retry_count INTEGER DEFAULT 3,
    expiry_hours INTEGER DEFAULT 24,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create unique constraint on template name and tenant
CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_templates_name_tenant 
ON shared.notification_templates(name, COALESCE(tenant_id, 'shared'));

-- =============================================
-- CREATE NOTIFICATIONS TABLE
-- =============================================

-- Create notifications table in shared schema
CREATE TABLE IF NOT EXISTS shared.notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255),
    organization_id VARCHAR(255),
    template_id UUID REFERENCES shared.notification_templates(id),
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK')),
    title VARCHAR(255),
    message TEXT NOT NULL,
    subject VARCHAR(255),
    recipient VARCHAR(255) NOT NULL,
    data JSONB DEFAULT '{}',
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SCHEDULED', 'SENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'CANCELLED', 'EXPIRED')),
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    attempt_count INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    provider_response TEXT,
    channel_specific_data JSONB DEFAULT '{}',
    expires_at TIMESTAMP,
    source_event VARCHAR(255),
    correlation_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- CREATE USER NOTIFICATION PREFERENCES TABLE
-- =============================================

-- Create user notification preferences table in shared schema
CREATE TABLE IF NOT EXISTS shared.user_notification_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255),
    organization_id VARCHAR(255),
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    in_app_enabled BOOLEAN DEFAULT true,
    webhook_enabled BOOLEAN DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'UTC',
    preferred_language VARCHAR(10) DEFAULT 'en',
    fallback_language VARCHAR(10) DEFAULT 'en',
    category_preferences JSONB DEFAULT '{}',
    digest_frequency VARCHAR(20) DEFAULT 'DAILY' CHECK (digest_frequency IN ('IMMEDIATE', 'HOURLY', 'DAILY', 'WEEKLY', 'NEVER')),
    max_notifications_per_day INTEGER DEFAULT 50,
    urgent_notifications_always BOOLEAN DEFAULT true,
    email_preferences JSONB DEFAULT '{}',
    sms_preferences JSONB DEFAULT '{}',
    push_preferences JSONB DEFAULT '{}',
    in_app_preferences JSONB DEFAULT '{}',
    device_tokens JSONB DEFAULT '[]',
    webhook_endpoints JSONB DEFAULT '[]',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, tenant_id)
);

-- =============================================
-- CREATE INDEXES
-- =============================================

-- Indexes for notification templates
CREATE INDEX IF NOT EXISTS idx_notification_templates_type ON shared.notification_templates(type);
CREATE INDEX IF NOT EXISTS idx_notification_templates_category ON shared.notification_templates(category);
CREATE INDEX IF NOT EXISTS idx_notification_templates_tenant ON shared.notification_templates(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notification_templates_active ON shared.notification_templates(is_active);

-- Indexes for notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON shared.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_tenant_id ON shared.notifications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON shared.notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON shared.notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON shared.notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled_at ON shared.notifications(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_correlation_id ON shared.notifications(correlation_id);
CREATE INDEX IF NOT EXISTS idx_notifications_source_event ON shared.notifications(source_event);

-- Indexes for user notification preferences
CREATE INDEX IF NOT EXISTS idx_user_notification_preferences_user_id ON shared.user_notification_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_notification_preferences_tenant_id ON shared.user_notification_preferences(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_notification_preferences_email_enabled ON shared.user_notification_preferences(email_enabled);
CREATE INDEX IF NOT EXISTS idx_user_notification_preferences_sms_enabled ON shared.user_notification_preferences(sms_enabled);
CREATE INDEX IF NOT EXISTS idx_user_notification_preferences_push_enabled ON shared.user_notification_preferences(push_enabled);

-- =============================================
-- CREATE TRIGGERS
-- =============================================

-- Create updated_at trigger for notification tables
CREATE OR REPLACE FUNCTION update_notification_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers
CREATE TRIGGER update_notification_templates_updated_at 
    BEFORE UPDATE ON shared.notification_templates 
    FOR EACH ROW EXECUTE FUNCTION update_notification_updated_at();

CREATE TRIGGER update_notifications_updated_at 
    BEFORE UPDATE ON shared.notifications 
    FOR EACH ROW EXECUTE FUNCTION update_notification_updated_at();

CREATE TRIGGER update_user_notification_preferences_updated_at 
    BEFORE UPDATE ON shared.user_notification_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_notification_updated_at();

-- =============================================
-- INSERT DEFAULT TEMPLATES
-- =============================================

-- Insert default notification templates
INSERT INTO shared.notification_templates (name, type, subject, content, category, priority) VALUES
('welcome_email', 'EMAIL', 'Welcome to SaaS Starter Kit', 'Hello {{firstName}}, welcome to our platform! We''re excited to have you on board.', 'welcome', 'NORMAL'),
('password_reset', 'EMAIL', 'Password Reset Request', 'Click here to reset your password: {{resetLink}}. This link will expire in {{expiryHours}} hours.', 'security', 'HIGH'),
('subscription_expired', 'EMAIL', 'Subscription Expired', 'Your subscription has expired. Please renew to continue using our services.', 'billing', 'URGENT'),
('welcome_sms', 'SMS', NULL, 'Welcome to SaaS Starter Kit! Your account is now active.', 'welcome', 'NORMAL'),
('in_app_notification', 'IN_APP', NULL, '{{message}}', 'general', 'NORMAL'),
('billing_reminder', 'EMAIL', 'Billing Reminder', 'Your subscription will renew on {{renewalDate}}. Amount: {{amount}}', 'billing', 'NORMAL'),
('security_alert', 'EMAIL', 'Security Alert', 'We detected a login from a new device. If this wasn''t you, please contact support immediately.', 'security', 'URGENT'),
('feature_update', 'IN_APP', 'New Feature Available', '{{featureName}} is now available! {{description}}', 'updates', 'LOW')
ON CONFLICT (name, COALESCE(tenant_id, 'shared')) DO NOTHING;

-- =============================================
-- CREATE FUNCTIONS
-- =============================================

-- Function to get user notification preferences
CREATE OR REPLACE FUNCTION get_user_notification_preferences(
    p_user_id VARCHAR(255),
    p_tenant_id VARCHAR(255) DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    result JSONB;
BEGIN
    -- Try to get preferences from user_notification_preferences table first
    SELECT to_jsonb(prefs.*) INTO result
    FROM shared.user_notification_preferences prefs
    WHERE prefs.user_id = p_user_id 
      AND (p_tenant_id IS NULL OR prefs.tenant_id = p_tenant_id);
    
    -- If not found, try to get from users table
    IF result IS NULL THEN
        SELECT notification_preferences INTO result
        FROM auth.users
        WHERE keycloak_user_id = p_user_id;
    END IF;
    
    -- If still not found, try tenant users table
    IF result IS NULL AND p_tenant_id IS NOT NULL THEN
        SELECT notification_preferences INTO result
        FROM tenant_management.tenant_users
        WHERE keycloak_user_id = p_user_id AND tenant_id = p_tenant_id;
    END IF;
    
    -- Return default preferences if nothing found
    IF result IS NULL THEN
        result := '{
            "email_enabled": true,
            "sms_enabled": true,
            "push_enabled": true,
            "in_app_enabled": true,
            "webhook_enabled": false,
            "preferred_language": "en",
            "fallback_language": "en",
            "timezone": "UTC",
            "digest_frequency": "DAILY",
            "max_notifications_per_day": 50,
            "urgent_notifications_always": true
        }'::jsonb;
    END IF;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to check if user can receive notifications
CREATE OR REPLACE FUNCTION can_user_receive_notification(
    p_user_id VARCHAR(255),
    p_tenant_id VARCHAR(255) DEFAULT NULL,
    p_priority VARCHAR(20) DEFAULT 'NORMAL'
)
RETURNS BOOLEAN AS $$
DECLARE
    prefs JSONB;
    current_time TIME;
    quiet_start TIME;
    quiet_end TIME;
    urgent_always BOOLEAN;
BEGIN
    -- Get user preferences
    prefs := get_user_notification_preferences(p_user_id, p_tenant_id);
    
    -- Check if urgent notifications are always allowed
    urgent_always := COALESCE(prefs->>'urgent_notifications_always', 'true')::boolean;
    
    -- If urgent notification and always allowed, return true
    IF p_priority = 'URGENT' AND urgent_always THEN
        RETURN true;
    END IF;
    
    -- Get current time in user's timezone
    current_time := (CURRENT_TIMESTAMP AT TIME ZONE COALESCE(prefs->>'timezone', 'UTC'))::time;
    
    -- Get quiet hours
    quiet_start := (prefs->>'quiet_hours_start')::time;
    quiet_end := (prefs->>'quiet_hours_end')::time;
    
    -- If no quiet hours set, user can receive notifications
    IF quiet_start IS NULL OR quiet_end IS NULL THEN
        RETURN true;
    END IF;
    
    -- Check if current time is within quiet hours
    IF quiet_start < quiet_end THEN
        -- Same day (e.g., 22:00 to 06:00)
        IF current_time >= quiet_start AND current_time <= quiet_end THEN
            RETURN false;
        END IF;
    ELSE
        -- Overnight (e.g., 22:00 to 06:00)
        IF current_time >= quiet_start OR current_time <= quiet_end THEN
            RETURN false;
        END IF;
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- COMMIT
-- =============================================

COMMIT;
