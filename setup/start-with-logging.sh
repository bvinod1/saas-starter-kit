#!/bin/bash

# SaaS Starter Kit - Start with Comprehensive Logging
# This script starts all services with proper logging and tracing configuration

set -e

echo "🚀 Starting SaaS Starter Kit with Comprehensive Logging and Tracing..."

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
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Check if required ports are available
check_ports() {
    local ports=("5432" "6379" "8080" "8081" "8082" "8083" "8084" "9200" "5601" "8200" "5044" "5000" "9600")
    
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_warning "Port $port is already in use. This might cause conflicts."
        fi
    done
}

# Create necessary directories
create_directories() {
    print_status "Creating necessary directories..."
    
    # Backend log directories
    mkdir -p backend/user-service/logs
    mkdir -p backend/auth-service/logs
    mkdir -p backend/billing-service/logs
    mkdir -p backend/notification-service/logs
    mkdir -p backend/analytics-service/logs
    
    # Frontend log directory
    mkdir -p frontend/logs
    
    # Infrastructure logs
    mkdir -p logs/elasticsearch
    mkdir -p logs/logstash
    mkdir -p logs/kibana
    mkdir -p logs/apm
    
    print_success "Directories created"
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    # Start ELK stack and other infrastructure
    docker-compose up -d postgres redis elasticsearch logstash kibana apm-server prometheus
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    
    # Wait for Elasticsearch
    print_status "Waiting for Elasticsearch..."
    until curl -s http://localhost:9200/_cluster/health > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Elasticsearch is ready"
    
    # Wait for Kibana
    print_status "Waiting for Kibana..."
    until curl -s http://localhost:5601/api/status > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Kibana is ready"
    
    # Wait for APM Server
    print_status "Waiting for APM Server..."
    until curl -s http://localhost:8200/health > /dev/null 2>&1; do
        sleep 5
    done
    print_success "APM Server is ready"
    
    # Wait for Logstash
    print_status "Waiting for Logstash..."
    until curl -s http://localhost:9600 > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Logstash is ready"
    
    print_success "Infrastructure services started"
}

# Start backend services
start_backend_services() {
    print_status "Starting backend services..."
    
    # Start user service
    print_status "Starting User Service..."
    cd backend/user-service
    if [ ! -f "target/user-service-1.0.0.jar" ]; then
        print_status "Building User Service..."
        mvn clean package -DskipTests
    fi
    
    # Start with proper logging configuration
    SPRING_PROFILES_ACTIVE=prod \
    ELASTIC_APM_SERVER_URL=http://localhost:8200 \
    ELASTIC_APM_SECRET_TOKEN=apm_secret_token \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_COM_SAASSTARTERKIT=DEBUG \
    nohup java -javaagent:target/elastic-apm-agent-1.48.1.jar \
        -Delastic.apm.server_url=http://localhost:8200 \
        -Delastic.apm.service_name=user-service \
        -Delastic.apm.application_packages=com.saasstarterkit.user \
        -Delastic.apm.secret_token=apm_secret_token \
        -jar target/user-service-1.0.0.jar > ../logs/user-service.log 2>&1 &
    
    cd ../..
    print_success "User Service started"
    
    # Start other services similarly
    print_status "Starting Auth Service..."
    cd backend/auth-service
    if [ ! -f "target/auth-service-1.0.0.jar" ]; then
        print_status "Building Auth Service..."
        mvn clean package -DskipTests
    fi
    
    nohup java -javaagent:target/elastic-apm-agent-1.48.1.jar \
        -Delastic.apm.server_url=http://localhost:8200 \
        -Delastic.apm.service_name=auth-service \
        -Delastic.apm.application_packages=com.saasstarterkit.auth \
        -Delastic.apm.secret_token=apm_secret_token \
        -jar target/auth-service-1.0.0.jar > ../logs/auth-service.log 2>&1 &
    
    cd ../..
    print_success "Auth Service started"
    
    # Start frontend
    print_status "Starting Frontend..."
    cd frontend
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_status "Installing frontend dependencies..."
        npm install
    fi
    
    # Start with logging environment variables
    REACT_APP_LOG_LEVEL=1 \
    REACT_APP_API_URL=http://localhost:8081 \
    nohup npm start > ../logs/frontend.log 2>&1 &
    
    cd ..
    print_success "Frontend started"
    
    print_success "All backend services started"
}

# Wait for services to be ready
wait_for_services() {
    print_status "Waiting for all services to be ready..."
    
    # Wait for user service
    until curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; do
        sleep 5
    done
    print_success "User Service is ready"
    
    # Wait for auth service
    until curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Auth Service is ready"
    
    # Wait for frontend
    until curl -s http://localhost:3000 > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Frontend is ready"
}

# Display service status
show_status() {
    print_status "Service Status:"
    echo "----------------------------------------"
    echo "PostgreSQL:     http://localhost:5432"
    echo "Redis:          http://localhost:6379"
    echo "Keycloak:       http://localhost:8080"
    echo "User Service:   http://localhost:8081"
    echo "Auth Service:   http://localhost:8082"
    echo "Frontend:       http://localhost:3000"
    echo "Elasticsearch:  http://localhost:9200"
    echo "Kibana:         http://localhost:5601"
    echo "APM Server:     http://localhost:8200"
    echo "Logstash:       http://localhost:9600"
    echo "----------------------------------------"
}

# Main execution
main() {
    print_status "Starting SaaS Starter Kit with comprehensive logging..."
    
    check_docker
    check_ports
    create_directories
    start_infrastructure
    start_backend_services
    wait_for_services
    show_status
    
    print_success "🎉 All services started successfully!"
    print_status "Access Kibana at: http://localhost:5601"
    print_status "Access APM at: http://localhost:5601/app/apm"
    print_status "View logs in Kibana with index pattern: saas-logs-*"
    
    # Save PIDs for easy stopping
    echo "Service PIDs saved to .service-pids"
    jobs -p > .service-pids
}

# Handle script interruption
trap 'print_warning "Interrupted. Use ./stop-all-services.sh to stop all services."; exit 1' INT

# Run main function
main "$@"
