# Quick Start Guide: Multi-Tenancy Implementation

This guide will help you quickly set up and test the multi-tenancy implementation in your SaaS Starter Kit.

## 🚀 Quick Setup

### 1. Run the Setup Script

```bash
# Make sure you're in the project root directory
cd /path/to/SaaS_Starter_Kit

# Run the multi-tenancy setup script
./setup-multi-tenancy.sh
```

This script will:
- ✅ Stop existing services
- ✅ Start the database with multi-tenant schema
- ✅ Initialize the multi-tenant database structure
- ✅ Create a test tenant
- ✅ Start all services
- ✅ Test the setup

### 2. Verify the Setup

After running the setup script, you should see:

```
🎉 Multi-Tenancy Setup Complete!

📋 Setup Summary:
  ✅ Database with multi-tenant schema
  ✅ Test tenant created (test-tenant)
  ✅ All services running

🔗 Access Points:
  Frontend: http://localhost:3000
  API Gateway: http://localhost:8000
  Keycloak: http://localhost:8080
  Database: localhost:5432
```

## 🧪 Testing Multi-Tenancy

### 1. Test Tenant API

```bash
# Check if test tenant exists
curl http://localhost:8000/api/tenants/check/test-tenant

# Expected response:
{
  "success": true,
  "data": {
    "exists": true,
    "active": true
  },
  "message": "Tenant check completed"
}
```

### 2. Create a New Tenant

```bash
# Create a new tenant (requires super admin role)
curl -X POST http://localhost:8000/api/tenants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Acme Corporation",
    "slug": "acme-corp",
    "domain": "acme.example.com",
    "keycloakRealmId": "acme-realm",
    "keycloakClientId": "acme-client"
  }'
```

### 3. Test Tenant-Specific Data

```bash
# Get organizations for current tenant
curl http://localhost:8000/api/organizations \
  -H "Authorization: Bearer YOUR_TOKEN"

# Expected response:
{
  "success": true,
  "data": [
    {
      "id": "...",
      "name": "Default Organization",
      "slug": "default",
      "description": "Default organization for this tenant"
    }
  ],
  "tenant": "test-tenant",
  "message": "Organizations retrieved successfully"
}
```

## 🔧 Manual Setup (Alternative)

If you prefer to set up manually:

### 1. Start Database

```bash
docker-compose up -d postgres
```

### 2. Initialize Schema

```bash
# Wait for database to be ready, then run:
docker-compose exec postgres psql -U saas_user -d saas_starter_kit -f /docker-entrypoint-initdb.d/02-multi-tenant-schema.sql
```

### 3. Create Test Tenant

```bash
docker-compose exec postgres psql -U saas_user -d saas_starter_kit -c "
SELECT create_tenant(
    'Test Tenant',
    'test-tenant',
    'test-tenant-realm',
    'test-tenant-client',
    10
);"
```

### 4. Start All Services

```bash
docker-compose up -d
```

## 🎯 Key Features to Test

### 1. Tenant Isolation

Each tenant has its own database schema:
- `tenant_test-tenant` - Test tenant data
- `tenant_acme-corp` - Acme Corp tenant data
- `shared` - System-wide shared data
- `tenant_management` - Tenant registry

### 2. Tenant Context

The system automatically detects the current tenant from JWT tokens:
- `tenant_id` claim in JWT
- `tenant_slug` claim in JWT
- Automatic schema switching

### 3. Frontend Tenant Switching

- Tenant selector in the top navigation
- Automatic tenant context switching
- Tenant-aware API calls

### 4. API Endpoints

Multi-tenant aware endpoints:
- `/api/users/profile` - Current user in tenant context
- `/api/users/tenants` - User's accessible tenants
- `/api/organizations` - Tenant-specific organizations
- `/api/tenants/*` - Tenant management

## 🔍 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check database status
   docker-compose logs postgres
   
   # Restart database
   docker-compose restart postgres
   ```

2. **Schema Not Found**
   ```bash
   # Check if tenant schema exists
   docker-compose exec postgres psql -U saas_user -d saas_starter_kit -c "
   SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%';"
   ```

3. **Tenant Context Not Set**
   ```bash
   # Check JWT token claims
   # Ensure tenant_id or tenant_slug is present in the token
   ```

4. **Services Not Starting**
   ```bash
   # Check all service logs
   docker-compose logs
   
   # Restart all services
   docker-compose down && docker-compose up -d
   ```

### Debug Commands

```bash
# Check tenant schemas
docker-compose exec postgres psql -U saas_user -d saas_starter_kit -c "
SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%';"

# Check tenant registry
docker-compose exec postgres psql -U saas_user -d saas_starter_kit -c "
SELECT * FROM tenant_management.tenants WHERE status = 'active';"

# Check tenant data
docker-compose exec postgres psql -U saas_user -d saas_starter_kit -c "
SELECT * FROM tenant_test-tenant.organizations;"
```

## 📚 Next Steps

1. **Review Documentation**: Read `docs/MULTI_TENANCY.md` for detailed implementation
2. **Customize Tenants**: Create tenant-specific configurations
3. **Add Features**: Implement tenant-specific features
4. **Security**: Review and enhance security measures
5. **Monitoring**: Set up tenant-specific monitoring

## 🆘 Support

If you encounter issues:

1. Check the logs: `docker-compose logs -f`
2. Review the documentation: `docs/MULTI_TENANCY.md`
3. Verify the setup: Run the setup script again
4. Check the troubleshooting section above

---

**Happy Multi-Tenant Development! 🎉**
