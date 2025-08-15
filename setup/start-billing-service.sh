#!/bin/bash

# Start Billing Service for SaaS Starter Kit
# This script starts the billing service with proper configuration

echo "Starting Billing Service..."

# Set environment variables (adjust these values as needed)
export ZOHO_BILLING_ACCESS_TOKEN="your-access-token-here"
export ZOHO_BILLING_ORG_ID="your-org-id-here"
export ZOHO_BILLING_WEBHOOK_SECRET="your-webhook-secret-here"
export JWT_SECRET="your-jwt-secret-here"

# Navigate to billing service directory
cd backend/billing-service

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "Starting with Maven..."
    mvn spring-boot:run
else
    echo "Maven not found. Please install Maven or use the JAR file."
    echo "You can also run: java -jar target/billing-service-1.0.0.jar"
fi
