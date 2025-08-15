#!/bin/bash

echo "🛑 Stopping SaaS Starter Kit - Complete Application"
echo "=================================================="

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo "🔄 Stopping $service_name (PID: $pid)..."
            kill "$pid"
            rm "$pid_file"
        else
            echo "⚠️  $service_name not running (PID: $pid)"
            rm "$pid_file"
        fi
    else
        echo "⚠️  No PID file found for $service_name"
    fi
}

# Stop all microservices
echo "🔧 Stopping microservices..."

stop_service "auth-service"
stop_service "user-service"
stop_service "billing-service"
stop_service "notification-service"
stop_service "analytics-service"
stop_service "frontend"

# Stop infrastructure services
echo "🏗️  Stopping infrastructure services..."
docker-compose down

echo ""
echo "✅ All services stopped"
echo "📝 Logs are still available in the 'logs' directory" 