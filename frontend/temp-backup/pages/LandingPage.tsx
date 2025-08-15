import React from 'react';
import { Link } from 'react-router-dom';

const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-white font-gilroy">
      {/* Navigation */}
      <nav className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-20">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <h1 className="text-3xl font-gilroy-bold text-saas-primary">SaaS Starter Kit</h1>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="hidden md:flex items-center space-x-6 mr-6">
                <a href="#features" className="text-gray-700 hover:text-saas-secondary px-3 py-2 text-sm font-gilroy-medium transition-colors duration-200">
                  Features
                </a>
              </div>
              <Link
                to="/auth/login"
                className="text-gray-700 hover:text-saas-secondary px-4 py-2 text-sm font-gilroy-medium transition-colors duration-200"
              >
                Sign In
              </Link>
              <Link
                to="/auth/register"
                className="text-white px-6 py-3 text-sm font-gilroy-semibold transition-all duration-200 shadow-md hover:shadow-lg"
                style={{ backgroundColor: '#D8362B' }}
              >
                Sign Up
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="py-24 bg-saas-primary">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h1 className="text-5xl md:text-7xl font-gilroy-bold text-white mb-8 leading-tight">
            Build, Extend & Scale<br />
            <span className="text-saas-secondary">Robust SaaS Products</span>
          </h1>
          <p className="text-xl text-gray-300 mb-10 max-w-4xl mx-auto leading-relaxed font-gilroy">
            Complete SaaS starter kit with 15 essential modules including multi-tenancy, authentication, 
            billing, monitoring, and more. Rapidly build production-ready SaaS applications.
          </p>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-20">
            <h2 className="text-5xl font-gilroy-bold mb-6 text-saas-primary">Essential SaaS Modules</h2>
            <p className="text-xl text-gray-600 max-w-4xl mx-auto leading-relaxed font-gilroy">
              Our SaaS starter kit provides 15 essential modules that empower teams to rapidly build, 
              extend, and scale robust SaaS products with enterprise-grade features.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-8">
            {/* Multi-Tenancy Support */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-accent">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Multi-Tenancy Support</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Tenant onboarding, isolation strategies, and context handling for scalable SaaS applications.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Authentication & Authorization */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-primary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Authentication & Authorization</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                IAM integration, SSO, OAuth2, SAML, and MFA with secure JWT token-based authentication.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-primary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Billing & Subscriptions */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-secondary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Billing & Subscriptions</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Payment gateway integration, plans, trials, upgrades/downgrades, and metered billing.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* User Management */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-accent">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">User Management</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                User profiles, roles, permissions, and organizational hierarchy management.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-accent"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Organization Management */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-primary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Organization Management</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Team structures, departments, and cross-functional collaboration tools.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-primary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Notification Service */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-secondary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-5 5v-5zM4.19 4.19A4 4 0 004 6v6a4 4 0 004 4h6a4 4 0 004-4V6a4 4 0 00-4-4H6a4 4 0 00-1.41.19M4 6a2 2 0 00-2 2v6a2 2 0 002 2h6a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-1.41.19" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Notification Service</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Email, SMS, push notifications, and in-app messaging with customizable templates.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Analytics Service */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-accent">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Analytics Service</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Data collection, reporting, dashboards, and business intelligence insights.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-accent"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Monitoring & Observability */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-primary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Monitoring & Observability</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Health checks, metrics tracking, alerting, and comprehensive activity dashboards.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-primary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* API Gateway */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-secondary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">API Gateway</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Request routing, rate limiting, authentication, and API versioning management.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Logging & Tracing */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-accent">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Logging & Tracing</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Centralized logging, distributed tracing, and error tracking for debugging.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-accent"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Security & Compliance */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-primary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Security & Compliance</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Data encryption, audit trails, GDPR compliance, and security best practices.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-primary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Database Management */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-secondary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Database Management</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Multi-tenant database schemas, migrations, backups, and performance optimization.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Deployment & DevOps */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-accent">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Deployment & DevOps</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                CI/CD pipelines, container orchestration, and infrastructure as code.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-accent"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Integration & Webhooks */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-primary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-.758l1.102-1.101a4 4 0 00-5.656-5.656l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-.758l1.102-1.101a4 4 0 00-5.656-5.656l-4 4a4 4 0 105.656 5.656l1.102-1.101" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Integration & Webhooks</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                Third-party integrations, webhook management, and API marketplace support.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-primary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* Admin Dashboard */}
            <div className="bg-white p-8 rounded-lg hover:shadow-xl transition-all duration-300 border border-gray-200 hover:border-saas-secondary">
              <div className="w-16 h-16 rounded-lg flex items-center justify-center mb-6 bg-saas-secondary">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Admin Dashboard</h3>
              <p className="text-gray-600 mb-4 leading-relaxed">
                System administration, user management, and platform configuration tools.
              </p>
              <Link
                to="/products"
                className="font-medium inline-flex items-center transition-colors duration-200 text-saas-secondary"
              >
                Learn more
                <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>
          </div>
        </div>
      </section>



      {/* About Us Section */}
      <section id="about" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-20 items-center">
            <div>
              <h2 className="text-5xl font-gilroy-bold mb-8 leading-tight text-saas-primary">Empowering Teams to Build Better SaaS</h2>
              <p className="text-lg text-gray-600 mb-6 leading-relaxed font-gilroy">
                Building SaaS applications from scratch is complex and time-consuming. Teams often spend months 
                implementing basic infrastructure instead of focusing on their core business logic and user experience.
              </p>
              <p className="text-lg text-gray-600 mb-6 leading-relaxed font-gilroy">
                <strong>We are SaaS Starter Kit.</strong> We provide a comprehensive, production-ready foundation 
                that eliminates the need to build common SaaS infrastructure from scratch.
              </p>
              <p className="text-lg text-gray-600 mb-8 leading-relaxed font-gilroy">
                Our starter kit includes 15 essential modules covering everything from multi-tenancy and authentication 
                to billing, monitoring, and deployment. Built with enterprise-grade security, scalability, and best practices.
              </p>
              <h3 className="text-3xl font-gilroy-semibold mb-6 text-saas-secondary">Focus on what matters most - your product.</h3>
            </div>
            <div className="p-16 rounded-lg border bg-saas-primary bg-opacity-5 border-saas-primary">
              <div className="text-center">
                <div className="w-36 h-36 rounded-full flex items-center justify-center mx-auto mb-8 shadow-lg bg-saas-primary">
                  <svg className="w-20 h-20 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <h4 className="text-2xl font-gilroy-semibold text-gray-900 mb-4">Our Promise</h4>
                <p className="text-lg text-gray-600 leading-relaxed font-gilroy">
                  To <strong>pioneer</strong> digital transformation and drive <strong>superior customer outcomes</strong> 
                  with <strong>precise solutions</strong>.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-20 bg-saas-accent">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="border-t border-gray-600 pt-8 text-center">
            <p className="text-gray-300">© All Rights Reserved 2025 SaaS Starter Kit</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
