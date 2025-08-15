import React from 'react';
import { Link } from 'react-router-dom';
import {
  UsersIcon,
  BuildingOfficeIcon,
  CreditCardIcon,
  ChartBarIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  ServerIcon,
} from '@heroicons/react/24/outline';

const AdminDashboard: React.FC = () => {
  // Mock data - in real app, this would come from API calls
  const stats = [
    { name: 'Total Tenants', value: '156', change: '+12%', changeType: 'positive', icon: BuildingOfficeIcon },
    { name: 'Active Users', value: '2,847', change: '+8%', changeType: 'positive', icon: UsersIcon },
    { name: 'Monthly Revenue', value: '$45,231', change: '+23%', changeType: 'positive', icon: CreditCardIcon },
    { name: 'API Requests', value: '1.2M', change: '+15%', changeType: 'positive', icon: ChartBarIcon },
  ];

  const recentAlerts = [
    { id: 1, type: 'warning', message: 'High API latency detected for tenant-123', time: '2 minutes ago' },
    { id: 2, type: 'error', message: 'Payment failed for tenant-456', time: '15 minutes ago' },
    { id: 3, type: 'info', message: 'New tenant onboarding completed', time: '1 hour ago' },
    { id: 4, type: 'success', message: 'System backup completed successfully', time: '2 hours ago' },
  ];

  const quickActions = [
    { name: 'Add New Tenant', href: '/admin/tenants', icon: BuildingOfficeIcon, color: 'blue' },
    { name: 'Manage Users', href: '/admin/users', icon: UsersIcon, color: 'green' },
    { name: 'View Billing', href: '/admin/billing', icon: CreditCardIcon, color: 'purple' },
    { name: 'System Monitoring', href: '/admin/monitoring', icon: ChartBarIcon, color: 'orange' },
  ];

  const systemHealth = {
    overall: 'Healthy',
    services: [
      { name: 'API Gateway', status: 'healthy', uptime: '99.9%' },
      { name: 'Database', status: 'healthy', uptime: '99.8%' },
      { name: 'Notification Service', status: 'warning', uptime: '98.5%' },
      { name: 'Billing Service', status: 'healthy', uptime: '99.7%' },
    ],
  };

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="mt-2 text-sm text-gray-600">
          Overview of your multi-tenant SaaS platform
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((item) => (
          <div key={item.name} className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <item.icon className="h-6 w-6 text-gray-400" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">{item.name}</dt>
                    <dd className="text-lg font-medium text-gray-900">{item.value}</dd>
                  </dl>
                </div>
              </div>
              <div className="mt-4">
                <span className={`text-sm font-medium ${
                  item.changeType === 'positive' ? 'text-green-600' : 'text-red-600'
                }`}>
                  {item.change}
                </span>
                <span className="text-sm text-gray-500"> from last month</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="mt-8">
        <h2 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {quickActions.map((action) => (
            <Link
              key={action.name}
              to={action.href}
              className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div>
                <span className={`inline-flex p-3 rounded-lg bg-${action.color}-50 text-${action.color}-700 ring-4 ring-white`}>
                  <action.icon className="h-6 w-6" />
                </span>
              </div>
              <div className="mt-8">
                <h3 className="text-lg font-medium">
                  <span className="absolute inset-0" aria-hidden="true" />
                  {action.name}
                </h3>
              </div>
            </Link>
          ))}
        </div>
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Alerts */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Recent Alerts</h3>
            <div className="flow-root">
              <ul className="-mb-8">
                {recentAlerts.map((alert, alertIdx) => (
                  <li key={alert.id}>
                    <div className="relative pb-8">
                      {alertIdx !== recentAlerts.length - 1 ? (
                        <span className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200" aria-hidden="true" />
                      ) : null}
                      <div className="relative flex space-x-3">
                        <div>
                          <span className={`h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white ${
                            alert.type === 'error' ? 'bg-red-500' :
                            alert.type === 'warning' ? 'bg-yellow-500' :
                            alert.type === 'success' ? 'bg-green-500' : 'bg-blue-500'
                          }`}>
                            {alert.type === 'error' ? (
                              <ExclamationTriangleIcon className="h-5 w-5 text-white" />
                            ) : alert.type === 'success' ? (
                              <CheckCircleIcon className="h-5 w-5 text-white" />
                            ) : (
                              <ClockIcon className="h-5 w-5 text-white" />
                            )}
                          </span>
                        </div>
                        <div className="min-w-0 flex-1 pt-1.5 flex justify-between space-x-4">
                          <div>
                            <p className="text-sm text-gray-500">{alert.message}</p>
                          </div>
                          <div className="text-right text-sm whitespace-nowrap text-gray-500">
                            {alert.time}
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        {/* System Health */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">System Health</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-500">Overall Status</span>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  {systemHealth.overall}
                </span>
              </div>
              {systemHealth.services.map((service) => (
                <div key={service.name} className="flex items-center justify-between">
                  <div className="flex items-center">
                    <ServerIcon className="h-4 w-4 text-gray-400 mr-2" />
                    <span className="text-sm text-gray-900">{service.name}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      service.status === 'healthy' ? 'bg-green-100 text-green-800' :
                      service.status === 'warning' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {service.status}
                    </span>
                    <span className="text-xs text-gray-500">{service.uptime}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Recent Admin Activity</h3>
            <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
              <table className="min-w-full divide-y divide-gray-300">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Action
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Admin
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Target
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Time
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      User suspended
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      admin@company.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      user@tenant-123.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      2 minutes ago
                    </td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      Plan upgraded
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      admin@company.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      tenant-456
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      15 minutes ago
                    </td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      New tenant created
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      admin@company.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      tenant-789
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      1 hour ago
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
