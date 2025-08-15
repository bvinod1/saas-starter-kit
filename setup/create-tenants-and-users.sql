-- Create Tenants and Users Script
-- This script creates two tenants with admin and normal users for each

-- 1. Create Acme Corporation Tenant
SELECT create_tenant(
    'Acme Corporation',
    'acme-corp',
    'acme-corp-realm',
    'acme-corp-client',
    25
);

-- 2. Create TechStart Inc Tenant
SELECT create_tenant(
    'TechStart Inc',
    'techstart-inc',
    'techstart-inc-realm',
    'techstart-inc-client',
    15
);

-- 3. Create users in Acme Corporation tenant
-- Admin user for Acme
INSERT INTO tenant_acme_corp.users (
    keycloak_user_id,
    email,
    first_name,
    last_name,
    status
) VALUES (
    'acme-admin-123',
    'admin@acme.com',
    'John',
    'Admin',
    'active'
);

-- Normal user for Acme
INSERT INTO tenant_acme_corp.users (
    keycloak_user_id,
    email,
    first_name,
    last_name,
    status
) VALUES (
    'acme-user-456',
    'user@acme.com',
    'Jane',
    'User',
    'active'
);

-- 4. Create users in TechStart Inc tenant
-- Admin user for TechStart
INSERT INTO tenant_techstart_inc.users (
    keycloak_user_id,
    email,
    first_name,
    last_name,
    status
) VALUES (
    'techstart-admin-789',
    'admin@techstart.com',
    'Mike',
    'Manager',
    'active'
);

-- Normal user for TechStart
INSERT INTO tenant_techstart_inc.users (
    keycloak_user_id,
    email,
    first_name,
    last_name,
    status
) VALUES (
    'techstart-user-012',
    'user@techstart.com',
    'Sarah',
    'Developer',
    'active'
);

-- 5. Create user roles for Acme Corporation
INSERT INTO tenant_acme_corp.user_roles (user_id, role_name)
SELECT u.id, 'admin'
FROM tenant_acme_corp.users u
WHERE u.keycloak_user_id = 'acme-admin-123';

INSERT INTO tenant_acme_corp.user_roles (user_id, role_name)
SELECT u.id, 'user'
FROM tenant_acme_corp.users u
WHERE u.keycloak_user_id = 'acme-user-456';

-- 6. Create user roles for TechStart Inc
INSERT INTO tenant_techstart_inc.user_roles (user_id, role_name)
SELECT u.id, 'admin'
FROM tenant_techstart_inc.users u
WHERE u.keycloak_user_id = 'techstart-admin-789';

INSERT INTO tenant_techstart_inc.user_roles (user_id, role_name)
SELECT u.id, 'user'
FROM tenant_techstart_inc.users u
WHERE u.keycloak_user_id = 'techstart-user-012';

-- 7. Create organizations for each tenant
-- Acme Corporation organization
INSERT INTO tenant_acme_corp.organizations (name, slug, description)
VALUES ('Acme Corporation', 'acme-corp', 'Main organization for Acme Corporation');

-- TechStart Inc organization
INSERT INTO tenant_techstart_inc.organizations (name, slug, description)
VALUES ('TechStart Inc', 'techstart-inc', 'Main organization for TechStart Inc');

-- 8. Add users to organizations
-- Acme users to Acme organization
INSERT INTO tenant_acme_corp.organization_members (organization_id, user_id, role)
SELECT 
    o.id,
    u.id,
    CASE 
        WHEN u.keycloak_user_id = 'acme-admin-123' THEN 'admin'
        ELSE 'member'
    END
FROM tenant_acme_corp.organizations o
CROSS JOIN tenant_acme_corp.users u
WHERE o.slug = 'acme-corp';

-- TechStart users to TechStart organization
INSERT INTO tenant_techstart_inc.organization_members (organization_id, user_id, role)
SELECT 
    o.id,
    u.id,
    CASE 
        WHEN u.keycloak_user_id = 'techstart-admin-789' THEN 'admin'
        ELSE 'member'
    END
FROM tenant_techstart_inc.organizations o
CROSS JOIN tenant_techstart_inc.users u
WHERE o.slug = 'techstart-inc';

-- 9. Register users in tenant_management.tenant_users
INSERT INTO tenant_management.tenant_users (tenant_id, keycloak_user_id, email, first_name, last_name, roles)
SELECT 
    t.id,
    u.keycloak_user_id,
    u.email,
    u.first_name,
    u.last_name,
    jsonb_build_array(
        CASE 
            WHEN u.keycloak_user_id LIKE '%admin%' THEN 'admin'
            ELSE 'user'
        END
    )
FROM tenant_management.tenants t
CROSS JOIN (
    SELECT 'acme-admin-123' as keycloak_user_id, 'admin@acme.com' as email, 'John' as first_name, 'Admin' as last_name
    UNION ALL
    SELECT 'acme-user-456', 'user@acme.com', 'Jane', 'User'
    UNION ALL
    SELECT 'techstart-admin-789', 'admin@techstart.com', 'Mike', 'Manager'
    UNION ALL
    SELECT 'techstart-user-012', 'user@techstart.com', 'Sarah', 'Developer'
) u
WHERE t.slug IN ('acme-corp', 'techstart-inc');

-- Display the created tenants and users
SELECT 'Tenants Created:' as info;
SELECT name, slug, status FROM tenant_management.tenants WHERE status = 'active';

SELECT 'Acme Corporation Users:' as info;
SELECT first_name, last_name, email, status FROM tenant_acme_corp.users;

SELECT 'TechStart Inc Users:' as info;
SELECT first_name, last_name, email, status FROM tenant_techstart_inc.users;
