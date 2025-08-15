import React, { useState } from 'react';
import {
  CogIcon,
  CloudIcon,
  FlagIcon,
  GlobeAltIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  XCircleIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  EyeSlashIcon,
} from '@heroicons/react/24/outline';

const SystemConfiguration: React.FC = () => {
  const [activeTab, setActiveTab] = useState('settings');
  const [showAddIntegration, setShowAddIntegration] = useState(false);
  const [showAddFeatureFlag, setShowAddFeatureFlag] = useState(false);

  // Mock data - in real app, this would come from API calls
  const platformSettings = {
    platformName: 'SaaS Starter Kit',
    version: '2.1.0',
    environment: 'production',
    maintenanceMode: false,
    debugMode: false,
    timezone: 'UTC',
    dateFormat: 'YYYY-MM-DD',
    maxFileUploadSize: '100MB',
    sessionTimeout: '8 hours',
    maxLoginAttempts: 5,
    passwordPolicy: 'Strong',
  };

  const integrations = [
    {
      id: 'stripe',
      name: 'Stripe',
      type: 'billing',
      status: 'active',
      lastSync: '2024-01-12 14:00',
      configStatus: 'configured',
      apiKey: 'sk_test_...',
      webhookUrl: 'https://api.example.com/webhooks/stripe',
      syncStatus: 'synced',
    },
    {
      id: 'sendgrid',
      name: 'SendGrid',
      type: 'email',
      status: 'active',
      lastSync: '2024-01-12 13:30',
      configStatus: 'configured',
      apiKey: 'SG...',
      webhookUrl: 'https://api.example.com/webhooks/sendgrid',
      syncStatus: 'synced',
    },
    {
      id: 'google-analytics',
      name: 'Google Analytics',
      type: 'analytics',
      status: 'inactive',
      lastSync: '2024-01-11 09:00',
      configStatus: 'configured',
      apiKey: 'GA...',
      webhookUrl: 'https://api.example.com/webhooks/ga',
      syncStatus: 'failed',
    },
    {
      id: 'slack',
      name: 'Slack',
      type: 'notifications',
      status: 'active',
      lastSync: '2024-01-12 14:15',
      configStatus: 'configured',
      apiKey: 'xoxb-...',
      webhookUrl: 'https://api.example.com/webhooks/slack',
      syncStatus: 'synced',
    },
  ];

  const featureFlags = [
    {
      id: 'new-dashboard',
      name: 'New Dashboard UI',
      description: 'Enable the redesigned dashboard interface',
      status: 'enabled',
      rolloutPercentage: 100,
      targetAudience: 'all_users',
      createdAt: '2024-01-10',
      expiresAt: '2024-02-10',
      createdBy: 'admin@company.com',
    },
    {
      id: 'advanced-analytics',
      name: 'Advanced Analytics',
      description: 'Enable enhanced analytics features for enterprise users',
      status: 'enabled',
      rolloutPercentage: 25,
      targetAudience: 'enterprise',
      createdAt: '2024-01-08',
      expiresAt: '2024-01-22',
      createdBy: 'admin@company.com',
    },
    {
      id: 'beta-features',
      name: 'Beta Features',
      description: 'Enable experimental features for beta testers',
      status: 'enabled',
      rolloutPercentage: 10,
      targetAudience: 'beta_users',
      createdAt: '2024-01-05',
      expiresAt: '2024-01-26',
      createdBy: 'admin@company.com',
    },
    {
      id: 'legacy-api',
      name: 'Legacy API Support',
      description: 'Maintain backward compatibility for old API versions',
      status: 'disabled',
      rolloutPercentage: 0,
      targetAudience: 'all_users',
      createdAt: '2023-12-01',
      expiresAt: '2024-03-01',
      createdBy: 'admin@company.com',
    },
  ];

  const environments = [
    {
      name: 'production',
      status: 'active',
      url: 'https://app.example.com',
      database: 'prod-db-cluster',
      lastDeploy: '2024-01-12 14:00',
      version: '2.1.0',
      health: 'healthy',
    },
    {
      name: 'staging',
      status: 'active',
      url: 'https://staging.example.com',
      database: 'staging-db',
      lastDeploy: '2024-01-12 12:00',
      version: '2.1.0-rc1',
      health: 'healthy',
    },
    {
      name: 'development',
      status: 'active',
      url: 'https://dev.example.com',
      database: 'dev-db',
      lastDeploy: '2024-01-12 10:00',
      version: '2.1.0-dev',
      health: 'degraded',
    },
    {
      name: 'testing',
      status: 'inactive',
      url: 'https://test.example.com',
      database: 'test-db',
      lastDeploy: '2024-01-11 16:00',
      version: '2.0.5',
      health: 'unknown',
    },
  ];

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'active':
      case 'enabled':
      case 'healthy':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-3 w-3 mr-1" />
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </span>
        );
      case 'inactive':
      case 'disabled':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
            <XCircleIcon className="h-3 w-3 mr-1" />
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </span>
        );
      case 'degraded':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </span>
        );
      case 'failed':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <XCircleIcon className="h-3 w-3 mr-1" />
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </span>
        );
    }
  };

  const getTypeBadge = (type: string) => {
    const colors = {
      billing: 'bg-purple-100 text-purple-800',
      email: 'bg-blue-100 text-blue-800',
      analytics: 'bg-green-100 text-green-800',
      notifications: 'bg-orange-100 text-orange-800',
    };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colors[type as keyof typeof colors] || 'bg-gray-100 text-gray-800'}`}>
        {type.charAt(0).toUpperCase() + type.slice(1)}
      </span>
    );
  };

  return (
    <div>
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">System Configuration & Integrations</h1>
            <p className="mt-2 text-sm text-gray-600">
              Manage global platform settings, integrations, and feature flags
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <CogIcon className="h-4 w-4 mr-2" />
              System Health Check
            </button>
            <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <PlusIcon className="h-4 w-4 mr-2" />
              Add Integration
            </button>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-8">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'settings', name: 'Platform Settings', icon: CogIcon },
            { id: 'integrations', name: 'Integrations', icon: CloudIcon },
            { id: 'features', name: 'Feature Flags', icon: FlagIcon },
            { id: 'environments', name: 'Environments', icon: GlobeAltIcon },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`${
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
            >
              <tab.icon className="h-4 w-4 mr-2" />
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      {/* Platform Settings Tab */}
      {activeTab === 'settings' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Platform Settings</h3>
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Platform Name</label>
                  <input
                    type="text"
                    value={platformSettings.platformName}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Version</label>
                  <input
                    type="text"
                    value={platformSettings.version}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Environment</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500">
                    <option value="production">Production</option>
                    <option value="staging">Staging</option>
                    <option value="development">Development</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Timezone</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500">
                    <option value="UTC">UTC</option>
                    <option value="EST">EST</option>
                    <option value="PST">PST</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Session Timeout</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500">
                    <option value="4 hours">4 hours</option>
                    <option value="8 hours">8 hours</option>
                    <option value="24 hours">24 hours</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Max Login Attempts</label>
                  <input
                    type="number"
                    value={platformSettings.maxLoginAttempts}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
              <div className="mt-6 flex items-center space-x-4">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={platformSettings.maintenanceMode}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-700">Maintenance Mode</span>
                </label>
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={platformSettings.debugMode}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-700">Debug Mode</span>
                </label>
              </div>
              <div className="mt-6">
                <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                  Save Changes
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Integrations Tab */}
      {activeTab === 'integrations' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Third-Party Integrations</h3>
                <button
                  onClick={() => setShowAddIntegration(true)}
                  className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <PlusIcon className="h-4 w-4 mr-2" />
                  Add Integration
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Integration</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Sync</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Config</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {integrations.map((integration) => (
                      <tr key={integration.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="text-sm font-medium text-gray-900">{integration.name}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getTypeBadge(integration.type)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(integration.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {integration.lastSync}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(integration.configStatus)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex items-center space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              <PencilIcon className="h-4 w-4" />
                            </button>
                            <button className="text-gray-400 hover:text-gray-600">
                              <EyeIcon className="h-4 w-4" />
                            </button>
                            <button className="text-red-600 hover:text-red-900">
                              <TrashIcon className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Feature Flags Tab */}
      {activeTab === 'features' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Feature Flags & A/B Testing</h3>
                <button
                  onClick={() => setShowAddFeatureFlag(true)}
                  className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <PlusIcon className="h-4 w-4 mr-2" />
                  Add Feature Flag
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Feature</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rollout</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Target</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Expires</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {featureFlags.map((flag) => (
                      <tr key={flag.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div>
                            <div className="text-sm font-medium text-gray-900">{flag.name}</div>
                            <div className="text-sm text-gray-500">{flag.description}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(flag.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">{flag.rolloutPercentage}%</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            {flag.targetAudience.replace('_', ' ')}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {flag.expiresAt}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex items-center space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              <PencilIcon className="h-4 w-4" />
                            </button>
                            <button className="text-gray-400 hover:text-gray-600">
                              <EyeIcon className="h-4 w-4" />
                            </button>
                            <button className="text-red-600 hover:text-red-900">
                              <TrashIcon className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Environments Tab */}
      {activeTab === 'environments' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Environment Management</h3>
              <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                {environments.map((env) => (
                  <div key={env.name} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center">
                        <span className="text-lg font-medium text-gray-900 capitalize">{env.name}</span>
                        {getStatusBadge(env.status)}
                      </div>
                      {getStatusBadge(env.health)}
                    </div>
                    <div className="space-y-2 text-sm">
                      <div>
                        <span className="text-gray-500">URL:</span>
                        <span className="ml-2 text-gray-900">{env.url}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Database:</span>
                        <span className="ml-2 text-gray-900">{env.database}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Version:</span>
                        <span className="ml-2 text-gray-900">{env.version}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Last Deploy:</span>
                        <span className="ml-2 text-gray-900">{env.lastDeploy}</span>
                      </div>
                    </div>
                    <div className="mt-4 flex items-center space-x-2">
                      <button className="text-blue-600 hover:text-blue-900 text-sm">
                        Deploy
                      </button>
                      <button className="text-gray-600 hover:text-gray-900 text-sm">
                        Logs
                      </button>
                      <button className="text-gray-600 hover:text-gray-900 text-sm">
                        Monitor
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add Integration Modal */}
      {showAddIntegration && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Add New Integration</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Integration Name</label>
                  <input
                    type="text"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    placeholder="e.g., Stripe, SendGrid"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Type</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option value="billing">Billing</option>
                    <option value="email">Email</option>
                    <option value="analytics">Analytics</option>
                    <option value="notifications">Notifications</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">API Key</label>
                  <input
                    type="password"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    placeholder="Enter API key"
                  />
                </div>
              </div>
              <div className="mt-6 flex items-center justify-end space-x-3">
                <button
                  onClick={() => setShowAddIntegration(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button className="px-4 py-2 border border-transparent rounded-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
                  Add Integration
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add Feature Flag Modal */}
      {showAddFeatureFlag && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Add New Feature Flag</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Feature Name</label>
                  <input
                    type="text"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    placeholder="e.g., new-dashboard"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Description</label>
                  <textarea
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    rows={3}
                    placeholder="Describe the feature"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Target Audience</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option value="all_users">All Users</option>
                    <option value="enterprise">Enterprise</option>
                    <option value="beta_users">Beta Users</option>
                    <option value="admin_only">Admin Only</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Rollout Percentage</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    placeholder="0-100"
                  />
                </div>
              </div>
              <div className="mt-6 flex items-center justify-end space-x-3">
                <button
                  onClick={() => setShowAddFeatureFlag(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button className="px-4 py-2 border border-transparent rounded-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
                  Add Feature Flag
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SystemConfiguration;
