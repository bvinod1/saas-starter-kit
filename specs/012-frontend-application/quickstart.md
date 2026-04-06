# Quickstart: Frontend Application

## Prerequisites
- Node.js 18+ and npm 9+
- Backend services running (or use mock API)

## Install dependencies
```bash
cd frontend
npm install
```

## Key environment variables
Create `frontend/.env.local`:
```
REACT_APP_API_BASE_URL=http://localhost:8000
REACT_APP_KEYCLOAK_URL=http://localhost:8080
REACT_APP_KEYCLOAK_REALM=saas-starter
REACT_APP_KEYCLOAK_CLIENT_ID=saas-frontend
```

## Start development server
```bash
cd frontend
npm start
# Opens http://localhost:3000
```

## Run tests
```bash
cd frontend
npm test
# Or in CI mode:
npm test -- --watchAll=false --coverage
```

## Build for production
```bash
cd frontend
npm run build
# Output in frontend/build/
```

## Lint and type-check
```bash
cd frontend
npx tsc --noEmit   # type check
npx eslint src/    # lint
```

## Key pages
| Route | Component |
|-------|-----------|
| `/login` | `src/pages/LoginPage.tsx` |
| `/register` | `src/pages/RegisterPage.tsx` |
| `/dashboard` | `src/pages/Dashboard.tsx` |
| `/admin/*` | `src/pages/admin/` |
| `/billing` | `src/pages/BillingPage.tsx` |
