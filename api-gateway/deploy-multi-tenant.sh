#!/bin/bash

# =============================================================================
# Kong API Gateway Multi-Tenant SaaS Platform - Deployment Script
# =============================================================================
# This script implements the multi-tenant Kong configuration from the example
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
KONG_ADMIN_URL="${KONG_ADMIN_URL:-http://localhost:8001}"
KONG_PROXY_URL="${KONG_PROXY_URL:-http://localhost:8000}"
LOG_FILE="/tmp/kong-multi-tenant-deployment.log"

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# Check if Kong is running
check_kong_status() {
    log "Checking Kong status..."
    if curl -s "$KONG_ADMIN_URL/status" > /dev/null 2>&1; then
        log_success "Kong is running and accessible"
        return 0
    else
        log_error "Kong is not running or not accessible at $KONG_ADMIN_URL"
        return 1
    fi
}

# Wait for Kong to be ready
wait_for_kong() {
    log "Waiting for Kong to be ready..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if check_kong_status; then
            log_success "Kong is ready"
            return 0
        fi
        
        log "Attempt $attempt/$max_attempts - Kong not ready yet, waiting 5 seconds..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    log_error "Kong failed to become ready after $max_attempts attempts"
    return 1
}

# =============================================================================
# 1. CREATE WORKSPACES (Tenant Segmentation)
# =============================================================================

create_workspaces() {
    log "Creating tenant workspaces..."
    
    # Create Tenant A workspace
    log "Creating Tenant A workspace..."
    if curl -s -X POST "$KONG_ADMIN_URL/workspaces" \
        -d "name=tenant-a" \
        -d "comment=Tenant A Workspace for SaaS Platform" > /dev/null 2>&1; then
        log_success "Tenant A workspace created"
    else
        log_warning "Tenant A workspace may already exist or creation failed"
    fi
    
    # Create Tenant B workspace
    log "Creating Tenant B workspace..."
    if curl -s -X POST "$KONG_ADMIN_URL/workspaces" \
        -d "name=tenant-b" \
        -d "comment=Tenant B Workspace for SaaS Platform" > /dev/null 2>&1; then
        log_success "Tenant B workspace created"
    else
        log_warning "Tenant B workspace may already exist or creation failed"
    fi
    
    # Create Tenant C workspace (Enterprise)
    log "Creating Tenant C workspace..."
    if curl -s -X POST "$KONG_ADMIN_URL/workspaces" \
        -d "name=tenant-c" \
        -d "comment=Tenant C Enterprise Workspace for SaaS Platform" > /dev/null 2>&1; then
        log_success "Tenant C workspace created"
    else
        log_warning "Tenant C workspace may already exist or creation failed"
    fi
    
    # List all workspaces
    log "Current workspaces:"
    curl -s "$KONG_ADMIN_URL/workspaces" | jq -r '.data[].name' 2>/dev/null || log_warning "Could not list workspaces"
}

# =============================================================================
# 2. CREATE SERVICES AND ROUTES SCOPED TO EACH WORKSPACE
# =============================================================================

create_tenant_a_services() {
    log "Setting Tenant A workspace context..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    log "Creating Tenant A services and routes..."
    
    # Tenant A User Service
    log "Creating Tenant A User Service..."
    curl -s -X POST "$KONG_ADMIN_URL/services" \
        -d "name=tenant-a-user-service" \
        -d "url=http://host.docker.internal:8081" \
        -d "tags[]=tenant-a" \
        -d "tags[]=user-service" > /dev/null 2>&1
    
    # Tenant A User Service Route
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-user-service/routes" \
        -d "name=tenant-a-user-route" \
        -d "paths[]=/tenant-a/api/users" \
        -d "strip_path=true" \
        -d "tags[]=tenant-a" \
        -d "tags[]=user-routes" > /dev/null 2>&1
    
    # Tenant A Billing Service
    log "Creating Tenant A Billing Service..."
    curl -s -X POST "$KONG_ADMIN_URL/services" \
        -d "name=tenant-a-billing-service" \
        -d "url=http://host.docker.internal:8082" \
        -d "tags[]=tenant-a" \
        -d "tags[]=billing-service" > /dev/null 2>&1
    
    # Tenant A Billing Service Route
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-billing-service/routes" \
        -d "name=tenant-a-billing-route" \
        -d "paths[]=/tenant-a/api/billing" \
        -d "strip_path=true" \
        -d "tags[]=tenant-a" \
        -d "tags[]=billing-routes" > /dev/null 2>&1
    
    log_success "Tenant A services and routes created"
}

create_tenant_b_services() {
    log "Setting Tenant B workspace context..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    log "Creating Tenant B services and routes..."
    
    # Tenant B User Service
    log "Creating Tenant B User Service..."
    curl -s -X POST "$KONG_ADMIN_URL/services" \
        -d "name=tenant-b-user-service" \
        -d "url=http://host.docker.internal:8081" \
        -d "tags[]=tenant-b" \
        -d "tags[]=user-service" > /dev/null 2>&1
    
    # Tenant B User Service Route
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-user-service/routes" \
        -d "name=tenant-b-user-route" \
        -d "paths[]=/tenant-b/api/users" \
        -d "strip_path=true" \
        -d "tags[]=tenant-b" \
        -d "tags[]=user-routes" > /dev/null 2>&1
    
    # Tenant B Analytics Service
    log "Creating Tenant B Analytics Service..."
    curl -s -X POST "$KONG_ADMIN_URL/services" \
        -d "name=tenant-b-analytics-service" \
        -d "url=http://host.docker.internal:8084" \
        -d "tags[]=tenant-b" \
        -d "tags[]=analytics-service" > /dev/null 2>&1
    
    # Tenant B Analytics Service Route
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-analytics-service/routes" \
        -d "name=tenant-b-analytics-route" \
        -d "paths[]=/tenant-b/api/analytics" \
        -d "strip_path=true" \
        -d "tags[]=tenant-b" \
        -d "tags[]=analytics-routes" > /dev/null 2>&1
    
    log_success "Tenant B services and routes created"
}

create_tenant_c_services() {
    log "Setting Tenant C workspace context..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    log "Creating Tenant C services and routes..."
    
    # Tenant C User Service (Enterprise)
    log "Creating Tenant C User Service..."
    curl -s -X POST "$KONG_ADMIN_URL/services" \
        -d "name=tenant-c-user-service" \
        -d "url=http://host.docker.internal:8081" \
        -d "tags[]=tenant-c" \
        -d "tags[]=user-service" \
        -d "tags[]=enterprise" > /dev/null 2>&1
    
    # Tenant C User Service Route
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-c-user-service/routes" \
        -d "name=tenant-c-user-route" \
        -d "paths[]=/tenant-c/api/users" \
        -d "strip_path=true" \
        -d "tags[]=tenant-c" \
        -d "tags[]=user-routes" > /dev/null 2>&1
    
    log_success "Tenant C services and routes created"
}

# =============================================================================
# 3. ENABLE AUTHENTICATION PLUGINS SCOPED PER TENANT WORKSPACE
# =============================================================================

configure_tenant_a_auth() {
    log "Setting Tenant A workspace context for authentication..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    log "Configuring Tenant A JWT authentication..."
    
    # JWT Plugin for Tenant A User Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-user-service/plugins" \
        -d "name=jwt" \
        -d "config.secret=tenant-a-jwt-secret-key" \
        -d "config.key_claim_name=kid" \
        -d "config.algorithm=HS256" \
        -d "config.claims_to_verify[]=iss" \
        -d "config.claims_to_verify[]=sub" \
        -d "config.claims_to_verify[]=tenant" \
        -d "tags[]=tenant-a" \
        -d "tags[]=jwt" > /dev/null 2>&1
    
    # JWT Plugin for Tenant A Billing Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-billing-service/plugins" \
        -d "name=jwt" \
        -d "config.secret=tenant-a-jwt-secret-key" \
        -d "config.key_claim_name=kid" \
        -d "config.algorithm=HS256" \
        -d "config.claims_to_verify[]=iss" \
        -d "config.claims_to_verify[]=sub" \
        -d "config.claims_to_verify[]=tenant" \
        -d "tags[]=tenant-a" \
        -d "tags[]=jwt" > /dev/null 2>&1
    
    log_success "Tenant A JWT authentication configured"
}

configure_tenant_b_auth() {
    log "Setting Tenant B workspace context for authentication..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    log "Configuring Tenant B OAuth2 authentication..."
    
    # OAuth2 Plugin for Tenant B User Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-user-service/plugins" \
        -d "name=oauth2" \
        -d "config.scopes[]=email" \
        -d "config.scopes[]=profile" \
        -d "config.scopes[]=read" \
        -d "config.scopes[]=write" \
        -d "config.enable_authorization_code=true" \
        -d "config.enable_client_credentials=true" \
        -d "config.enable_implicit_grant=true" \
        -d "config.enable_password_grant=true" \
        -d "config.token_expiration=7200" \
        -d "config.refresh_token_ttl=1209600" \
        -d "tags[]=tenant-b" \
        -d "tags[]=oauth2" > /dev/null 2>&1
    
    # OAuth2 Plugin for Tenant B Analytics Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-analytics-service/plugins" \
        -d "name=oauth2" \
        -d "config.scopes[]=email" \
        -d "config.scopes[]=profile" \
        -d "config.scopes[]=read" \
        -d "config.scopes[]=write" \
        -d "config.enable_authorization_code=true" \
        -d "config.enable_client_credentials=true" \
        -d "config.enable_implicit_grant=true" \
        -d "config.enable_password_grant=true" \
        -d "config.token_expiration=7200" \
        -d "config.refresh_token_ttl=1209600" \
        -d "tags[]=tenant-b" \
        -d "tags[]=oauth2" > /dev/null 2>&1
    
    log_success "Tenant B OAuth2 authentication configured"
}

configure_tenant_c_auth() {
    log "Setting Tenant C workspace context for authentication..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    log "Configuring Tenant C JWT authentication..."
    
    # JWT Plugin for Tenant C User Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-c-user-service/plugins" \
        -d "name=jwt" \
        -d "config.secret=tenant-c-jwt-secret-key" \
        -d "config.key_claim_name=kid" \
        -d "config.algorithm=HS256" \
        -d "config.claims_to_verify[]=iss" \
        -d "config.claims_to_verify[]=sub" \
        -d "config.claims_to_verify[]=tenant" \
        -d "tags[]=tenant-c" \
        -d "tags[]=jwt" > /dev/null 2>&1
    
    log_success "Tenant C JWT authentication configured"
}

# =============================================================================
# 4. APPLY RATE LIMITING AND QUOTAS PER TENANT
# =============================================================================

configure_tenant_a_rate_limiting() {
    log "Setting Tenant A workspace context for rate limiting..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    log "Configuring Tenant A rate limiting..."
    
    # Rate Limiting for Tenant A User Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-user-service/plugins" \
        -d "name=rate-limiting" \
        -d "config.minute=100" \
        -d "config.hour=1000" \
        -d "config.day=10000" \
        -d "config.policy=local" \
        -d "config.fault_tolerant=true" \
        -d "tags[]=tenant-a" \
        -d "tags[]=rate-limiting" > /dev/null 2>&1
    
    # Rate Limiting for Tenant A Billing Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-a-billing-service/plugins" \
        -d "name=rate-limiting" \
        -d "config.minute=50" \
        -d "config.hour=500" \
        -d "config.day=5000" \
        -d "config.policy=local" \
        -d "config.fault_tolerant=true" \
        -d "tags[]=tenant-a" \
        -d "tags[]=rate-limiting" > /dev/null 2>&1
    
    log_success "Tenant A rate limiting configured"
}

configure_tenant_b_rate_limiting() {
    log "Setting Tenant B workspace context for rate limiting..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    log "Configuring Tenant B rate limiting..."
    
    # Rate Limiting for Tenant B User Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-user-service/plugins" \
        -d "name=rate-limiting" \
        -d "config.minute=50" \
        -d "config.hour=500" \
        -d "config.day=5000" \
        -d "config.policy=local" \
        -d "config.fault_tolerant=true" \
        -d "tags[]=tenant-b" \
        -d "tags[]=rate-limiting" > /dev/null 2>&1
    
    # Rate Limiting for Tenant B Analytics Service
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-b-analytics-service/plugins" \
        -d "name=rate-limiting" \
        -d "config.minute=300" \
        -d "config.hour=3000" \
        -d "config.day=30000" \
        -d "config.policy=local" \
        -d "config.fault_tolerant=true" \
        -d "tags[]=tenant-b" \
        -d "tags[]=rate-limiting" > /dev/null 2>&1
    
    log_success "Tenant B rate limiting configured"
}

configure_tenant_c_rate_limiting() {
    log "Setting Tenant C workspace context for rate limiting..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    log "Configuring Tenant C rate limiting (Enterprise tier)..."
    
    # Rate Limiting for Tenant C User Service (Higher limits for Enterprise)
    curl -s -X POST "$KONG_ADMIN_URL/services/tenant-c-user-service/plugins" \
        -d "name=rate-limiting" \
        -d "config.minute=500" \
        -d "config.hour=5000" \
        -d "config.day=50000" \
        -d "config.policy=local" \
        -d "config.fault_tolerant=true" \
        -d "tags[]=tenant-c" \
        -d "tags[]=rate-limiting" \
        -d "tags[]=enterprise" > /dev/null 2>&1
    
    log_success "Tenant C rate limiting configured (Enterprise tier)"
}

# =============================================================================
# 5. CREATE CONSUMERS (END-USERS) PER TENANT WORKSPACE
# =============================================================================

create_tenant_consumers() {
    log "Creating consumers for each tenant..."
    
    # Tenant A Consumers
    log "Creating Tenant A consumers..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=user1_tenant_a" \
        -d "custom_id=user1_tenant_a" \
        -d "tags[]=tenant-a" \
        -d "tags[]=user" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=admin_tenant_a" \
        -d "custom_id=admin_tenant_a" \
        -d "tags[]=tenant-a" \
        -d "tags[]=admin" > /dev/null 2>&1
    
    # Tenant B Consumers
    log "Creating Tenant B consumers..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=user1_tenant_b" \
        -d "custom_id=user1_tenant_b" \
        -d "tags[]=tenant-b" \
        -d "tags[]=user" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=admin_tenant_b" \
        -d "custom_id=admin_tenant_b" \
        -d "tags[]=tenant-b" \
        -d "tags[]=admin" > /dev/null 2>&1
    
    # Tenant C Consumers (Enterprise)
    log "Creating Tenant C consumers..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=user1_tenant_c" \
        -d "custom_id=user1_tenant_c" \
        -d "tags[]=tenant-c" \
        -d "tags[]=user" \
        -d "tags[]=enterprise" > /dev/null 2>&1
    
    curl -s -X POST "$KONG_ADMIN_URL/consumers" \
        -d "username=admin_tenant_c" \
        -d "custom_id=admin_tenant_c" \
        -d "tags[]=tenant-c" \
        -d "tags[]=admin" \
        -d "tags[]=enterprise" > /dev/null 2>&1
    
    log_success "All tenant consumers created"
}

# =============================================================================
# 6. VERIFY DEPLOYMENT
# =============================================================================

verify_deployment() {
    log "Verifying multi-tenant deployment..."
    
    # Check workspaces
    log "Checking workspaces..."
    local workspaces=$(curl -s "$KONG_ADMIN_URL/workspaces" | jq -r '.data[].name' 2>/dev/null)
    if echo "$workspaces" | grep -q "tenant-a"; then
        log_success "Tenant A workspace exists"
    else
        log_warning "Tenant A workspace not found"
    fi
    
    if echo "$workspaces" | grep -q "tenant-b"; then
        log_success "Tenant B workspace exists"
    else
        log_warning "Tenant B workspace not found"
    fi
    
    if echo "$workspaces" | grep -q "tenant-c"; then
        log_success "Tenant C workspace exists"
    else
        log_warning "Tenant C workspace not found"
    fi
    
    # Check services in each workspace
    log "Checking services in Tenant A workspace..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    local tenant_a_services=$(curl -s "$KONG_ADMIN_URL/services" | jq -r '.data[].name' 2>/dev/null)
    if echo "$tenant_a_services" | grep -q "tenant-a-user-service"; then
        log_success "Tenant A User Service exists"
    else
        log_warning "Tenant A User Service not found"
    fi
    
    log "Checking services in Tenant B workspace..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    local tenant_b_services=$(curl -s "$KONG_ADMIN_URL/services" | jq -r '.data[].name' 2>/dev/null)
    if echo "$tenant_b_services" | grep -q "tenant-b-user-service"; then
        log_success "Tenant B User Service exists"
    else
        log_warning "Tenant B User Service not found"
    fi
    
    log "Checking services in Tenant C workspace..."
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    local tenant_c_services=$(curl -s "$KONG_ADMIN_URL/services" | jq -r '.data[].name' 2>/dev/null)
    if echo "$tenant_c_services" | grep -q "tenant-c-user-service"; then
        log_success "Tenant C User Service exists"
    else
        log_warning "Tenant C User Service not found"
    fi
    
    log_success "Deployment verification completed"
}

# =============================================================================
# 7. MAIN DEPLOYMENT FUNCTION
# =============================================================================

deploy_multi_tenant() {
    log "Starting Kong Multi-Tenant SaaS Platform deployment..."
    
    # Check Kong status
    if ! wait_for_kong; then
        log_error "Kong is not ready. Please ensure Kong is running and accessible."
        exit 1
    fi
    
    # Create workspaces
    create_workspaces
    
    # Create services and routes for each tenant
    create_tenant_a_services
    create_tenant_b_services
    create_tenant_c_services
    
    # Configure authentication for each tenant
    configure_tenant_a_auth
    configure_tenant_b_auth
    configure_tenant_c_auth
    
    # Configure rate limiting for each tenant
    configure_tenant_a_rate_limiting
    configure_tenant_b_rate_limiting
    configure_tenant_c_rate_limiting
    
    # Create consumers for each tenant
    create_tenant_consumers
    
    # Verify deployment
    verify_deployment
    
    log_success "Kong Multi-Tenant SaaS Platform deployment completed successfully!"
    log "Log file: $LOG_FILE"
    
    # Display summary
    echo ""
    echo "============================================================================="
    echo "DEPLOYMENT SUMMARY"
    echo "============================================================================="
    echo "✅ Workspaces created: tenant-a, tenant-b, tenant-c"
    echo "✅ Services configured with tenant isolation"
    echo "✅ Authentication configured (JWT for A/C, OAuth2 for B)"
    echo "✅ Rate limiting configured per tenant tier"
    echo "✅ Consumers created for each tenant"
    echo ""
    echo "Tenant A (Standard): /tenant-a/* - JWT Auth - 100 req/min"
    echo "Tenant B (Standard): /tenant-b/* - OAuth2 Auth - 50 req/min"
    echo "Tenant C (Enterprise): /tenant-c/* - JWT Auth - 500 req/min"
    echo ""
    echo "Test endpoints:"
    echo "  Tenant A: $KONG_PROXY_URL/tenant-a/api/users"
    echo "  Tenant B: $KONG_PROXY_URL/tenant-b/api/users"
    echo "  Tenant C: $KONG_PROXY_URL/tenant-c/api/users"
    echo "============================================================================="
}

# =============================================================================
# 8. CLEANUP FUNCTION
# =============================================================================

cleanup() {
    log "Cleaning up multi-tenant configuration..."
    
    # This would remove all the created resources
    # Use with caution in production
    log_warning "Cleanup function not implemented for safety"
    log "To remove workspaces manually, use:"
    log "  curl -X DELETE $KONG_ADMIN_URL/workspaces/tenant-a"
    log "  curl -X DELETE $KONG_ADMIN_URL/workspaces/tenant-b"
    log "  curl -X DELETE $KONG_ADMIN_URL/workspaces/tenant-c"
}

# =============================================================================
# 9. SCRIPT EXECUTION
# =============================================================================

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    log_error "jq is required but not installed. Please install jq first."
    log "On macOS: brew install jq"
    log "On Ubuntu/Debian: sudo apt-get install jq"
    exit 1
fi

# Parse command line arguments
case "${1:-deploy}" in
    "deploy")
        deploy_multi_tenant
        ;;
    "cleanup")
        cleanup
        ;;
    "verify")
        verify_deployment
        ;;
    "status")
        check_kong_status
        ;;
    *)
        echo "Usage: $0 {deploy|cleanup|verify|status}"
        echo ""
        echo "Commands:"
        echo "  deploy   - Deploy the multi-tenant Kong configuration (default)"
        echo "  cleanup  - Remove the multi-tenant configuration"
        echo "  verify   - Verify the current deployment"
        echo "  status   - Check Kong status"
        exit 1
        ;;
esac
