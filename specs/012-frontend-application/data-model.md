# Data Model: Frontend Application

> The frontend is a React SPA; it has no persistent store. All data
> is fetched from backend APIs. This file documents client-side
> state shapes and local storage contracts.

## Client-Side Auth State (AuthContext)
```typescript
interface AuthState {
  user: {
    id: string;          // Keycloak user UUID
    email: string;
    firstName: string;
    lastName: string;
    roles: string[];
    tenantId: string;
  } | null;
  accessToken: string | null;   // JWT; stored in memory only
  refreshToken: string | null;  // Stored in httpOnly cookie
  isAuthenticated: boolean;
  isLoading: boolean;
}
```

## LocalStorage Keys
| Key | Value | Notes |
|-----|-------|-------|
| `saas.tenantId` | UUID string | Current active tenant |
| `saas.theme` | `light` \| `dark` | UI preference |
| `saas.locale` | BCP-47 string | e.g. `en-US` |

> **Security note**: Access tokens MUST NOT be stored in localStorage.
> Use in-memory state only; refresh token lives in httpOnly cookie.

## React Query Cache Keys (naming convention)
```
['users', tenantId]
['users', tenantId, userId]
['billing', 'subscription', tenantId]
['billing', 'invoices', tenantId]
['notifications', tenantId, userId]
['analytics', tenantId, 'usage']
['org', tenantId, orgId]
```

## API Request Headers (Axios interceptor)
| Header | Value | Source |
|--------|-------|--------|
| `Authorization` | `Bearer {accessToken}` | AuthContext |
| `X-Tenant-ID` | `{tenantId}` | AuthContext |
| `X-Correlation-ID` | UUID v4 per request | Generated |

## Route → Permission Map
| Route | Required Permission |
|-------|---------------------|
| `/admin/*` | `admin:read` |
| `/billing/*` | `billing:read` |
| `/users/*` | `users:read` |
| `/analytics/*` | `analytics:read` |
| `/settings/*` | `settings:read` |

## Component State Contracts
### LoginPage
```typescript
interface LoginFormState {
  email: string;
  password: string;
  tenantSubdomain: string;
  mfaCode: string | null;
  error: string | null;
  isSubmitting: boolean;
}
```

## Files
- `frontend/src/contexts/` – AuthContext, TenantContext
- `frontend/src/services/` – API service layer
- `frontend/src/components/auth/` – Auth UI components
- `frontend/src/pages/` – Page components
