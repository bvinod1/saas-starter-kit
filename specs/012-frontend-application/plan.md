# Implementation Plan: Frontend Application

**Branch**: `012-frontend-application` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

React 18 + TypeScript SPA with route-guarded public/auth/protected/admin areas. AuthContext
manages session in localStorage. Admin panel covers all 13 feature management pages (lazy-loaded).
React Query handles caching; Tailwind CSS for styling; React Hook Form for forms; typed Axios
service layer for all API calls. OTel browser SDK and Sentry for frontend observability.

## Technical Context

**Language/Version**: TypeScript 5.x, React 18, Node 20
**Primary Dependencies**: React Router 6, @tanstack/react-query, Axios, React Hook Form, Tailwind CSS, @heroicons/react, react-hot-toast
**Storage**: localStorage (session only)
**Testing**: Jest, React Testing Library, MSW (API mocking)
**Target Platform**: Browser (Chrome/Firefox/Safari/Edge latest 2)
**Project Type**: React SPA (North-South API consumer)
**Performance Goals**: LCP < 3 s on 3G; admin pages lazy-loaded (code-split)
**Constraints**: Auth state persisted on refresh; role-based route guards enforced
**Scale/Scope**: 12 admin pages; 3 auth pages; 4 user pages; 1 landing page

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | All API calls via typed services/api.ts; no direct fetch in components |
| V. Traffic-Aware | PASS | SPA is the North-South consumer of Kong-fronted REST APIs |
| VI. Tenant-Centric | PASS | X-Tenant-ID injected in Axios request interceptor for all authenticated requests |
| VII. Observability | PASS | OTel browser SDK; Sentry for error tracking |
| VIII. Security | PASS | JWT handled per browser security best practice; React XSS defaults |

## Project Structure

### Documentation (this feature)

```
specs/012-frontend-application/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi/   # Generated TypeScript types from consumed API contracts
```

### Source Code (repository root)

```
frontend/
├── src/
│   ├── App.tsx
│   ├── contexts/
│   │   ├── AuthContext.tsx
│   │   └── NotificationContext.tsx
│   ├── components/
│   │   ├── auth/
│   │   └── layout/           # Layout, AuthLayout
│   ├── pages/
│   │   ├── LandingPage.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Billing.tsx
│   │   ├── auth/             # Login, Register, ForgotPassword
│   │   └── admin/            # 12 admin management pages (lazy)
│   └── services/
│       └── api.ts
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── jest.config.js
```

**Structure Decision**: Standard React SPA. Admin pages code-split. services/api.ts is the single API boundary.

## Complexity Tracking

> No constitution violations.
