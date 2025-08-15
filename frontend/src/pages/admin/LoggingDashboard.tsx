import React, { useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  DocumentTextIcon,
  ServerIcon,
  ChartBarIcon,
  Cog6ToothIcon,
  InformationCircleIcon,
} from '@heroicons/react/24/outline';

const LoggingDashboard: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [levelFilter, setLevelFilter] = useState('all');
  const [serviceFilter, setServiceFilter] = useState('all');
  const [tenantFilter, setTenantFilter] = useState('all');

  // Mock data - in real app, this would come from API calls
  const logs = [
    {
      id: 'log-1',
      timestamp: '2024-01-12 14:30:15',
      level: 'info',
      service: 'user-service',
      tenant: 'acme-corp',
      message: 'User login successful: john.doe@acme.com',
      traceId: 'trace-123-456-789',
      userId: 'user-123',
      ip: '192.168.1.100',
    },
    {
      id: 'log-2',
      timestamp: '2024-01-12 14:30:12',
      level: 'warn',
      service: 'billing-service',
      tenant: 'global-ind',
      message: 'Payment processing delayed: retry attempt 2',
      traceId: 'trace-123-456-790',
      userId: 'user-456',
      ip: '192.168.1.101',
    },
    {
      id: 'log-3',
      timestamp: '2024-01-12 14:30:08',
      level: 'error',
      service: 'notification-service',
      tenant: 'startup-inc',
      message: 'Failed to send SMS notification: invalid phone number',
      traceId: 'trace-123-456-791',
      userId: 'user-789',
      ip: '192.168.1.102',
    },
    {
      id: 'log-4',
      timestamp: '2024-01-12 14:30:05',
      level: 'debug',
      service: 'api-gateway',
      tenant: 'healthcare-sol',
      message: 'API request processed: /api/v1/users/123',
      traceId: 'trace-123-456-792',
      userId: 'user-101',
      ip: '192.168.1.103',
    },
  ];

  const logStats = {
    totalLogs: '2.4M',
    logs24h: '45.2K',
    errorRate: '0.8%',
    avgResponseTime: '67ms',
  };

  const services = [
    { name: 'user-service', logs24h: 12500, errorRate: '0.2%' },
    { name: 'billing-service', logs24h: 8900, errorRate: '1.1%' },
    { name: 'notification-service', logs24h: 15600, errorRate: '2.3%' },
    { name: 'api-gateway', logs24h: 23400, errorRate: '0.1%' },
    { name: 'database', logs24h: 6700, errorRate: '0.05%' },
  ];

  const filteredLogs = logs.filter(log => {
    const matchesSearch = log.message.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         log.traceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         log.userId.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesLevel = levelFilter === 'all' || log.level === levelFilter;
    const matchesService = serviceFilter === 'all' || log.service === serviceFilter;
    const matchesTenant = tenantFilter === 'all' || log.tenant === tenantFilter;
    return matchesSearch && matchesLevel && matchesService && matchesTenant;
  });

  const getLevelBadge = (level: string) => {
    switch (level) {
      case 'error':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            ERROR
          </span>
        );
      case 'warn':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            WARN
          </span>
        );
      case 'info':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            <InformationCircleIcon className="h-3 w-3 mr-1" />
            INFO
          </span>
        );
      case 'debug':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
            <DocumentTextIcon className="h-3 w-3 mr-1" />
            DEBUG
          </span>
        );
      default:
        return null;
    }
  };

  const getServiceBadge = (service: string) => {
    const serviceColors = {
      'user-service': 'bg-blue-100 text-blue-800',
      'billing-service': 'bg-green-100 text-green-800',
      'notification-service': 'bg-purple-100 text-purple-800',
      'api-gateway': 'bg-indigo-100 text-indigo-800',
      'database': 'bg-orange-100 text-orange-800',
    };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${serviceColors[service as keyof typeof serviceColors] || 'bg-gray-100 text-gray-800'}`}>
        {service}
      </span>
    );
  };

  return (
    <div>
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Logging Dashboard</h1>
            <p className="mt-2 text-sm text-gray-600">
              Centralized logging, log analysis, and distributed tracing
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
              Export Logs
            </button>
            <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              View Traces
            </button>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-4 mb-8">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <DocumentTextIcon className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Logs</dt>
                  <dd className="text-lg font-medium text-gray-900">{logStats.totalLogs}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ClockIcon className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Logs (24h)</dt>
                  <dd className="text-lg font-medium text-gray-900">{logStats.logs24h}</dd>
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
                  <dd className="text-lg font-medium text-gray-900">{logStats.errorRate}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ServerIcon className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Avg Response</dt>
                  <dd className="text-lg font-medium text-gray-900">{logStats.avgResponseTime}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="bg-white shadow rounded-lg p-6 mb-6">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-5">
          <div>
            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
              Search Logs
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
                placeholder="Search logs, trace IDs, or messages..."
              />
            </div>
          </div>

          <div>
            <label htmlFor="level" className="block text-sm font-medium text-gray-700 mb-2">
              Log Level
            </label>
            <select
              id="level"
              value={levelFilter}
              onChange={(e) => setLevelFilter(e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            >
              <option value="all">All Levels</option>
              <option value="error">Error</option>
              <option value="warn">Warning</option>
              <option value="info">Info</option>
              <option value="debug">Debug</option>
            </select>
          </div>

          <div>
            <label htmlFor="service" className="block text-sm font-medium text-gray-700 mb-2">
              Service
            </label>
            <select
              id="service"
              value={serviceFilter}
              onChange={(e) => setServiceFilter(e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            >
              <option value="all">All Services</option>
              <option value="user-service">User Service</option>
              <option value="billing-service">Billing Service</option>
              <option value="notification-service">Notification Service</option>
              <option value="api-gateway">API Gateway</option>
              <option value="database">Database</option>
            </select>
          </div>

          <div>
            <label htmlFor="tenant" className="block text-sm font-medium text-gray-700 mb-2">
              Tenant
            </label>
            <select
              id="tenant"
              value={tenantFilter}
              onChange={(e) => setTenantFilter(e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md leading-5 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            >
              <option value="all">All Tenants</option>
              <option value="acme-corp">Acme Corp</option>
              <option value="global-ind">Global Industries</option>
              <option value="startup-inc">Startup Inc</option>
              <option value="healthcare-sol">Healthcare Solutions</option>
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

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Logs Table */}
        <div className="lg:col-span-2">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Recent Logs</h3>
              <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                <table className="min-w-full divide-y divide-gray-300">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Timestamp
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Level
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Service
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Message
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Trace ID
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredLogs.map((log) => (
                      <tr key={log.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {log.timestamp}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getLevelBadge(log.level)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getServiceBadge(log.service)}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-900">
                          <div className="max-w-xs truncate">{log.message}</div>
                          <div className="text-xs text-gray-500 mt-1">
                            Tenant: {log.tenant} | User: {log.userId} | IP: {log.ip}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                          {log.traceId}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Service Logs Summary */}
        <div>
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Service Logs Summary</h3>
              <div className="space-y-4">
                {services.map((service) => (
                  <div key={service.name} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center">
                        <ServerIcon className="h-5 w-5 text-gray-400 mr-2" />
                        <span className="text-sm font-medium text-gray-900">{service.name}</span>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div>
                        <span className="text-gray-500">Logs (24h):</span>
                        <span className="ml-2 text-gray-900">{service.logs24h.toLocaleString()}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Error Rate:</span>
                        <span className="ml-2 text-gray-900">{service.errorRate}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Log Analytics Placeholder */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Log Analytics</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Log Volume by Level (24h)</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Log volume by level</p>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Log Volume by Service (24h)</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Log volume by service</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Distributed Tracing */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Distributed Tracing</h3>
          <div className="space-y-4">
            <div className="border border-gray-200 rounded-lg p-4">
              <div className="flex items-center justify-between mb-3">
                <h4 className="text-sm font-medium text-gray-900">Trace: trace-123-456-789</h4>
                <span className="text-sm text-gray-500">Duration: 156ms</span>
              </div>
              <div className="space-y-2">
                <div className="flex items-center text-sm">
                  <div className="w-4 h-4 bg-blue-500 rounded-full mr-2"></div>
                  <span className="text-gray-600">API Gateway (23ms)</span>
                </div>
                <div className="flex items-center text-sm">
                  <div className="w-4 h-4 bg-green-500 rounded-full mr-2"></div>
                  <span className="text-gray-600">User Service (67ms)</span>
                </div>
                <div className="flex items-center text-sm">
                  <div className="w-4 h-4 bg-purple-500 rounded-full mr-2"></div>
                  <span className="text-gray-600">Database (12ms)</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoggingDashboard;
