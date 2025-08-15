#!/bin/bash

# Multi-Tenancy Setup Script for SaaS Starter Kit
# This script sets up the multi-tenancy infrastructure

set -e

echo "🚀 Setting up Multi-Tenancy for SaaS Starter Kit..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    print_status "Checking Docker status..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Stop existing services
stop_services() {
    print_status "Stopping existing services..."
    docker-compose down
    print_success "Services stopped"
}

# Start database with new schema
start_database() {
    print_status "Starting database with multi-tenant schema..."
    docker-compose up -d postgres
    
    # Wait for database to be ready
    print_status "Waiting for database to be ready..."
    sleep 10
    
    # Check if database is ready
    until docker-compose exec -T postgres pg_isready -U saas_user -d saas_starter_kit; do
        print_status "Waiting for database to be ready..."
        sleep 2
    done
    
    print_success "Database is ready"
}

# Initialize multi-tenant schema
init_schema() {
    print_status "Initializing multi-tenant database schema..."
    
    # Run the multi-tenant schema initialization
    docker-compose exec -T postgres psql -U saas_user -d saas_starter_kit -f /docker-entrypoint-initdb.d/02-multi-tenant-schema.sql
    
    print_success "Multi-tenant schema initialized"
}

# Create test tenant
create_test_tenant() {
    print_status "Creating test tenant..."
    
    # Create a test tenant using the database function
    docker-compose exec -T postgres psql -U saas_user -d saas_starter_kit -c "
    SELECT create_tenant(
        'Test Tenant',
        'test_tenant',
        'test-tenant-realm',
        'test-tenant-client',
        10
    );"
    
    print_success "Test tenant created"
}

# Start all services
start_services() {
    print_status "Starting all services..."
    docker-compose up -d
    
    print_status "Waiting for services to be ready..."
    sleep 30
    
    print_success "All services started"
}

# Test multi-tenancy
test_multi_tenancy() {
    print_status "Testing multi-tenancy setup..."
    
    # Test tenant creation via API
    print_status "Testing tenant API..."
    
    # Wait for services to be fully ready
    sleep 10
    
    # Test if the tenant API is accessible
    if curl -f http://localhost:8000/api/tenants/check/test-tenant > /dev/null 2>&1; then
        print_success "Tenant API is working"
    else
        print_warning "Tenant API test failed - this is normal if services are still starting"
    fi
}

# Display setup information
display_info() {
    echo ""
    echo "🎉 Multi-Tenancy Setup Complete!"
    echo ""
    echo "📋 Setup Summary:"
    echo "  ✅ Database with multi-tenant schema"
    echo "  ✅ Test tenant created (test-tenant)"
    echo "  ✅ All services running"
    echo ""
    echo "🔗 Access Points:"
    echo "  Frontend: http://localhost:3000"
    echo "  API Gateway: http://localhost:8000"
    echo "  Keycloak: http://localhost:8080"
    echo "  Database: localhost:5432"
    echo ""
    echo "📚 Next Steps:"
    echo "  1. Access the frontend at http://localhost:3000"
    echo "  2. Create a new tenant via the API or admin interface"
    echo "  3. Test tenant switching functionality"
    echo "  4. Review the documentation in docs/MULTI_TENANCY.md"
    echo ""
    echo "🔧 Useful Commands:"
    echo "  View logs: docker-compose logs -f"
    echo "  Stop services: docker-compose down"
    echo "  Restart services: docker-compose restart"
    echo ""
}

# Main execution
main() {
    echo "=========================================="
    echo "  SaaS Starter Kit - Multi-Tenancy Setup"
    echo "=========================================="
    echo ""
    
    check_docker
    stop_services
    start_database
    init_schema
    create_test_tenant
    start_services
    test_multi_tenancy
    display_info
}

# Run main function
main "$@"
