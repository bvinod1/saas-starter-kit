import React, { useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  ServerIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const MonitoringDashboard: React.FC = () => {
  const [selectedTimeRange, setSelectedTimeRange] = useState('24h');

  // Mock data - in real app, this would come from API calls
  const systemMetrics = {
    overall: 'healthy',
    uptime: '99.9%',
    responseTime: '45ms',
    errorRate: '0.02%',
    activeConnections: 1247,
  };

  const services = [
    {
      name: 'API Gateway',
      status: 'healthy',
      uptime: '99.9%',
      responseTime: '23ms',
      errorRate: '0.01%',
      cpu: 45,
      memory: 62,
      disk: 28,
    },
    {
      name: 'User Service',
      status: 'healthy',
      uptime: '99.8%',
      responseTime: '67ms',
      errorRate: '0.03%',
      cpu: 38,
      memory: 71,
      disk: 35,
    },
    {
      name: 'Billing Service',
      status: 'healthy',
      uptime: '99.7%',
      responseTime: '89ms',
      errorRate: '0.02%',
      cpu: 52,
      memory: 58,
      disk: 42,
    },
    {
      name: 'Notification Service',
      status: 'warning',
      uptime: '98.5%',
      responseTime: '156ms',
      errorRate: '0.08%',
      cpu: 78,
      memory: 85,
      disk: 67,
    },
    {
      name: 'Database',
      status: 'healthy',
      uptime: '99.9%',
      responseTime: '12ms',
      errorRate: '0.01%',
      cpu: 41,
      memory: 69,
      disk: 55,
    },
  ];

  const alerts = [
    {
      id: 1,
      severity: 'warning',
      service: 'Notification Service',
      message: 'High CPU usage detected (78%)',
      time: '2 minutes ago',
      acknowledged: false,
    },
    {
      id: 2,
      severity: 'info',
      service: 'API Gateway',
      message: 'Traffic spike detected - 2x normal load',
      time: '15 minutes ago',
      acknowledged: true,
    },
    {
      id: 3,
      severity: 'error',
      service: 'Database',
      message: 'Connection pool exhaustion warning',
      time: '1 hour ago',
      acknowledged: false,
    },
  ];

  const tenantMetrics = [
    {
      tenant: 'Acme Corporation',
      apiCalls: 45000,
      responseTime: '45ms',
      errorRate: '0.02%',
      status: 'healthy',
    },
    {
      tenant: 'Global Industries',
      apiCalls: 125000,
      responseTime: '67ms',
      errorRate: '0.04%',
      status: 'healthy',
    },
    {
      tenant: 'Startup Inc',
      apiCalls: 8000,
      responseTime: '89ms',
      errorRate: '0.12%',
      status: 'warning',
    },
    {
      tenant: 'Healthcare Solutions',
      apiCalls: 95000,
      responseTime: '56ms',
      errorRate: '0.03%',
      status: 'healthy',
    },
  ];

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'healthy':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-3 w-3 mr-1" />
            Healthy
          </span>
        );
      case 'warning':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Warning
          </span>
        );
      case 'error':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Error
          </span>
        );
      default:
        return null;
    }
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'error':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            Error
          </span>
        );
      case 'warning':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            Warning
          </span>
        );
      case 'info':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            Info
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">System Monitoring</h1>
            <p className="mt-2 text-sm text-gray-600">
              Real-time system health, performance metrics, and alerts
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <select
              value={selectedTimeRange}
              onChange={(e) => setSelectedTimeRange(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="1h">Last Hour</option>
              <option value="24h">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
            </select>
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <ChartBarIcon className="h-4 w-4 mr-2" />
              Export Report
            </button>
          </div>
        </div>
      </div>

      {/* System Overview */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-5 mb-8">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ServerIcon className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Overall Status</dt>
                  <dd className="text-lg font-medium text-gray-900">{systemMetrics.overall}</dd>
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
                  <dt className="text-sm font-medium text-gray-500 truncate">Uptime</dt>
                  <dd className="text-lg font-medium text-gray-900">{systemMetrics.uptime}</dd>
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
                  <dt className="text-sm font-medium text-gray-500 truncate">Response Time</dt>
                  <dd className="text-lg font-medium text-gray-900">{systemMetrics.responseTime}</dd>
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
                  <dd className="text-lg font-medium text-gray-900">{systemMetrics.errorRate}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ServerIcon className="h-6 w-6 text-indigo-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Active Connections</dt>
                  <dd className="text-lg font-medium text-gray-900">{systemMetrics.activeConnections.toLocaleString()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Service Health */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Service Health</h3>
            <div className="space-y-4">
              {services.map((service) => (
                <div key={service.name} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      <ServerIcon className="h-5 w-5 text-gray-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{service.name}</span>
                    </div>
                    {getStatusBadge(service.status)}
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Uptime:</span>
                      <span className="ml-2 text-gray-900">{service.uptime}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Response:</span>
                      <span className="ml-2 text-gray-900">{service.responseTime}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">CPU:</span>
                      <span className="ml-2 text-gray-900">{service.cpu}%</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Memory:</span>
                      <span className="ml-2 text-gray-900">{service.memory}%</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Active Alerts */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Active Alerts</h3>
            <div className="space-y-4">
              {alerts.map((alert) => (
                <div key={alert.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center mb-2">
                        {getSeverityBadge(alert.severity)}
                        <span className="ml-2 text-sm text-gray-500">{alert.time}</span>
                      </div>
                      <p className="text-sm font-medium text-gray-900">{alert.service}</p>
                      <p className="text-sm text-gray-600">{alert.message}</p>
                    </div>
                    <button className="text-sm text-blue-600 hover:text-blue-800">
                      {alert.acknowledged ? 'Acknowledged' : 'Acknowledge'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Tenant Performance */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Tenant Performance Metrics</h3>
            <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
              <table className="min-w-full divide-y divide-gray-300">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tenant
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      API Calls (24h)
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Response Time
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Error Rate
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {tenantMetrics.map((tenant) => (
                    <tr key={tenant.tenant} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {tenant.tenant}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {tenant.apiCalls.toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {tenant.responseTime}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {tenant.errorRate}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(tenant.status)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Performance Charts Placeholder */}
      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Response Time Trends</h3>
          <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
            <p className="text-gray-500">Chart placeholder - Response time over time</p>
          </div>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Error Rate Trends</h3>
          <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
            <p className="text-gray-500">Chart placeholder - Error rate over time</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MonitoringDashboard;
