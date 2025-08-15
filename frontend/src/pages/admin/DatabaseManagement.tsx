import React, { useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  ServerIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const DatabaseManagement: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  // Mock data - in real app, this would come from API calls
  const databaseStats = {
    totalSize: '2.4 TB',
    activeConnections: 1247,
    queryPerSecond: 15420,
    avgResponseTime: '12ms',
    uptime: '99.9%',
  };

  const databases = [
    {
      id: 'db-1',
      name: 'user-database',
      type: 'PostgreSQL',
      status: 'healthy',
      size: '450 GB',
      connections: 234,
      queriesPerSecond: 3200,
      responseTime: '8ms',
      uptime: '99.9%',
      lastBackup: '2024-01-12 02:00',
      replication: 'active',
    },
    {
      id: 'db-2',
      name: 'billing-database',
      type: 'PostgreSQL',
      status: 'healthy',
      size: '890 GB',
      connections: 156,
      queriesPerSecond: 2100,
      responseTime: '15ms',
      uptime: '99.8%',
      lastBackup: '2024-01-12 02:00',
      replication: 'active',
    },
    {
      id: 'db-3',
      name: 'analytics-database',
      type: 'ClickHouse',
      status: 'warning',
      size: '1.2 TB',
      connections: 89,
      queriesPerSecond: 4500,
      responseTime: '45ms',
      uptime: '98.5%',
      lastBackup: '2024-01-11 02:00',
      replication: 'lagging',
    },
    {
      id: 'db-4',
      name: 'cache-database',
      type: 'Redis',
      status: 'healthy',
      size: '45 GB',
      connections: 567,
      queriesPerSecond: 8900,
      responseTime: '2ms',
      uptime: '99.9%',
      lastBackup: '2024-01-12 02:00',
      replication: 'active',
    },
  ];

  const tenantData = [
    {
      tenant: 'Acme Corporation',
      databaseSize: '125 GB',
      tableCount: 45,
      indexCount: 234,
      lastOptimization: '2024-01-10',
      performance: 'excellent',
    },
    {
      tenant: 'Global Industries',
      databaseSize: '890 GB',
      tableCount: 128,
      indexCount: 567,
      lastOptimization: '2024-01-08',
      performance: 'good',
    },
    {
      tenant: 'Startup Inc',
      databaseSize: '45 GB',
      tableCount: 23,
      indexCount: 89,
      lastOptimization: '2024-01-05',
      performance: 'excellent',
    },
    {
      tenant: 'Healthcare Solutions',
      databaseSize: '567 GB',
      tableCount: 89,
      indexCount: 345,
      lastOptimization: '2024-01-09',
      performance: 'good',
    },
  ];

  const slowQueries = [
    {
      id: 'query-1',
      sql: 'SELECT * FROM users WHERE email LIKE \'%test%\' ORDER BY created_at DESC',
      duration: '2.3s',
      database: 'user-database',
      tenant: 'Global Industries',
      timestamp: '2024-01-12 14:25',
      executionCount: 15,
    },
    {
      id: 'query-2',
      sql: 'SELECT COUNT(*) FROM billing_transactions WHERE created_at > NOW() - INTERVAL \'30 days\'',
      duration: '1.8s',
      database: 'billing-database',
      tenant: 'Healthcare Solutions',
      timestamp: '2024-01-12 14:20',
      executionCount: 8,
    },
    {
      id: 'query-3',
      sql: 'SELECT * FROM analytics_events WHERE event_type = \'page_view\' AND timestamp > \'2024-01-01\'',
      duration: '3.2s',
      database: 'analytics-database',
      tenant: 'Acme Corporation',
      timestamp: '2024-01-12 14:15',
      executionCount: 23,
    },
  ];

  const filteredDatabases = databases.filter(db => {
    const matchesSearch = db.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         db.type.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || db.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

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
      case 'critical':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Critical
          </span>
        );
      default:
        return null;
    }
  };

  const getPerformanceBadge = (performance: string) => {
    switch (performance) {
      case 'excellent':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            Excellent
          </span>
        );
      case 'good':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            Good
          </span>
        );
      case 'poor':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            Poor
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
            <h1 className="text-2xl font-bold text-gray-900">Database Management</h1>
            <p className="mt-2 text-sm text-gray-600">
              Monitor database health, performance, and manage backups
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
              Export Report
            </button>
            <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <Cog6ToothIcon className="h-4 w-4 mr-2" />
              Database Settings
            </button>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-5 mb-8">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                {/* DatabaseIcon was removed, so this will be empty or replaced with a placeholder */}
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Size</dt>
                  <dd className="text-lg font-medium text-gray-900">{databaseStats.totalSize}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ServerIcon className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Active Connections</dt>
                  <dd className="text-lg font-medium text-gray-900">{databaseStats.activeConnections.toLocaleString()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ChartBarIcon className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Queries/sec</dt>
                  <dd className="text-lg font-medium text-gray-900">{databaseStats.queryPerSecond.toLocaleString()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ClockIcon className="h-6 w-6 text-indigo-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Avg Response</dt>
                  <dd className="text-lg font-medium text-gray-900">{databaseStats.avgResponseTime}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <CheckCircleIcon className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Uptime</dt>
                  <dd className="text-lg font-medium text-gray-900">{databaseStats.uptime}</dd>
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
              Search Databases
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
                placeholder="Search by name or type..."
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
              <option value="healthy">Healthy</option>
              <option value="warning">Warning</option>
              <option value="critical">Critical</option>
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

      {/* Databases Table */}
      <div className="bg-white shadow rounded-lg mb-8">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Database Instances</h3>
          <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
            <table className="min-w-full divide-y divide-gray-300">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Database
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Size & Connections
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Performance
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Backup & Replication
                  </th>
                  <th className="relative px-6 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredDatabases.map((db) => (
                  <tr key={db.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-lg bg-blue-100 flex items-center justify-center">
                            {/* DatabaseIcon was removed, so this will be empty or replaced with a placeholder */}
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{db.name}</div>
                          <div className="text-sm text-gray-500">{db.type}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(db.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        <div>Size: {db.size}</div>
                        <div>Connections: {db.connections}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        <div>QPS: {db.queriesPerSecond.toLocaleString()}</div>
                        <div>Response: {db.responseTime}</div>
                        <div>Uptime: {db.uptime}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        <div>Last Backup: {db.lastBackup}</div>
                        <div>Replication: {db.replication}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button className="text-blue-600 hover:text-blue-900 mr-3">
                        Monitor
                      </button>
                      <button className="text-gray-400 hover:text-gray-600">
                        ⋯
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
        {/* Tenant Database Usage */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Tenant Database Usage</h3>
            <div className="space-y-4">
              {tenantData.map((tenant) => (
                <div key={tenant.tenant} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      {/* DatabaseIcon was removed, so this will be empty or replaced with a placeholder */}
                      <span className="text-sm font-medium text-gray-900">{tenant.tenant}</span>
                    </div>
                    {getPerformanceBadge(tenant.performance)}
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Size:</span>
                      <span className="ml-2 text-gray-900">{tenant.databaseSize}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Tables:</span>
                      <span className="ml-2 text-gray-900">{tenant.tableCount}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Indexes:</span>
                      <span className="ml-2 text-gray-900">{tenant.indexCount}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Last Optimized:</span>
                      <span className="ml-2 text-gray-900">{tenant.lastOptimization}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Slow Queries */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Slow Queries</h3>
            <div className="space-y-4">
              {slowQueries.map((query) => (
                <div key={query.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      <ClockIcon className="h-5 w-5 text-red-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{query.duration}</span>
                    </div>
                    <span className="text-xs text-gray-500">{query.executionCount} executions</span>
                  </div>
                  <div className="space-y-2 text-sm">
                    <div>
                      <span className="text-gray-500">Database:</span>
                      <span className="ml-2 text-gray-900">{query.database}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Tenant:</span>
                      <span className="ml-2 text-gray-900">{query.tenant}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">SQL:</span>
                      <div className="mt-1 p-2 bg-gray-50 rounded text-xs font-mono text-gray-700">
                        {query.sql}
                      </div>
                    </div>
                    <div>
                      <span className="text-gray-500">Timestamp:</span>
                      <span className="ml-2 text-gray-900">{query.timestamp}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Database Analytics Placeholder */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Database Analytics</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Query Performance Over Time</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Query performance trends</p>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Connection Pool Usage</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Connection pool utilization</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Backup Management */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Backup Management</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div className="border border-gray-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-900 mb-2">Last Backup Status</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">User Database:</span>
                  <span className="text-green-600">✓ Completed</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Billing Database:</span>
                  <span className="text-green-600">✓ Completed</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Analytics Database:</span>
                  <span className="text-yellow-600">⚠ In Progress</span>
                </div>
              </div>
            </div>
            <div className="border border-gray-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-900 mb-2">Backup Schedule</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Daily:</span>
                  <span className="text-gray-900">2:00 AM</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Weekly:</span>
                  <span className="text-gray-900">Sunday 3:00 AM</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Monthly:</span>
                  <span className="text-gray-900">1st of month</span>
                </div>
              </div>
            </div>
            <div className="border border-gray-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-900 mb-2">Storage Usage</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Backup Storage:</span>
                  <span className="text-gray-900">1.8 TB</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Retention:</span>
                  <span className="text-gray-900">90 days</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Next Cleanup:</span>
                  <span className="text-gray-900">2024-02-01</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DatabaseManagement;
