#!/bin/bash

# SaaS Starter Kit Setup Script
# This script sets up the entire SaaS Starter Kit project

set -e

echo "🚀 Setting up SaaS Starter Kit..."

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

# Check if required tools are installed
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 18+ first."
        exit 1
    fi
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17+ first."
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    print_success "All prerequisites are installed!"
}

# Create environment file
create_env_file() {
    print_status "Creating environment file..."
    
    if [ ! -f .env ]; then
        cat > .env << EOF
# Database Configuration
POSTGRES_DB=saas_starter_kit
POSTGRES_USER=saas_user
POSTGRES_PASSWORD=saas_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-that-should-be-at-least-256-bits-long

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
MICROSOFT_CLIENT_ID=your-microsoft-client-id
MICROSOFT_CLIENT_SECRET=your-microsoft-client-secret

# Payment Gateway Configuration
STRIPE_SECRET_KEY=your-stripe-secret-key
STRIPE_PUBLISHABLE_KEY=your-stripe-publishable-key


# SMS Configuration
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=your-twilio-phone-number

# Monitoring Configuration
PROMETHEUS_RETENTION_DAYS=15
GRAFANA_ADMIN_PASSWORD=admin
EOF
        print_success "Environment file created!"
        print_warning "Please update the .env file with your actual configuration values."
    else
        print_status "Environment file already exists."
    fi
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    # Start PostgreSQL, Redis, Elasticsearch, etc.
    docker-compose up -d postgres redis elasticsearch logstash kibana prometheus grafana alertmanager kong kong-database
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Run Kong migration
    print_status "Running Kong database migration..."
    docker-compose run --rm kong kong migrations bootstrap
    
    print_success "Infrastructure services started!"
}

# Setup database
setup_database() {
    print_status "Setting up database..."
    
    # Wait for PostgreSQL to be ready
    print_status "Waiting for PostgreSQL to be ready..."
    until docker-compose exec postgres pg_isready -U saas_user -d saas_starter_kit; do
        sleep 2
    done
    
    # The database schema will be automatically created by the init script
    print_success "Database setup completed!"
}

# Build and start backend services
build_backend_services() {
    print_status "Building backend services..."
    
    # Build auth service
    print_status "Building auth service..."
    cd backend/auth-service
    mvn clean package -DskipTests
    cd ../..
    
    # Build other services (you can add more services here)
    # cd backend/user-service
    # mvn clean package -DskipTests
    # cd ../..
    
    print_success "Backend services built!"
}

# Setup frontend
setup_frontend() {
    print_status "Setting up frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing frontend dependencies..."
    npm install
    
    # Create .env file for frontend
    if [ ! -f .env ]; then
        cat > .env << EOF
REACT_APP_API_URL=http://localhost:8000
REACT_APP_WS_URL=ws://localhost:8000
REACT_APP_STRIPE_PUBLISHABLE_KEY=your-stripe-publishable-key
EOF
        print_warning "Please update the frontend .env file with your actual configuration values."
    fi
    
    cd ..
    
    print_success "Frontend setup completed!"
}

# Create startup scripts
create_startup_scripts() {
    print_status "Creating startup scripts..."
    
    # Create start-all.sh
    cat > start-all.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting SaaS Starter Kit..."

# Start infrastructure
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Start backend services (in background)
echo "🔧 Starting backend services..."
cd backend/auth-service
mvn spring-boot:run &
cd ../..

# Start frontend
echo "🎨 Starting frontend..."
cd frontend
npm start &
cd ..

echo "✅ SaaS Starter Kit is starting up!"
echo "📊 Grafana: http://localhost:3000 (admin/admin)"
echo "📈 Prometheus: http://localhost:9090"
echo "🔍 Kibana: http://localhost:5601"
echo "🌐 Kong Admin: http://localhost:8002"
echo "🎨 Frontend: http://localhost:3000"
echo "🔧 API Gateway: http://localhost:8000"

# Wait for user to stop
echo "Press Ctrl+C to stop all services"
wait
EOF
    chmod +x start-all.sh
    
    # Create stop-all.sh
    cat > stop-all.sh << 'EOF'
#!/bin/bash
echo "🛑 Stopping SaaS Starter Kit..."

# Stop all containers
docker-compose down

# Kill any running Java processes
pkill -f "spring-boot:run" || true

# Kill any running Node processes
pkill -f "react-scripts start" || true

echo "✅ All services stopped!"
EOF
    chmod +x stop-all.sh
    
    print_success "Startup scripts created!"
}

# Create documentation
create_documentation() {
    print_status "Creating documentation..."
    
    # Create quick start guide
    cat > QUICK_START.md << 'EOF'
# Quick Start Guide

## Prerequisites
- Docker & Docker Compose
- Node.js 18+
- Java 17+
- Maven

## Setup
1. Clone the repository
2. Run the setup script: `./setup.sh`
3. Update the `.env` file with your configuration
4. Start all services: `./start-all.sh`

## Access Points
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8000
- Kong Admin: http://localhost:8002
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Kibana: http://localhost:5601

## Default Credentials
- Database: saas_user / saas_password
- Grafana: admin / admin

## Stopping Services
Run `./stop-all.sh` to stop all services.
EOF
    
    print_success "Documentation created!"
}

# Main execution
main() {
    echo "🎯 SaaS Starter Kit Setup"
    echo "=========================="
    
    check_prerequisites
    create_env_file
    start_infrastructure
    setup_database
    build_backend_services
    setup_frontend
    create_startup_scripts
    create_documentation
    
    echo ""
    echo "🎉 Setup completed successfully!"
    echo ""
    echo "Next steps:"
    echo "1. Update the .env file with your actual configuration values"
    echo "2. Run './start-all.sh' to start all services"
    echo "3. Access the application at http://localhost:3000"
    echo ""
    echo "📚 Check QUICK_START.md for more information"
}

# Run main function
main "$@" 