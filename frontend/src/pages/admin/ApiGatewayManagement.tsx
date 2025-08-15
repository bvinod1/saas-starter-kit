import React, { useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  EllipsisVerticalIcon,
  GlobeAltIcon,
  ShieldCheckIcon,
  ClockIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const ApiGatewayManagement: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  // Mock data - in real app, this would come from API calls
  const apiEndpoints = [
    {
      id: 'endpoint-1',
      path: '/api/v1/users',
      method: 'GET',
      tenant: 'All Tenants',
      status: 'active',
      rateLimit: '1000/hour',
      responseTime: '45ms',
      errorRate: '0.02%',
      requests24h: 15420,
      lastModified: '2024-01-10',
    },
    {
      id: 'endpoint-2',
      path: '/api/v1/billing',
      method: 'POST',
      tenant: 'Enterprise Only',
      status: 'active',
      rateLimit: '500/hour',
      responseTime: '89ms',
      errorRate: '0.05%',
      requests24h: 8230,
      lastModified: '2024-01-08',
    },
    {
      id: 'endpoint-3',
      path: '/api/v1/notifications',
      method: 'PUT',
      tenant: 'All Tenants',
      status: 'maintenance',
      rateLimit: '2000/hour',
      responseTime: '156ms',
      errorRate: '0.12%',
      requests24h: 12450,
      lastModified: '2024-01-12',
    },
    {
      id: 'endpoint-4',
      path: '/api/v1/analytics',
      method: 'GET',
      tenant: 'Professional+',
      status: 'active',
      rateLimit: '200/hour',
      responseTime: '234ms',
      errorRate: '0.08%',
      requests24h: 5670,
      lastModified: '2024-01-05',
    },
  ];

  const rateLimitRules = [
    {
      id: 'rule-1',
      name: 'Standard User Rate Limit',
      pattern: '/api/v1/users/*',
      limit: '1000/hour',
      burst: '100',
      tenant: 'All Tenants',
      status: 'active',
    },
    {
      id: 'rule-2',
      name: 'Billing API Rate Limit',
      pattern: '/api/v1/billing/*',
      limit: '500/hour',
      burst: '50',
      tenant: 'All Tenants',
      status: 'active',
    },
    {
      id: 'rule-3',
      name: 'Analytics API Rate Limit',
      pattern: '/api/v1/analytics/*',
      limit: '200/hour',
      burst: '20',
      tenant: 'Professional+',
      status: 'active',
    },
  ];

  const apiKeys = [
    {
      id: 'key-1',
      name: 'Acme Corp API Key',
      tenant: 'Acme Corporation',
      key: 'ak_****1234',
      permissions: ['read:users', 'write:users'],
      lastUsed: '2024-01-12 14:30',
      status: 'active',
    },
    {
      id: 'key-2',
      name: 'Global Industries API Key',
      tenant: 'Global Industries',
      key: 'ak_****5678',
      permissions: ['read:all', 'write:all'],
      lastUsed: '2024-01-12 11:15',
      status: 'active',
    },
    {
      id: 'key-3',
      name: 'Startup Inc API Key',
      tenant: 'Startup Inc',
      key: 'ak_****9012',
      permissions: ['read:users'],
      lastUsed: '2023-12-15 16:45',
      status: 'suspended',
    },
  ];

  const filteredEndpoints = apiEndpoints.filter(endpoint => {
    const matchesSearch = endpoint.path.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         endpoint.tenant.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || endpoint.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'active':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-3 w-3 mr-1" />
            Active
          </span>
        );
      case 'maintenance':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ClockIcon className="h-3 w-3 mr-1" />
            Maintenance
          </span>
        );
      case 'deprecated':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Deprecated
          </span>
        );
      default:
        return null;
    }
  };

  const getMethodBadge = (method: string) => {
    const methodColors = {
      'GET': 'bg-green-100 text-green-800',
      'POST': 'bg-blue-100 text-blue-800',
      'PUT': 'bg-yellow-100 text-yellow-800',
      'DELETE': 'bg-red-100 text-red-800',
      'PATCH': 'bg-purple-100 text-purple-800',
    };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${methodColors[method as keyof typeof methodColors] || 'bg-gray-100 text-gray-800'}`}>
        {method}
      </span>
    );
  };

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">API Gateway Management</h1>
        <p className="mt-2 text-sm text-gray-600">
          Monitor API endpoints, rate limits, and manage API keys
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-4 mb-8">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <GlobeAltIcon className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Endpoints</dt>
                  <dd className="text-lg font-medium text-gray-900">24</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ChartBarIcon className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Requests (24h)</dt>
                  <dd className="text-lg font-medium text-gray-900">41.7K</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ClockIcon className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Avg Response Time</dt>
                  <dd className="text-lg font-medium text-gray-900">131ms</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ExclamationTriangleIcon className="h-6 w-6 text-red-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Error Rate</dt>
                  <dd className="text-lg font-medium text-gray-900">0.07%</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="bg-white shadow rounded-lg p-6 mb-6">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div>
            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
              Search Endpoints
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="text"
                id="search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="Search by path or tenant..."
              />
            </div>
          </div>

          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
              Status
            </label>
            <select
              id="status"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            >
              <option value="all">All Statuses</option>
              <option value="active">Active</option>
              <option value="maintenance">Maintenance</option>
              <option value="deprecated">Deprecated</option>
            </select>
          </div>

          <div className="flex items-end">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <FunnelIcon className="h-4 w-4 mr-2" />
              More Filters
            </button>
          </div>
        </div>
      </div>

      {/* API Endpoints */}
      <div className="bg-white shadow rounded-lg mb-8">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">API Endpoints</h3>
          <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
            <table className="min-w-full divide-y divide-gray-300">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Endpoint
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Method
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tenant Access
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Rate Limit
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Performance
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="relative px-6 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredEndpoints.map((endpoint) => (
                  <tr key={endpoint.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{endpoint.path}</div>
                      <div className="text-sm text-gray-500">Last modified: {endpoint.lastModified}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getMethodBadge(endpoint.method)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {endpoint.tenant}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {endpoint.rateLimit}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        <div>Response: {endpoint.responseTime}</div>
                        <div>Error Rate: {endpoint.errorRate}</div>
                        <div>Requests: {endpoint.requests24h.toLocaleString()}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(endpoint.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button className="text-gray-400 hover:text-gray-600">
                        <EllipsisVerticalIcon className="h-5 w-5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Rate Limit Rules */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Rate Limit Rules</h3>
            <div className="space-y-4">
              {rateLimitRules.map((rule) => (
                <div key={rule.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      <ShieldCheckIcon className="h-5 w-5 text-gray-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{rule.name}</span>
                    </div>
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      {rule.status}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Pattern:</span>
                      <span className="ml-2 text-gray-900">{rule.pattern}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Limit:</span>
                      <span className="ml-2 text-gray-900">{rule.limit}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Burst:</span>
                      <span className="ml-2 text-gray-900">{rule.burst}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Tenant:</span>
                      <span className="ml-2 text-gray-900">{rule.tenant}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* API Keys */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">API Keys</h3>
            <div className="space-y-4">
              {apiKeys.map((key) => (
                <div key={key.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      <Cog6ToothIcon className="h-5 w-5 text-gray-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{key.name}</span>
                    </div>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      key.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {key.status}
                    </span>
                  </div>
                  <div className="space-y-2 text-sm">
                    <div>
                      <span className="text-gray-500">Key:</span>
                      <span className="ml-2 text-gray-900 font-mono">{key.key}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Tenant:</span>
                      <span className="ml-2 text-gray-900">{key.tenant}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Permissions:</span>
                      <span className="ml-2 text-gray-900">{key.permissions.join(', ')}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Last Used:</span>
                      <span className="ml-2 text-gray-900">{key.lastUsed}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* API Analytics Placeholder */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">API Analytics</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Request Volume (24h)</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Request volume over time</p>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Response Time Distribution</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Response time distribution</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ApiGatewayManagement;
