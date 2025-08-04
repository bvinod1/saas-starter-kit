#!/bin/bash

echo "🚀 Starting SaaS Starter Kit - Complete Application"
echo "=================================================="

# Check if infrastructure is running
echo "📋 Checking infrastructure services..."
if ! docker-compose ps | grep -q "Up"; then
    echo "⚠️  Infrastructure services not running. Starting them..."
    docker-compose up -d
    sleep 10
fi

# Function to start a service
start_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    
    echo "🔄 Starting $service_name on port $port..."
    cd "$service_path"
    mvn spring-boot:run > "../logs/${service_name}.log" 2>&1 &
    echo $! > "../logs/${service_name}.pid"
    cd - > /dev/null
    sleep 5
}

# Create logs directory
mkdir -p logs

# Start all microservices
echo "🔧 Starting microservices..."

start_service "auth-service" "backend/auth-service" "8080"
start_service "user-service" "backend/user-service" "8081"
start_service "billing-service" "backend/billing-service" "8082"
start_service "notification-service" "backend/notification-service" "8083"
start_service "analytics-service" "backend/analytics-service" "8084"

# Start frontend if not already running
echo "🎨 Starting frontend..."
if ! lsof -i :3000 > /dev/null 2>&1; then
    cd frontend
    npm start > "../logs/frontend.log" 2>&1 &
    echo $! > "../logs/frontend.pid"
    cd - > /dev/null
else
    echo "✅ Frontend already running on port 3000"
fi

echo ""
echo "⏳ Waiting for services to start..."
sleep 15

# Check service status
echo ""
echo "📊 Service Status:"
echo "=================="

services=(
    "auth-service:8080"
    "user-service:8081"
    "billing-service:8082"
    "notification-service:8083"
    "analytics-service:8084"
    "frontend:3000"
)

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        echo "✅ $name (port $port) - Running"
    elif [ "$name" = "frontend" ] && curl -s "http://localhost:$port" > /dev/null 2>&1; then
        echo "✅ $name (port $port) - Running"
    else
        echo "❌ $name (port $port) - Not responding"
    fi
done

echo ""
echo "🌐 Application URLs:"
echo "==================="
echo "Frontend: http://localhost:3000"
echo "API Gateway: http://localhost:8000"
echo "Kong Admin: http://localhost:8001"
echo "Grafana: http://localhost:3000"
echo "Kibana: http://localhost:5601"
echo ""
echo "📝 Logs are available in the 'logs' directory"
echo "🛑 To stop all services, run: ./stop-all-services.sh" 