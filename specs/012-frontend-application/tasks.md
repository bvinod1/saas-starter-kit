# Tasks: Frontend Application

**Feature**: `012-frontend-application`
**Input**: `specs/012-frontend-application/`
**Source**: `frontend/src/`

## Phase 1: Setup

- [ ] T001 Verify `frontend/package.json` includes: react@18, react-router-dom@6, axios, react-hook-form, @tanstack/react-query, tailwindcss; run `npm install`
- [ ] T002 [P] Configure `frontend/tailwind.config.js` with content paths and custom theme tokens
- [ ] T003 [P] Configure `frontend/src/services/api.ts` — Axios instance with base URL, request interceptor (inject `Authorization: Bearer {token}` + `X-Tenant-ID` + `X-Correlation-ID` headers), response interceptor (401 → trigger token refresh)

---

## Phase 2: Foundational

- [ ] T004 Create `frontend/src/contexts/AuthContext.tsx` — `AuthState` interface, `AuthProvider` component (store user + accessToken in-memory, tenantId in localStorage, restore from localStorage on mount, expose `login()`, `logout()`, `refreshToken()`)
- [ ] T005 [P] Create `frontend/src/components/auth/ProtectedRoute.tsx` — `requireAuth` guard (redirect to `/auth/login` if not authenticated), `requireAdmin` guard (redirect to `/app/dashboard` if not admin)
- [ ] T006 [P] Create `frontend/src/components/layout/AppLayout.tsx` — sidebar navigation, top bar with user info and logout button
- [ ] T007 Create `frontend/src/App.tsx` — React Router routes: public (`/`, `/auth/login`, `/auth/register`), protected (`/app/*`), admin (`/admin/*`); wrap with `QueryClientProvider` + `AuthProvider`

**Checkpoint**: Auth context, route guards, and layouts ready

---

## Phase 3: User Story 1 + 2 — Registration, Login & Protected Routes (Priority: P1) 🎯 MVP

**Goal**: Visitor registers and is redirected to login; user logs in and accesses protected dashboard; unauthenticated access redirected.

**Independent Test**: Navigate to `/auth/register` → form renders. Submit valid form → success toast + redirect to `/auth/login`. Login with valid creds → redirect to `/app/dashboard`. Navigate to `/app/dashboard` unauthenticated → redirected to `/auth/login`. Non-admin visits `/admin/*` → redirected to `/app/dashboard`.

- [ ] T008 [P] [US1] Create `frontend/src/pages/auth/RegisterPage.tsx` — form with firstName, lastName, email, password, confirmPassword, companyName using React Hook Form + Tailwind; submit to `POST /api/v1/auth/register` via axios
- [ ] T009 [P] [US1] Create `frontend/src/services/authService.ts` — `register()`, `login()`, `logout()`, `refreshToken()`, `validateToken()` typed API calls
- [ ] T010 [US2] Create `frontend/src/pages/auth/LoginPage.tsx` — form with email, password; on success call `AuthContext.login()`, redirect to `/app/dashboard`; on MFA required show OTP input
- [ ] T011 [US2] Wire `ProtectedRoute` into all `/app/*` routes in `App.tsx`
- [ ] T012 [US1] Add toast notification context (`frontend/src/contexts/ToastContext.tsx`) and `Toast.tsx` component (success/error/info/warning variants)

**Checkpoint**: Registration, login, protected routes, and toasts independently testable

---

## Phase 4: User Story 3 — Dashboard (Priority: P1)

**Goal**: Authenticated user sees KPI cards (active users, API calls, response time) and recent activity feed.

**Independent Test**: Navigate to `/app/dashboard` → 4 KPI cards render with loading spinners → data loads → numbers shown. Activity feed shows latest 10 entries.

- [ ] T013 [P] [US3] Create `frontend/src/services/analyticsService.ts` — `getUsageSummary(tenantId)`, `getActivityFeed(tenantId)`
- [ ] T014 [P] [US3] Create `frontend/src/components/ui/KpiCard.tsx` (metric, value, trend, loading state)
- [ ] T015 [US3] Create `frontend/src/pages/app/DashboardPage.tsx` — use React Query `useQuery(['analytics', tenantId, 'usage'])` for KPIs; `useQuery(['analytics', tenantId, 'activity'])` for feed; show skeleton loader during fetch

**Checkpoint**: Dashboard independently testable

---

## Phase 5: User Story 4 — Billing Page (Priority: P2)

**Goal**: User views current plan, usage metrics with limits, and invoice history.

**Independent Test**: Navigate to `/app/billing` → plan card shows plan name/price/next billing date → usage bars show API calls/storage/users against limits → invoice table lists all invoices.

- [ ] T016 [P] [US4] Create `frontend/src/services/billingService.ts` — `getCurrentSubscription(tenantId)`, `getInvoices(tenantId)`, `listPlans()`
- [ ] T017 [P] [US4] Create `frontend/src/components/billing/UsageBar.tsx` (label, current, limit, warning at 80%)
- [ ] T018 [US4] Create `frontend/src/pages/app/BillingPage.tsx` — plan card, 3 usage bars, invoice table; all data via React Query with stale-while-revalidate

**Checkpoint**: Billing page independently testable

---

## Phase 6: User Story 5 — Admin Panel (Priority: P2)

**Goal**: Admin accesses full management tools (tenants, users, monitoring, security, notifications, API gateway, system config), all code-split.

**Independent Test**: Admin navigates to each `/admin/*` route → correct lazy-loaded page renders. Non-admin cannot access any `/admin/*` route.

- [ ] T019 [P] [US5] Create `frontend/src/pages/admin/TenantManagementPage.tsx` — tenant list with status/plan/user count; `frontend/src/services/tenantService.ts`
- [ ] T020 [P] [US5] Create `frontend/src/pages/admin/UserManagementPage.tsx` — user list, activate/deactivate actions; update `authService.ts` with user management calls
- [ ] T021 [P] [US5] Create `frontend/src/pages/admin/MonitoringDashboardPage.tsx` — per-service health cards from analytics API
- [ ] T022 [P] [US5] Create `frontend/src/pages/admin/SecurityCompliancePage.tsx` — security alert list + compliance scores
- [ ] T023 [P] [US5] Create `frontend/src/pages/admin/BillingManagementPage.tsx`, `NotificationManagementPage.tsx`, `ApiGatewayManagementPage.tsx`, `SystemConfigurationPage.tsx`
- [ ] T024 [US5] Configure all admin pages as `React.lazy()` + `Suspense` in `App.tsx` for code splitting

**Checkpoint**: All admin pages independently accessible and testable

---

## Phase 7: Polish & Cross-Cutting

- [ ] T025 [P] Add `frontend/src/contexts/TenantContext.tsx` — selected tenantId persisted to localStorage `saas.tenantId`
- [ ] T026 [P] Add `npm run build` optimisation: verify LCP < 3 s on simulated 3G with `lighthouse` CI
- [ ] T027 Run `frontend/test-registration.sh` end-to-end smoke test

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T007): Depends on Phase 1
- **Phase 3** (T008–T012): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T013–T015): Depends on Phase 2, Phase 3
- **Phase 5** (T016–T018): Depends on Phase 2
- **Phase 6** (T019–T024): Depends on Phase 2, Phase 3
- **Phase 7** (T025–T027): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working registration, login, and protected routing.
