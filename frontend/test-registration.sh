#!/bin/bash

echo "🧪 Running Registration Page Tests..."
echo "======================================"

# Check if we're in the frontend directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: Please run this script from the frontend directory"
    exit 1
fi

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Check if testing dependencies are available
if ! npm list @testing-library/react > /dev/null 2>&1; then
    echo "📦 Installing testing dependencies..."
    npm install --save-dev @testing-library/react @testing-library/jest-dom @testing-library/user-event jest ts-jest jsdom
fi

echo "🔍 Running tests with detailed output..."
echo ""

# Run the tests with verbose output
npm test -- --verbose --testPathPattern=Registration.test.tsx --watchAll=false

echo ""
echo "✅ Tests completed!"
echo ""
echo "📋 Test Summary:"
echo "- UI Rendering Tests: Verify form elements and validation"
echo "- API Connection Tests: Test successful registration and error handling"
echo "- Environment Configuration Tests: Verify API URL configuration"
echo "- Form State Management Tests: Test form behavior and state changes"
echo ""
echo "💡 If tests fail, check:"
echo "1. Environment variables in .env file"
echo "2. API service configuration"
echo "3. Network connectivity to backend"
echo "4. Backend service status"
