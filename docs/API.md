# API Documentation

## Overview

The SaaS Starter Kit provides a comprehensive REST API through Kong API Gateway. All API endpoints are secured with JWT authentication and include rate limiting.

**Base URL**: `http://localhost:8000` (via Kong Gateway)

## Authentication

### JWT Token Format

All authenticated requests must include a JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

### Token Structure

```json
{
  "sub": "user_id",
  "iss": "saas-starter-kit",
  "aud": "saas-starter-kit",
  "exp": 1640995200,
  "iat": 1640908800,
  "roles": ["user", "admin"],
  "permissions": ["users.read", "users.write"],
  "organization_id": "org_uuid"
}
```

## Authentication Service API

### User Registration

**POST** `/api/auth/register`

Register a new user account.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "organizationName": "My Company"
}
```

**Response**:
```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification.",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "verificationRequired": true
  }
}
```

### User Login

**POST** `/api/auth/login`

Authenticate user and receive JWT token.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_token",
    "refreshToken": "refresh_token",
    "expiresIn": 86400,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "roles": ["user"],
      "organizationId": "org_uuid"
    }
  }
}
```

### Refresh Token

**POST** `/api/auth/refresh`

Refresh expired access token.

**Request Body**:
```json
{
  "refreshToken": "refresh_token"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "new_jwt_token",
    "expiresIn": 86400
  }
}
```

### Forgot Password

**POST** `/api/auth/forgot-password`

Request password reset email.

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Password reset email sent"
}
```

### Reset Password

**POST** `/api/auth/reset-password`

Reset password using token.

**Request Body**:
```json
{
  "token": "reset_token",
  "newPassword": "newSecurePassword123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Password reset successful"
}
```

### Verify Email

**POST** `/api/auth/verify-email`

Verify email address.

**Request Body**:
```json
{
  "token": "verification_token"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

### OAuth2 Authorization

**GET** `/api/auth/oauth2/authorization/{provider}`

Redirect to OAuth2 provider for authentication.

**Parameters**:
- `provider`: `google`, `facebook`, `microsoft`

**Response**: Redirect to OAuth2 provider

### OAuth2 Callback

**GET** `/api/auth/oauth2/callback/{provider}`

Handle OAuth2 callback and authenticate user.

**Parameters**:
- `provider`: `google`, `facebook`, `microsoft`

**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "jwt_token",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  }
}
```

### MFA Enable

**POST** `/api/auth/mfa/enable`

Enable multi-factor authentication.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "qrCode": "data:image/png;base64,...",
    "secret": "JBSWY3DPEHPK3PXP"
  }
}
```

### MFA Verify

**POST** `/api/auth/mfa/verify`

Verify MFA token.

**Request Body**:
```json
{
  "token": "123456"
}
```

**Response**:
```json
{
  "success": true,
  "message": "MFA verified successfully"
}
```

## User Management Service API

### Get User Profile

**GET** `/api/users/profile`

Get current user's profile.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "avatarUrl": "https://example.com/avatar.jpg",
    "bio": "Software Developer",
    "timezone": "UTC",
    "language": "en",
    "preferences": {
      "theme": "dark",
      "notifications": true
    },
    "organization": {
      "id": "org_uuid",
      "name": "My Company",
      "role": "admin"
    }
  }
}
```

### Update User Profile

**PUT** `/api/users/profile`

Update current user's profile.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "bio": "Software Developer",
  "timezone": "UTC",
  "language": "en",
  "preferences": {
    "theme": "dark",
    "notifications": true
  }
}
```

**Response**:
```json
{
  "success": true,
  "message": "Profile updated successfully"
}
```

### Get User by ID

**GET** `/api/users/{id}`

Get user by ID (admin only).

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00Z",
    "lastLoginAt": "2024-01-15T10:30:00Z"
  }
}
```

### Get Organization Members

**GET** `/api/organizations/{id}/members`

Get organization members.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "members": [
      {
        "id": "uuid",
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "admin",
        "joinedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### Invite User to Organization

**POST** `/api/organizations/{id}/invite`

Invite user to organization.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "role": "member",
  "message": "Welcome to our team!"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Invitation sent successfully"
}
```

## Billing Service API

### Get Subscription Plans

**GET** `/api/billing/plans`

Get available subscription plans.

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "plan_uuid",
      "name": "Starter",
      "description": "Perfect for small teams",
      "price": 29.00,
      "currency": "USD",
      "billingCycle": "monthly",
      "features": {
        "users": 5,
        "storage": "10GB",
        "features": ["basic_analytics", "email_support"]
      },
      "trialDays": 14
    }
  ]
}
```

### Get Current Subscription

**GET** `/api/billing/subscriptions/current`

Get current organization subscription.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "sub_uuid",
    "plan": {
      "id": "plan_uuid",
      "name": "Professional",
      "price": 99.00
    },
    "status": "active",
    "currentPeriodStart": "2024-01-01T00:00:00Z",
    "currentPeriodEnd": "2024-02-01T00:00:00Z",
    "trialStart": "2024-01-01T00:00:00Z",
    "trialEnd": "2024-01-15T00:00:00Z",
    "cancelAtPeriodEnd": false
  }
}
```

### Create Subscription

**POST** `/api/billing/subscriptions`

Create new subscription.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "planId": "plan_uuid",
  "paymentMethodId": "pm_uuid",
  "couponCode": "WELCOME10"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "sub_uuid",
    "status": "active",
    "clientSecret": "pi_secret"
  }
}
```

### Cancel Subscription

**POST** `/api/billing/subscriptions/{id}/cancel`

Cancel subscription.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "message": "Subscription will be canceled at the end of the current period"
}
```

### Get Invoices

**GET** `/api/billing/invoices`

Get organization invoices.

**Headers**: `Authorization: Bearer <jwt_token>`

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 10)
- `status`: Filter by status (paid, unpaid, draft)

**Response**:
```json
{
  "success": true,
  "data": {
    "invoices": [
      {
        "id": "inv_uuid",
        "number": "INV-2024-001",
        "amount": 99.00,
        "currency": "USD",
        "status": "paid",
        "dueDate": "2024-01-15T00:00:00Z",
        "paidAt": "2024-01-10T00:00:00Z",
        "items": [
          {
            "description": "Professional Plan",
            "amount": 99.00,
            "quantity": 1
          }
        ]
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### Get Payment Methods

**GET** `/api/billing/payment-methods`

Get organization payment methods.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "pm_uuid",
      "type": "card",
      "lastFour": "4242",
      "brand": "visa",
      "expiryMonth": 12,
      "expiryYear": 2025,
      "isDefault": true
    }
  ]
}
```

### Add Payment Method

**POST** `/api/billing/payment-methods`

Add new payment method.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "type": "card",
  "token": "tok_visa"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "pm_uuid",
    "lastFour": "4242",
    "brand": "visa"
  }
}
```

## Notification Service API

### Send Email Notification

**POST** `/api/notifications/email`

Send email notification.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "to": "user@example.com",
  "template": "welcome_email",
  "variables": {
    "firstName": "John",
    "companyName": "My Company"
  }
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "notif_uuid",
    "status": "sent"
  }
}
```

### Send SMS Notification

**POST** `/api/notifications/sms`

Send SMS notification.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "to": "+1234567890",
  "template": "welcome_sms",
  "variables": {
    "firstName": "John"
  }
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "notif_uuid",
    "status": "sent"
  }
}
```

### Get Notifications

**GET** `/api/notifications`

Get user notifications.

**Headers**: `Authorization: Bearer <jwt_token>`

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 10)
- `type`: Filter by type (email, sms, push, in_app)
- `status`: Filter by status (pending, sent, failed, read)

**Response**:
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": "notif_uuid",
        "type": "email",
        "title": "Welcome to SaaS Starter Kit",
        "message": "Hello John, welcome to our platform!",
        "status": "sent",
        "sentAt": "2024-01-01T00:00:00Z",
        "readAt": null
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### Mark Notification as Read

**PUT** `/api/notifications/{id}/read`

Mark notification as read.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "message": "Notification marked as read"
}
```

### Get Notification Preferences

**GET** `/api/notifications/preferences`

Get user notification preferences.

**Headers**: `Authorization: Bearer <jwt_token>`

**Response**:
```json
{
  "success": true,
  "data": {
    "emailEnabled": true,
    "smsEnabled": true,
    "pushEnabled": true,
    "inAppEnabled": true,
    "preferences": {
      "marketing": true,
      "security": true,
      "billing": true
    }
  }
}
```

### Update Notification Preferences

**PUT** `/api/notifications/preferences`

Update user notification preferences.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "inAppEnabled": true,
  "preferences": {
    "marketing": false,
    "security": true,
    "billing": true
  }
}
```

**Response**:
```json
{
  "success": true,
  "message": "Preferences updated successfully"
}
```

## Analytics Service API

### Track User Activity

**POST** `/api/analytics/events`

Track user activity event.

**Headers**: `Authorization: Bearer <jwt_token>`

**Request Body**:
```json
{
  "action": "user_login",
  "resourceType": "auth",
  "resourceId": "user_uuid",
  "metadata": {
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "location": "New York, NY"
  }
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "event_uuid",
    "trackedAt": "2024-01-01T00:00:00Z"
  }
}
```

### Get User Activities

**GET** `/api/analytics/activities`

Get user activity logs.

**Headers**: `Authorization: Bearer <jwt_token>`

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 10)
- `action`: Filter by action
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)

**Response**:
```json
{
  "success": true,
  "data": {
    "activities": [
      {
        "id": "activity_uuid",
        "action": "user_login",
        "resourceType": "auth",
        "resourceId": "user_uuid",
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0...",
        "metadata": {
          "location": "New York, NY"
        },
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### Get API Usage Statistics

**GET** `/api/analytics/usage`

Get API usage statistics.

**Headers**: `Authorization: Bearer <jwt_token>`

**Query Parameters**:
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)
- `endpoint`: Filter by endpoint

**Response**:
```json
{
  "success": true,
  "data": {
    "totalRequests": 1000,
    "averageResponseTime": 150,
    "successRate": 98.5,
    "endpoints": [
      {
        "endpoint": "/api/auth/login",
        "requests": 100,
        "averageResponseTime": 120,
        "successRate": 99.0
      }
    ],
    "period": {
      "start": "2024-01-01T00:00:00Z",
      "end": "2024-01-31T23:59:59Z"
    }
  }
}
```

### Get Business Metrics

**GET** `/api/analytics/metrics`

Get business metrics.

**Headers**: `Authorization: Bearer <jwt_token>`

**Query Parameters**:
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)
- `metric`: Specific metric (users, revenue, subscriptions)

**Response**:
```json
{
  "success": true,
  "data": {
    "users": {
      "total": 1000,
      "active": 850,
      "newThisMonth": 50
    },
    "revenue": {
      "total": 50000.00,
      "thisMonth": 5000.00,
      "growth": 15.5
    },
    "subscriptions": {
      "total": 800,
      "active": 750,
      "churnRate": 2.5
    }
  }
}
```

## Error Responses

All API endpoints return consistent error responses:

### 400 Bad Request
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "email": ["Email is required"],
      "password": ["Password must be at least 8 characters"]
    }
  }
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

### 403 Forbidden
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Insufficient permissions"
  }
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found"
  }
}
```

### 429 Too Many Requests
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded",
    "retryAfter": 60
  }
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An unexpected error occurred"
  }
}
```

## Rate Limiting

API endpoints are rate-limited to prevent abuse:

- **Authentication endpoints**: 100 requests per minute
- **User management endpoints**: 200 requests per minute
- **Billing endpoints**: 50 requests per minute
- **Notification endpoints**: 100 requests per minute
- **Analytics endpoints**: 300 requests per minute

Rate limit headers are included in responses:
- `X-RateLimit-Limit`: Request limit per window
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Time when the rate limit resets

## Pagination

List endpoints support pagination with the following query parameters:

- `page`: Page number (default: 1)
- `size`: Page size (default: 10, max: 100)

Response includes pagination metadata:
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 100,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## WebSocket API

### Real-time Notifications

**WebSocket URL**: `ws://localhost:8000/ws/notifications`

**Authentication**: Include JWT token in query parameter
```
ws://localhost:8000/ws/notifications?token=<jwt_token>
```

**Message Format**:
```json
{
  "type": "notification",
  "data": {
    "id": "notif_uuid",
    "title": "New message",
    "message": "You have a new message",
    "type": "in_app",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

## SDKs and Libraries

### JavaScript/TypeScript SDK

```javascript
import { SaasClient } from '@saas-starter-kit/sdk';

const client = new SaasClient({
  baseUrl: 'http://localhost:8000',
  apiKey: 'your-api-key'
});

// Authenticate
const auth = await client.auth.login({
  email: 'user@example.com',
  password: 'password'
});

// Use authenticated client
const user = await client.users.getProfile();
```

### Java SDK

```java
import com.saasstarterkit.client.SaasClient;

SaasClient client = SaasClient.builder()
    .baseUrl("http://localhost:8000")
    .apiKey("your-api-key")
    .build();

// Authenticate
AuthResponse auth = client.auth().login(LoginRequest.builder()
    .email("user@example.com")
    .password("password")
    .build());

// Use authenticated client
UserProfile user = client.users().getProfile();
``` 