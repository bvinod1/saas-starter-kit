# Research: Frontend Application

## Decision: React 18 + TypeScript + React Router 6

**Decision**: Industry-standard React SPA stack. TypeScript for type safety. React Router 6 for declarative nested routing (Layout  -> ProtectedRoute -> Page).
**Rationale**: Existing codebase already uses this stack. Consistency; team familiarity; extensive ecosystem.

## Decision: AuthContext with localStorage Persistence

**Decision**: AuthContext stores user object in React state AND localStorage. Restores session on page refresh via useEffect.
**Rationale**: JWT-based SPA pattern. localStorage survives page reload. Cleared on logout. Note: implement httpOnly cookie alternative if XSS risk increases.

## Decision: React Query for Server State

**Decision**: @tanstack/react-query manages all server state: caching, background refetch, loading/error states.
**Rationale**: Eliminates manual loading state management. Stale-while-revalidate for snappy UX. Prefetch on hover for admin navigation.

## Decision: Axios Interceptor for Tenant Header Injection

**Decision**: Axios request interceptor adds Authorization: Bearer {token} and X-Tenant-ID: {tenantId} to every API call.
**Rationale**: Kong requires these headers. Centralising in interceptor eliminates per-call boilerplate. Token refresh logic in response interceptor handles 401 automatically.

## Decision: Code Splitting for Admin Pages

**Decision**: React.lazy() + Suspense for all admin pages. Public routes (Landing, Login, Register) bundled in main chunk.
**Rationale**: Admin pages are each 50-100KB. Loading all 12 admin pages upfront adds 600-1200KB to initial bundle. Code splitting reduces LCP for auth/user flows.

## Best Practices Confirmed

- react-hot-toast for notifications: toast.success(), toast.error() called after all mutations
- Form validation: React Hook Form with zod resolver for schema-based validation
- Error boundaries around each admin page to contain failures
- ARIA labels on all interactive elements for accessibility
- CSS variables for brand colors (green, red, dark-red) defined in index.css
