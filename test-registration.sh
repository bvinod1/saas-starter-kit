#!/bin/bash

echo "Testing Registration Endpoint..."

# Test 1: Valid registration
echo "Test 1: Valid registration request"
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n\nTest 2: Invalid email"
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "invalid-email",
    "password": "password123", 
    "confirmPassword": "password123"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n\nTest 3: Mismatched passwords"
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com", 
    "password": "password123",
    "confirmPassword": "different"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n\nTest 4: Missing required fields"
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s 