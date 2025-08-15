#!/bin/bash

# =============================================================================
# Kong Multi-Tenant Configuration Test Script
# =============================================================================
# This script tests the multi-tenant Kong configuration
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
LOG_FILE="/tmp/kong-multi-tenant-test.log"

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

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Test function
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_result="$3"
    
    log "Running test: $test_name"
    
    if eval "$test_command" > /dev/null 2>&1; then
        log_success "Test passed: $test_name"
        ((TESTS_PASSED++))
        return 0
    else
        log_error "Test failed: $test_name"
        ((TESTS_FAILED++))
        return 1
    fi
}

# =============================================================================
# 1. BASIC CONNECTIVITY TESTS
# =============================================================================

test_kong_connectivity() {
    log "Testing Kong connectivity..."
    
    # Test Kong Admin API
    run_test "Kong Admin API accessible" \
        "curl -s '$KONG_ADMIN_URL/status' > /dev/null" \
        "Kong Admin API should be accessible"
    
    # Test Kong Proxy
    run_test "Kong Proxy accessible" \
        "curl -s '$KONG_PROXY_URL/' > /dev/null" \
        "Kong Proxy should be accessible"
}

# =============================================================================
# 2. WORKSPACE TESTS
# =============================================================================

test_workspaces() {
    log "Testing workspace configuration..."
    
    # Test Tenant A workspace
    run_test "Tenant A workspace exists" \
        "curl -s '$KONG_ADMIN_URL/workspaces' | jq -e '.data[] | select(.name == \"tenant-a\")' > /dev/null" \
        "Tenant A workspace should exist"
    
    # Test Tenant B workspace
    run_test "Tenant B workspace exists" \
        "curl -s '$KONG_ADMIN_URL/workspaces' | jq -e '.data[] | select(.name == \"tenant-b\")' > /dev/null" \
        "Tenant B workspace should exist"
    
    # Test Tenant C workspace
    run_test "Tenant C workspace exists" \
        "curl -s '$KONG_ADMIN_URL/workspaces' | jq -e '.data[] | select(.name == \"tenant-c\")' > /dev/null" \
        "Tenant C workspace should exist"
}

# =============================================================================
# 3. SERVICE TESTS
# =============================================================================

test_tenant_a_services() {
    log "Testing Tenant A services..."
    
    # Switch to Tenant A workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    # Test User Service
    run_test "Tenant A User Service exists" \
        "curl -s '$KONG_ADMIN_URL/services' | jq -e '.data[] | select(.name == \"tenant-a-user-service\")' > /dev/null" \
        "Tenant A User Service should exist"
    
    # Test Billing Service
    run_test "Tenant A Billing Service exists" \
        "curl -s '$KONG_ADMIN_URL/services' | jq -e '.data[] | select(.name == \"tenant-a-billing-service\")' > /dev/null" \
        "Tenant A Billing Service should exist"
}

test_tenant_b_services() {
    log "Testing Tenant B services..."
    
    # Switch to Tenant B workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    # Test User Service
    run_test "Tenant B User Service exists" \
        "curl -s '$KONG_ADMIN_URL/services' | jq -e '.data[] | select(.name == \"tenant-b-user-service\")' > /dev/null" \
        "Tenant B User Service should exist"
    
    # Test Analytics Service
    run_test "Tenant B Analytics Service exists" \
        "curl -s '$KONG_ADMIN_URL/services' | jq -e '.data[] | select(.name == \"tenant-b-analytics-service\")' > /dev/null" \
        "Tenant B Analytics Service should exist"
}

test_tenant_c_services() {
    log "Testing Tenant C services..."
    
    # Switch to Tenant C workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    # Test User Service
    run_test "Tenant C User Service exists" \
        "curl -s '$KONG_ADMIN_URL/services' | jq -e '.data[] | select(.name == \"tenant-c-user-service\")' > /dev/null" \
        "Tenant C User Service should exist"
}

# =============================================================================
# 4. ROUTE TESTS
# =============================================================================

test_tenant_a_routes() {
    log "Testing Tenant A routes..."
    
    # Switch to Tenant A workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    # Test User Route
    run_test "Tenant A User Route exists" \
        "curl -s '$KONG_ADMIN_URL/routes' | jq -e '.data[] | select(.name == \"tenant-a-user-route\")' > /dev/null" \
        "Tenant A User Route should exist"
    
    # Test Billing Route
    run_test "Tenant A Billing Route exists" \
        "curl -s '$KONG_ADMIN_URL/routes' | jq -e '.data[] | select(.name == \"tenant-a-billing-route\")' > /dev/null" \
        "Tenant A Billing Route should exist"
}

test_tenant_b_routes() {
    log "Testing Tenant B routes..."
    
    # Switch to Tenant B workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    # Test User Route
    run_test "Tenant B User Route exists" \
        "curl -s '$KONG_ADMIN_URL/routes' | jq -e '.data[] | select(.name == \"tenant-b-user-route\")' > /dev/null" \
        "Tenant B User Route should exist"
    
    # Test Analytics Route
    run_test "Tenant B Analytics Route exists" \
        "curl -s '$KONG_ADMIN_URL/routes' | jq -e '.data[] | select(.name == \"tenant-b-analytics-route\")' > /dev/null" \
        "Tenant B Analytics Route should exist"
}

test_tenant_c_routes() {
    log "Testing Tenant C routes..."
    
    # Switch to Tenant C workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    # Test User Route
    run_test "Tenant C User Route exists" \
        "curl -s '$KONG_ADMIN_URL/routes' | jq -e '.data[] | select(.name == \"tenant-c-user-route\")' > /dev/null" \
        "Tenant C User Route should exist"
}

# =============================================================================
# 5. PLUGIN TESTS
# =============================================================================

test_tenant_a_plugins() {
    log "Testing Tenant A plugins..."
    
    # Switch to Tenant A workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    
    # Test JWT Plugin on User Service
    run_test "Tenant A User Service has JWT plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-a-user-service/plugins' | jq -e '.data[] | select(.name == \"jwt\")' > /dev/null" \
        "Tenant A User Service should have JWT plugin"
    
    # Test Rate Limiting Plugin on User Service
    run_test "Tenant A User Service has Rate Limiting plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-a-user-service/plugins' | jq -e '.data[] | select(.name == \"rate-limiting\")' > /dev/null" \
        "Tenant A User Service should have Rate Limiting plugin"
}

test_tenant_b_plugins() {
    log "Testing Tenant B plugins..."
    
    # Switch to Tenant B workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    
    # Test OAuth2 Plugin on User Service
    run_test "Tenant B User Service has OAuth2 plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-b-user-service/plugins' | jq -e '.data[] | select(.name == \"oauth2\")' > /dev/null" \
        "Tenant B User Service should have OAuth2 plugin"
    
    # Test Rate Limiting Plugin on User Service
    run_test "Tenant B User Service has Rate Limiting plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-b-user-service/plugins' | jq -e '.data[] | select(.name == \"rate-limiting\")' > /dev/null" \
        "Tenant B User Service should have Rate Limiting plugin"
}

test_tenant_c_plugins() {
    log "Testing Tenant C plugins..."
    
    # Switch to Tenant C workspace
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    
    # Test JWT Plugin on User Service
    run_test "Tenant C User Service has JWT plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-c-user-service/plugins' | jq -e '.data[] | select(.name == \"jwt\")' > /dev/null" \
        "Tenant C User Service should have JWT plugin"
    
    # Test Rate Limiting Plugin on User Service
    run_test "Tenant C User Service has Rate Limiting plugin" \
        "curl -s '$KONG_ADMIN_URL/services/tenant-c-user-service/plugins' | jq -e '.data[] | select(.name == \"rate-limiting\")' > /dev/null" \
        "Tenant C User Service should have Rate Limiting plugin"
}

# =============================================================================
# 6. CONSUMER TESTS
# =============================================================================

test_consumers() {
    log "Testing consumer configuration..."
    
    # Test Tenant A consumers
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-a" > /dev/null 2>&1
    run_test "Tenant A has consumers" \
        "curl -s '$KONG_ADMIN_URL/consumers' | jq -e '.data | length > 0' > /dev/null" \
        "Tenant A should have consumers"
    
    # Test Tenant B consumers
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-b" > /dev/null 2>&1
    run_test "Tenant B has consumers" \
        "curl -s '$KONG_ADMIN_URL/consumers' | jq -e '.data | length > 0' > /dev/null" \
        "Tenant B should have consumers"
    
    # Test Tenant C consumers
    curl -s -X POST "$KONG_ADMIN_URL/workspaces/tenant-c" > /dev/null 2>&1
    run_test "Tenant C has consumers" \
        "curl -s '$KONG_ADMIN_URL/consumers' | jq -e '.data | length > 0' > /dev/null" \
        "Tenant C should have consumers"
}

# =============================================================================
# 7. ENDPOINT ACCESS TESTS
# =============================================================================

test_endpoint_access() {
    log "Testing endpoint access..."
    
    # Test Tenant A endpoint (should require authentication)
    run_test "Tenant A endpoint accessible (requires auth)" \
        "curl -s -o /dev/null -w '%{http_code}' '$KONG_PROXY_URL/tenant-a/api/users' | grep -q '401\|403'" \
        "Tenant A endpoint should require authentication"
    
    # Test Tenant B endpoint (should require authentication)
    run_test "Tenant B endpoint accessible (requires auth)" \
        "curl -s -o /dev/null -w '%{http_code}' '$KONG_PROXY_URL/tenant-b/api/users' | grep -q '401\|403'" \
        "Tenant B endpoint should require authentication"
    
    # Test Tenant C endpoint (should require authentication)
    run_test "Tenant C endpoint accessible (requires auth)" \
        "curl -s -o /dev/null -w '%{http_code}' '$KONG_PROXY_URL/tenant-c/api/users' | grep -q '401\|403'" \
        "Tenant C endpoint should require authentication"
    
    # Test invalid tenant endpoint (should be blocked)
    run_test "Invalid tenant endpoint blocked" \
        "curl -s -o /dev/null -w '%{http_code}' '$KONG_PROXY_URL/invalid-tenant/api/users' | grep -q '404\|400'" \
        "Invalid tenant endpoint should be blocked"
}

# =============================================================================
# 8. RATE LIMITING TESTS
# =============================================================================

test_rate_limiting() {
    log "Testing rate limiting configuration..."
    
    # This is a basic test - in a real scenario you'd test actual rate limiting
    # by making multiple requests and checking headers
    
    log_warning "Rate limiting tests are basic - actual rate limiting behavior requires multiple requests"
    
    # Test that rate limiting plugins are configured
    run_test "Rate limiting plugins configured" \
        "curl -s '$KONG_ADMIN_URL/plugins' | jq -e '.data[] | select(.name == \"rate-limiting\")' > /dev/null" \
        "Rate limiting plugins should be configured"
}

# =============================================================================
# 9. MAIN TEST FUNCTION
# =============================================================================

run_all_tests() {
    log "Starting Kong Multi-Tenant Configuration Tests..."
    echo ""
    
    # Reset test counters
    TESTS_PASSED=0
    TESTS_FAILED=0
    
    # Run all test suites
    test_kong_connectivity
    echo ""
    
    test_workspaces
    echo ""
    
    test_tenant_a_services
    test_tenant_b_services
    test_tenant_c_services
    echo ""
    
    test_tenant_a_routes
    test_tenant_b_routes
    test_tenant_c_routes
    echo ""
    
    test_tenant_a_plugins
    test_tenant_b_plugins
    test_tenant_c_plugins
    echo ""
    
    test_consumers
    echo ""
    
    test_endpoint_access
    echo ""
    
    test_rate_limiting
    echo ""
    
    # Display test results
    echo "============================================================================="
    echo "TEST RESULTS SUMMARY"
    echo "============================================================================="
    echo "✅ Tests Passed: $TESTS_PASSED"
    echo "❌ Tests Failed: $TESTS_FAILED"
    echo "📊 Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
    echo ""
    
    if [ $TESTS_FAILED -eq 0 ]; then
        log_success "All tests passed! Multi-tenant configuration is working correctly."
        echo ""
        echo "🎉 Your Kong Multi-Tenant SaaS Platform is ready!"
        echo ""
        echo "Test endpoints:"
        echo "  Tenant A: $KONG_PROXY_URL/tenant-a/api/users"
        echo "  Tenant B: $KONG_PROXY_URL/tenant-b/api/users"
        echo "  Tenant C: $KONG_PROXY_URL/tenant-c/api/users"
        echo ""
        echo "Note: All endpoints require proper authentication tokens."
    else
        log_error "Some tests failed. Please check the configuration and try again."
        echo ""
        echo "🔧 Troubleshooting tips:"
        echo "  1. Ensure Kong is running and accessible"
        echo "  2. Check that the multi-tenant configuration was deployed"
        echo "  3. Verify all services are running"
        echo "  4. Check Kong logs for errors"
    fi
    
    echo "============================================================================="
    echo "Log file: $LOG_FILE"
    echo "============================================================================="
}

# =============================================================================
# 10. SCRIPT EXECUTION
# =============================================================================

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    log_error "jq is required but not installed. Please install jq first."
    log "On macOS: brew install jq"
    log "On Ubuntu/Debian: sudo apt-get install jq"
    exit 1
fi

# Check if curl is installed
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed."
    exit 1
fi

# Parse command line arguments
case "${1:-all}" in
    "all")
        run_all_tests
        ;;
    "connectivity")
        test_kong_connectivity
        ;;
    "workspaces")
        test_workspaces
        ;;
    "services")
        test_tenant_a_services
        test_tenant_b_services
        test_tenant_c_services
        ;;
    "routes")
        test_tenant_a_routes
        test_tenant_b_routes
        test_tenant_c_routes
        ;;
    "plugins")
        test_tenant_a_plugins
        test_tenant_b_plugins
        test_tenant_c_plugins
        ;;
    "consumers")
        test_consumers
        ;;
    "endpoints")
        test_endpoint_access
        ;;
    "rate-limiting")
        test_rate_limiting
        ;;
    *)
        echo "Usage: $0 {all|connectivity|workspaces|services|routes|plugins|consumers|endpoints|rate-limiting}"
        echo ""
        echo "Test Categories:"
        echo "  all           - Run all tests (default)"
        echo "  connectivity  - Test Kong connectivity"
        echo "  workspaces    - Test workspace configuration"
        echo "  services      - Test service configuration"
        echo "  routes        - Test route configuration"
        echo "  plugins       - Test plugin configuration"
        echo "  consumers     - Test consumer configuration"
        echo "  endpoints     - Test endpoint access"
        echo "  rate-limiting - Test rate limiting configuration"
        exit 1
        ;;
esac
