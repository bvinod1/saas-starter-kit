# Research: Authentication & Identity Management

## Decision: Identity Provider — Keycloak

**Decision**: Use Keycloak as the local identity provider, mapped to cloud-native providers per environment.
**Rationale**: Keycloak is fully open-source, runs offline, supports OAuth2/OIDC/SAML, provides Admin REST API for programmatic user/client management, and maps cleanly to Cognito (AWS), Firebase Auth (GCP), and Azure AD B2C.
**Alternatives considered**:
- Auth0: SaaS-only, not cloud-agnostic, no offline mode
- Custom JWT: No SSO, significant re-implementation surface

## Decision: Client-per-Organisation Model

**Decision**: Each new organisation registration creates a dedicated Keycloak OAuth2 client (client_id = org slug).
**Rationale**: Provides hard isolation of tokens per organisation; token scope cannot bleed across orgs; enables per-org OAuth2 callback URLs; allows organisation-specific SSO configuration later.
**Alternatives considered**:
- Single client with custom claims: Cross-tenant token reuse risk; harder to revoke per org

## Decision: JWT Access + Refresh Tokens

**Decision**: Keycloak issues short-lived access tokens (15 min default) and longer-lived refresh tokens.
**Rationale**: Standard OAuth2 OIDC pattern; access token validated at Kong; does NOT re-validate downstream (reduces latency); refresh rotates silently via /api/auth/refresh.
**Alternatives considered**:
- Session cookies: Incompatible with microservices and mobile clients

## Decision: MFA via TOTP (Time-based One-Time Passwords)

**Decision**: TOTP-based MFA stored in Keycloak (mfa_secret per user).
**Rationale**: TOTP is widely supported (Google Authenticator, Authy), offline-capable, and built into Keycloak's Required Actions.
**Alternatives considered**:
- SMS OTP: Phishable, carrier-dependent, costs money per message

## Decision: Password Reset via Email Token

**Decision**: Server generates a time-limited token, emails it, and validates on submission.
**Rationale**: Industry standard; Keycloak's built-in Forgot Password flow handles this.
**Alternatives considered**:
- Security questions: Deprecated security practice

## Best Practices Confirmed

- JWT signed with RS256 (asymmetric); public key distributed via JWKS endpoint
- Account lockout: exponential backoff after N failed attempts (configurable per tenant)
- Email verification token: UUID v4, expires in 24 hours
- Password reset token: UUID v4, expires in 1 hour (shorter)
- Refresh tokens must be invalidated on logout (Keycloak session revocation)
