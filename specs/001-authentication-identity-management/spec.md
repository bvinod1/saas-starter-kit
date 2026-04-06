# Feature Specification: Authentication & Identity Management

**Feature Branch**: `001-authentication-identity-management`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Register with organisation provisioning (Priority: P1)

A new user fills in first name, last name, email, password, confirm password, and company name,
then submits. The system creates a Keycloak user + dedicated Keycloak client for the organisation,
provisions a per-tenant PostgreSQL schema, and returns access + refresh tokens.

**Acceptance Scenarios**:
1. **Given** no account exists for email, **When** valid registration form submitted, **Then** 201 response with accessToken, refreshToken, and expiresIn.
2. **Given** email already registered, **When** same email submitted, **Then** 409 Conflict with descriptive error.
3. **Given** password < 8 chars, **When** form submitted, **Then** 400 with validation error.
4. **Given** password ≠ confirmPassword, **When** form submitted, **Then** 400 with validation error.

---

### User Story 2 — Email/password login (Priority: P1)

A registered user submits email and password. The system authenticates via Keycloak and returns tokens.

**Acceptance Scenarios**:
1. **Given** valid credentials, **When** POST /api/auth/login, **Then** 200 with accessToken + refreshToken.
2. **Given** invalid password, **When** POST /api/auth/login, **Then** 401 with error message.
3. **Given** account locked, **When** login attempted, **Then** 423 with lockout message.

---

### User Story 3 — Token refresh (Priority: P1)

A client with an expired access token sends its refresh token to obtain a new access token without re-authenticating.

**Acceptance Scenarios**:
1. **Given** valid refresh token, **When** POST /api/auth/refresh, **Then** 200 with new accessToken.
2. **Given** expired/invalid refresh token, **When** POST /api/auth/refresh, **Then** 401.

---

### User Story 4 — OAuth2 social login (Priority: P2)

A user authenticates via Google, Apple, or Microsoft. The system links the OAuth provider identity to an account.

**Acceptance Scenarios**:
1. **Given** valid OAuth callback, **When** redirect lands on /api/auth/login-success, **Then** session created.
2. **Given** OAuth failure, **When** redirect lands on /api/auth/login-failure, **Then** error message returned.

---

### User Story 5 — Token validation (Priority: P2)

A downstream service or client validates a token to check validity and expiry.

**Acceptance Scenarios**:
1. **Given** valid non-expired token, **When** POST /api/auth/validate, **Then** `valid=true, expired=false`.
2. **Given** expired token, **When** POST /api/auth/validate, **Then** `valid=false, expired=true`.

---

### User Story 6 — MFA enrolment and enforcement (Priority: P3)

A user enables TOTP-based MFA on their account. Subsequent logins require the TOTP code.

**Acceptance Scenarios**:
1. **Given** MFA enabled, **When** login with only password, **Then** 401 requesting OTP.
2. **Given** MFA enabled, **When** login with password + valid OTP, **Then** 200 with tokens.

---

### Edge Cases
- What happens when Keycloak is unreachable during registration?
- What happens when tenant schema creation fails after Keycloak user creation?
- How are concurrent registrations with the same email handled?

## Requirements

### Functional Requirements
- **FR-001**: System MUST register users via Keycloak with a dedicated OAuth2 client per organisation.
- **FR-002**: System MUST return JWT access token and refresh token on successful login/registration.
- **FR-003**: System MUST validate tokens and return validity, expiry status, and expiration epoch.
- **FR-004**: System MUST support OAuth2 social login (Google, Apple, Microsoft).
- **FR-005**: System MUST support TOTP-based MFA.
- **FR-006**: System MUST enforce password minimum length of 8 characters.
- **FR-007**: System MUST lock accounts after configurable failed login attempts.
- **FR-008**: System MUST expose token refresh endpoint.
- **FR-009**: Password reset MUST be supported via email-based token flow.
- **FR-010**: Email verification MUST be triggered on registration.

### Key Entities
- **User**: Keycloak identity + local auth metadata (MFA secret, lockout, verification tokens)
- **OAuthProvider**: Links a user to an external OAuth2 identity (google/apple/microsoft)
- **Session**: Active session with IP, user-agent, expiry
- **Role**: Named permission bundle (SUPER_ADMIN, ADMIN, TENANT_ADMIN, USER)
- **Permission**: Fine-grained resource:action pair

### Non-Functional Requirements
- Token refresh MUST complete in < 200 ms p95
- Registration MUST complete in < 2 s p95 (including Keycloak + DB provisioning)
- Auth service MUST support 500 concurrent logins
