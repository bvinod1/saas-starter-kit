#!/usr/bin/env python3
"""Write contracts/ YAML stubs for all applicable features."""
import os

SPECS_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "specs")

CONTRACTS = {
    # ── REST / OpenAPI ────────────────────────────────────────────────────
    "001-authentication-identity-management": {
        "openapi/auth-service.yaml": """\
openapi: "3.1.0"
info:
  title: Auth Service API
  version: "1.0.0"
  description: Authentication and identity management endpoints.

servers:
  - url: /api/v1/auth
    description: Via Kong proxy

security:
  - bearerAuth: []

paths:
  /login:
    post:
      summary: Authenticate user and obtain tokens
      operationId: login
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        "200":
          description: Tokens issued
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TokenResponse'
        "401":
          $ref: '#/components/responses/Unauthorized'
        "429":
          $ref: '#/components/responses/TooManyRequests'

  /logout:
    post:
      summary: Revoke refresh token
      operationId: logout
      responses:
        "204":
          description: Logged out
        "401":
          $ref: '#/components/responses/Unauthorized'

  /refresh:
    post:
      summary: Refresh access token
      operationId: refreshToken
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshRequest'
      responses:
        "200":
          description: New access token
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TokenResponse'
        "401":
          $ref: '#/components/responses/Unauthorized'

  /mfa/enroll:
    post:
      summary: Enroll TOTP MFA for the current user
      operationId: enrollMfa
      responses:
        "200":
          description: QR code and secret returned
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MfaEnrollResponse'

  /mfa/verify:
    post:
      summary: Verify TOTP code and complete MFA setup
      operationId: verifyMfa
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MfaVerifyRequest'
      responses:
        "204":
          description: MFA verified

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    LoginRequest:
      type: object
      required: [email, password, tenantSubdomain]
      properties:
        email:
          type: string
          format: email
        password:
          type: string
          format: password
        tenantSubdomain:
          type: string
        mfaCode:
          type: string
          pattern: '^[0-9]{6}$'

    TokenResponse:
      type: object
      properties:
        accessToken:
          type: string
        expiresIn:
          type: integer
          description: Seconds until access token expires
        tokenType:
          type: string
          enum: [Bearer]

    RefreshRequest:
      type: object
      required: [refreshToken]
      properties:
        refreshToken:
          type: string

    MfaEnrollResponse:
      type: object
      properties:
        secret:
          type: string
        qrCodeUri:
          type: string

    MfaVerifyRequest:
      type: object
      required: [code]
      properties:
        code:
          type: string
          pattern: '^[0-9]{6}$'

  responses:
    Unauthorized:
      description: Authentication required or token invalid
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    TooManyRequests:
      description: Rate limit exceeded
      headers:
        Retry-After:
          schema:
            type: integer

    ErrorResponse:
      description: Generic error

  schemas:
    ErrorResponse:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
        traceId:
          type: string
""",
    },

    "002-multi-tenancy-tenant-lifecycle": {
        "openapi/tenant-service.yaml": """\
openapi: "3.1.0"
info:
  title: Tenant Service API
  version: "1.0.0"
  description: Tenant lifecycle management.

servers:
  - url: /api/v1

security:
  - bearerAuth: []

paths:
  /tenants:
    post:
      summary: Create (provision) a new tenant
      operationId: createTenant
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTenantRequest'
      responses:
        "202":
          description: Provisioning accepted
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TenantResponse'
        "409":
          description: Subdomain already taken

    get:
      summary: List all tenants (super-admin only)
      operationId: listTenants
      parameters:
        - name: status
          in: query
          schema:
            type: string
            enum: [PENDING, ACTIVE, SUSPENDED, ARCHIVED]
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        "200":
          description: Paginated tenant list
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TenantPage'

  /tenants/{tenantId}:
    parameters:
      - name: tenantId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    get:
      summary: Get tenant details
      operationId: getTenant
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TenantResponse'
    patch:
      summary: Update tenant
      operationId: updateTenant
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateTenantRequest'
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TenantResponse'
    delete:
      summary: Archive (soft-delete) tenant
      operationId: archiveTenant
      responses:
        "204":
          description: Archived

  /tenants/{tenantId}/status:
    get:
      summary: Get provisioning status
      operationId: getTenantStatus
      parameters:
        - name: tenantId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TenantStatusResponse'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    CreateTenantRequest:
      type: object
      required: [name, subdomain, planId]
      properties:
        name:
          type: string
          maxLength: 200
        subdomain:
          type: string
          pattern: '^[a-z0-9-]{3,50}$'
        planId:
          type: string
          enum: [starter, pro, enterprise]

    UpdateTenantRequest:
      type: object
      properties:
        name:
          type: string
        status:
          type: string
          enum: [ACTIVE, SUSPENDED]

    TenantResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
        subdomain:
          type: string
        status:
          type: string
        planId:
          type: string
        createdAt:
          type: string
          format: date-time

    TenantStatusResponse:
      type: object
      properties:
        tenantId:
          type: string
          format: uuid
        status:
          type: string
          enum: [PENDING, PROVISIONING, ACTIVE, FAILED]
        errorMessage:
          type: string
          nullable: true

    TenantPage:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/TenantResponse'
        totalElements:
          type: integer
        totalPages:
          type: integer
        page:
          type: integer
""",
    },

    "003-user-management": {
        "openapi/user-service.yaml": """\
openapi: "3.1.0"
info:
  title: User Service API
  version: "1.0.0"
  description: User management endpoints. All paths require X-Tenant-ID header.

servers:
  - url: /api/v1

security:
  - bearerAuth: []

paths:
  /users:
    get:
      summary: List users for current tenant
      operationId: listUsers
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
        - name: status
          in: query
          schema:
            type: string
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        "200":
          description: Paginated user list
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserPage'

    post:
      summary: Create a new user
      operationId: createUser
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        "201":
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        "409":
          description: Email already exists in this tenant

  /users/{userId}:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: userId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    get:
      summary: Get user profile
      operationId: getUser
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
    patch:
      summary: Update user profile
      operationId: updateUser
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateUserRequest'
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
    delete:
      summary: Deactivate (soft-delete) user
      operationId: deleteUser
      responses:
        "204":
          description: Deactivated

  /users/{userId}/roles:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: userId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    post:
      summary: Assign a role to user
      operationId: assignRole
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AssignRoleRequest'
      responses:
        "204":
          description: Role assigned

  /users/{userId}/erasure:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: userId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    post:
      summary: Request GDPR data erasure
      operationId: requestErasure
      responses:
        "202":
          description: Erasure request accepted

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  parameters:
    TenantIdHeader:
      name: X-Tenant-ID
      in: header
      required: true
      schema:
        type: string
        format: uuid

  schemas:
    CreateUserRequest:
      type: object
      required: [email, firstName, lastName]
      properties:
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        phone:
          type: string
        roles:
          type: array
          items:
            type: string
        dynamicFields:
          type: object
          additionalProperties: true
          description: Tenant-specific profile fields

    UpdateUserRequest:
      type: object
      properties:
        firstName:
          type: string
        lastName:
          type: string
        phone:
          type: string
        status:
          type: string
          enum: [ACTIVE, SUSPENDED]
        dynamicFields:
          type: object
          additionalProperties: true

    AssignRoleRequest:
      type: object
      required: [roleId]
      properties:
        roleId:
          type: string

    UserResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        tenantId:
          type: string
          format: uuid
        email:
          type: string
        firstName:
          type: string
        lastName:
          type: string
        status:
          type: string
        roles:
          type: array
          items:
            type: string
        dynamicFields:
          type: object
          additionalProperties: true
        createdAt:
          type: string
          format: date-time

    UserPage:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/UserResponse'
        totalElements:
          type: integer
        totalPages:
          type: integer
        page:
          type: integer
""",
    },

    "004-organization-management": {
        "openapi/organisation-service.yaml": """\
openapi: "3.1.0"
info:
  title: Organisation Service API
  version: "1.0.0"
  description: Organization hierarchy and membership management.

servers:
  - url: /api/v1

security:
  - bearerAuth: []

paths:
  /organizations:
    get:
      summary: List organizations for tenant
      operationId: listOrganizations
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
        - name: parentOrgId
          in: query
          schema:
            type: string
            format: uuid
      responses:
        "200":
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/OrgResponse'

    post:
      summary: Create organization
      operationId: createOrganization
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateOrgRequest'
      responses:
        "201":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrgResponse'

  /organizations/{orgId}:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: orgId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    get:
      summary: Get organization
      operationId: getOrganization
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrgResponse'
    patch:
      summary: Update organization
      operationId: updateOrganization
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateOrgRequest'
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrgResponse'
    delete:
      summary: Archive organization (soft-delete)
      operationId: archiveOrganization
      responses:
        "204":
          description: Archived

  /organizations/{orgId}/members:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: orgId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    get:
      summary: List organization members
      operationId: listMembers
      responses:
        "200":
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MemberResponse'
    post:
      summary: Add member to organization
      operationId: addMember
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddMemberRequest'
      responses:
        "204":
          description: Member added

  /organizations/{orgId}/members/{userId}:
    parameters:
      - $ref: '#/components/parameters/TenantIdHeader'
      - name: orgId
        in: path
        required: true
        schema:
          type: string
          format: uuid
      - name: userId
        in: path
        required: true
        schema:
          type: string
          format: uuid
    delete:
      summary: Remove member from organization
      operationId: removeMember
      responses:
        "204":
          description: Member removed

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  parameters:
    TenantIdHeader:
      name: X-Tenant-ID
      in: header
      required: true
      schema:
        type: string
        format: uuid

  schemas:
    CreateOrgRequest:
      type: object
      required: [name, type]
      properties:
        name:
          type: string
        type:
          type: string
          enum: [COMPANY, DIVISION, DEPARTMENT, TEAM]
        parentOrgId:
          type: string
          format: uuid
        metadata:
          type: object
          additionalProperties: true

    UpdateOrgRequest:
      type: object
      properties:
        name:
          type: string
        metadata:
          type: object
          additionalProperties: true

    OrgResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        tenantId:
          type: string
          format: uuid
        name:
          type: string
        type:
          type: string
        parentOrgId:
          type: string
          format: uuid
          nullable: true
        status:
          type: string
        createdAt:
          type: string
          format: date-time

    AddMemberRequest:
      type: object
      required: [userId, roleInOrg]
      properties:
        userId:
          type: string
          format: uuid
        roleInOrg:
          type: string
          enum: [OWNER, ADMIN, MEMBER]

    MemberResponse:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        roleInOrg:
          type: string
        joinedAt:
          type: string
          format: date-time
""",
    },

    "006-subscription-billing-management": {
        "openapi/billing-service.yaml": """\
openapi: "3.1.0"
info:
  title: Billing Service API
  version: "1.0.0"
  description: Subscription and billing management.

servers:
  - url: /api/v1

security:
  - bearerAuth: []

paths:
  /subscriptions:
    post:
      summary: Create or upgrade subscription for tenant
      operationId: createSubscription
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateSubscriptionRequest'
      responses:
        "201":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubscriptionResponse'

  /subscriptions/current:
    get:
      summary: Get current tenant subscription
      operationId: getCurrentSubscription
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubscriptionResponse'

  /subscriptions/{subscriptionId}/cancel:
    post:
      summary: Cancel subscription
      operationId: cancelSubscription
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
        - name: subscriptionId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CancelSubscriptionRequest'
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SubscriptionResponse'

  /invoices:
    get:
      summary: List invoices for tenant
      operationId: listInvoices
      parameters:
        - $ref: '#/components/parameters/TenantIdHeader'
        - name: status
          in: query
          schema:
            type: string
        - name: page
          in: query
          schema:
            type: integer
            default: 0
      responses:
        "200":
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/InvoicePage'

  /billing/webhook:
    post:
      summary: Receive provider webhook (Stripe / other)
      operationId: handleWebhook
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: Webhook processed

  /plans:
    get:
      summary: List available subscription plans
      operationId: listPlans
      security: []
      responses:
        "200":
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/PlanResponse'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  parameters:
    TenantIdHeader:
      name: X-Tenant-ID
      in: header
      required: true
      schema:
        type: string
        format: uuid

  schemas:
    CreateSubscriptionRequest:
      type: object
      required: [planId, billingCycle]
      properties:
        planId:
          type: string
        billingCycle:
          type: string
          enum: [MONTHLY, ANNUAL]
        paymentMethodId:
          type: string
          format: uuid

    CancelSubscriptionRequest:
      type: object
      properties:
        cancelAtPeriodEnd:
          type: boolean
          default: true
        reason:
          type: string

    SubscriptionResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        tenantId:
          type: string
          format: uuid
        planId:
          type: string
        status:
          type: string
        billingCycle:
          type: string
        currentPeriodStart:
          type: string
          format: date-time
        currentPeriodEnd:
          type: string
          format: date-time
        trialEndsAt:
          type: string
          format: date-time
          nullable: true

    PlanResponse:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        priceMonthly:
          type: integer
          description: Price in cents
        priceAnnual:
          type: integer
        features:
          type: object
          additionalProperties: true

    InvoicePage:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/InvoiceResponse'
        totalElements:
          type: integer
        page:
          type: integer

    InvoiceResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        amountCents:
          type: integer
        currency:
          type: string
        status:
          type: string
        dueDate:
          type: string
          format: date
        paidAt:
          type: string
          format: date-time
          nullable: true
""",
    },

    # ── AsyncAPI (event-driven) ───────────────────────────────────────────
    "007-notification-service": {
        "asyncapi/notification-events.yaml": """\
asyncapi: "2.6.0"
info:
  title: Notification Service Events
  version: "1.0.0"
  description: >
    Notification service consumes domain events from RabbitMQ and emits
    notification delivery status events.

servers:
  rabbitmq:
    url: amqp://localhost:5672
    protocol: amqp
    description: RabbitMQ broker

channels:
  "{tenantId}.user.registered.email":
    description: Trigger welcome email on user registration
    subscribe:
      message:
        $ref: '#/components/messages/UserRegisteredEvent'

  "{tenantId}.user.password_reset.email":
    description: Trigger password reset email
    subscribe:
      message:
        $ref: '#/components/messages/PasswordResetEvent'

  "{tenantId}.subscription.activated.email":
    description: Trigger subscription confirmation email
    subscribe:
      message:
        $ref: '#/components/messages/SubscriptionActivatedEvent'

  "{tenantId}.notification.delivered":
    description: Notification delivery status published by notification-service
    publish:
      message:
        $ref: '#/components/messages/NotificationDeliveredEvent'

  notifications.dlq:
    description: Dead-letter queue for failed notifications
    publish:
      message:
        $ref: '#/components/messages/NotificationFailedEvent'

components:
  messages:
    UserRegisteredEvent:
      payload:
        type: object
        required: [userId, email, firstName, tenantId]
        properties:
          userId:
            type: string
            format: uuid
          email:
            type: string
          firstName:
            type: string
          tenantId:
            type: string
            format: uuid

    PasswordResetEvent:
      payload:
        type: object
        required: [userId, email, resetToken, tenantId]
        properties:
          userId:
            type: string
            format: uuid
          email:
            type: string
          resetToken:
            type: string
          expiresAt:
            type: string
            format: date-time
          tenantId:
            type: string
            format: uuid

    SubscriptionActivatedEvent:
      payload:
        type: object
        required: [tenantId, planId, billingCycle]
        properties:
          tenantId:
            type: string
            format: uuid
          planId:
            type: string
          billingCycle:
            type: string

    NotificationDeliveredEvent:
      payload:
        type: object
        properties:
          notificationId:
            type: string
            format: uuid
          channel:
            type: string
          status:
            type: string
            enum: [DELIVERED, FAILED]
          sentAt:
            type: string
            format: date-time

    NotificationFailedEvent:
      payload:
        type: object
        properties:
          notificationId:
            type: string
            format: uuid
          reason:
            type: string
          retryCount:
            type: integer
""",
    },

    "009-security-compliance": {
        "asyncapi/compliance-events.yaml": """\
asyncapi: "2.6.0"
info:
  title: Security & Compliance Events
  version: "1.0.0"
  description: >
    Events published and consumed for GDPR erasure sagas and security
    audit event propagation.

servers:
  rabbitmq:
    url: amqp://localhost:5672
    protocol: amqp

channels:
  "user.data.erasure.requested":
    description: GDPR Right-to-Erasure saga initiator
    publish:
      message:
        $ref: '#/components/messages/UserDataErasureRequestedEvent'

  "{service}.user.data.erasure.completed":
    description: Per-service confirmation that erasure is done
    subscribe:
      message:
        $ref: '#/components/messages/ServiceErasureCompletedEvent'

  "security.event.detected":
    description: Security incident published for SIEM/alerting
    publish:
      message:
        $ref: '#/components/messages/SecurityEventDetectedEvent'

  "user.consent.updated":
    description: Consent preference changed by user
    publish:
      message:
        $ref: '#/components/messages/ConsentUpdatedEvent'

components:
  messages:
    UserDataErasureRequestedEvent:
      payload:
        type: object
        required: [erasureRequestId, userId, tenantId, requestedAt]
        properties:
          erasureRequestId:
            type: string
            format: uuid
          userId:
            type: string
            format: uuid
          tenantId:
            type: string
            format: uuid
          requestedAt:
            type: string
            format: date-time

    ServiceErasureCompletedEvent:
      payload:
        type: object
        required: [erasureRequestId, serviceName, completedAt]
        properties:
          erasureRequestId:
            type: string
            format: uuid
          serviceName:
            type: string
          completedAt:
            type: string
            format: date-time
          status:
            type: string
            enum: [SUCCESS, PARTIAL, FAILED]

    SecurityEventDetectedEvent:
      payload:
        type: object
        required: [eventType, severity, tenantId, occurredAt]
        properties:
          eventType:
            type: string
            enum: [FAILED_LOGIN, BRUTE_FORCE, PRIVILEGE_ESCALATION, ANOMALY]
          severity:
            type: string
            enum: [LOW, MEDIUM, HIGH, CRITICAL]
          tenantId:
            type: string
            format: uuid
          userId:
            type: string
            format: uuid
            nullable: true
          sourceIp:
            type: string
          occurredAt:
            type: string
            format: date-time

    ConsentUpdatedEvent:
      payload:
        type: object
        required: [userId, tenantId, consentType, consented]
        properties:
          userId:
            type: string
            format: uuid
          tenantId:
            type: string
            format: uuid
          consentType:
            type: string
          consented:
            type: boolean
          version:
            type: string
          updatedAt:
            type: string
            format: date-time
""",
    },

    "008-analytics-observability": {
        "asyncapi/analytics-events.yaml": """\
asyncapi: "2.6.0"
info:
  title: Analytics & Observability Events
  version: "1.0.0"
  description: >
    Feature usage events consumed by the analytics service for usage
    reporting and billing telemetry.

servers:
  rabbitmq:
    url: amqp://localhost:5672
    protocol: amqp

channels:
  "analytics.feature.usage":
    description: Feature usage event emitted by any service
    subscribe:
      message:
        $ref: '#/components/messages/FeatureUsageEvent'

  "analytics.usage.summary.computed":
    description: Periodic usage summary published by analytics-service
    publish:
      message:
        $ref: '#/components/messages/UsageSummaryComputedEvent'

components:
  messages:
    FeatureUsageEvent:
      payload:
        type: object
        required: [tenantId, feature, action, occurredAt]
        properties:
          tenantId:
            type: string
            format: uuid
          userId:
            type: string
            format: uuid
            nullable: true
          feature:
            type: string
            description: Feature identifier, e.g. "user-management"
          action:
            type: string
            description: Action taken, e.g. "create", "read"
          occurredAt:
            type: string
            format: date-time
          metadata:
            type: object
            additionalProperties: true

    UsageSummaryComputedEvent:
      payload:
        type: object
        required: [tenantId, periodStart, periodEnd]
        properties:
          tenantId:
            type: string
            format: uuid
          periodStart:
            type: string
            format: date
          periodEnd:
            type: string
            format: date
          activeUsers:
            type: integer
          apiCalls:
            type: integer
            format: int64
          computedAt:
            type: string
            format: date-time
""",
    },

    "005-role-based-access-control": {
        "asyncapi/rbac-events.yaml": """\
asyncapi: "2.6.0"
info:
  title: RBAC Events
  version: "1.0.0"
  description: >
    Role assignment and permission change events published for cache
    invalidation and audit propagation.

servers:
  rabbitmq:
    url: amqp://localhost:5672
    protocol: amqp

channels:
  "rbac.role.assigned":
    description: A role was assigned to a user
    publish:
      message:
        $ref: '#/components/messages/RoleAssignedEvent'

  "rbac.role.revoked":
    description: A role was revoked from a user
    publish:
      message:
        $ref: '#/components/messages/RoleRevokedEvent'

  "rbac.permission.changed":
    description: Permissions on a role were modified; triggers cache invalidation
    publish:
      message:
        $ref: '#/components/messages/PermissionChangedEvent'

components:
  messages:
    RoleAssignedEvent:
      payload:
        type: object
        required: [userId, tenantId, roleId, grantedBy]
        properties:
          userId:
            type: string
            format: uuid
          tenantId:
            type: string
            format: uuid
          roleId:
            type: string
          grantedBy:
            type: string
            format: uuid
          grantedAt:
            type: string
            format: date-time

    RoleRevokedEvent:
      payload:
        type: object
        required: [userId, tenantId, roleId, revokedBy]
        properties:
          userId:
            type: string
            format: uuid
          tenantId:
            type: string
            format: uuid
          roleId:
            type: string
          revokedBy:
            type: string
            format: uuid
          revokedAt:
            type: string
            format: date-time

    PermissionChangedEvent:
      payload:
        type: object
        required: [roleId, tenantId, changedBy]
        properties:
          roleId:
            type: string
          tenantId:
            type: string
            format: uuid
          changedBy:
            type: string
            format: uuid
          changedAt:
            type: string
            format: date-time
          added:
            type: array
            items:
              type: string
          removed:
            type: array
            items:
              type: string
""",
    },
}


def main():
    written = 0
    for feature_dir, files in CONTRACTS.items():
        for rel_path, content in files.items():
            target = os.path.join(SPECS_DIR, feature_dir, "contracts", rel_path)
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with open(target, "w") as f:
                f.write(content)
            print(f"OK: {feature_dir}/contracts/{rel_path}")
            written += 1
    print(f"\nContract files written: {written}")


if __name__ == "__main__":
    main()
