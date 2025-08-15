import React, { useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowDownTrayIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  ShieldCheckIcon,
  ServerIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const SecurityCompliance: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [severityFilter, setSeverityFilter] = useState('all');
  const [complianceFilter, setComplianceFilter] = useState('all');

  // Mock data - in real app, this would come from API calls
  const securityStats = {
    totalAlerts: 156,
    criticalAlerts: 3,
    highAlerts: 12,
    mediumAlerts: 45,
    lowAlerts: 96,
    complianceScore: 94.2,
    lastScan: '2024-01-12 14:00',
    nextScan: '2024-01-13 14:00',
  };

  const securityAlerts = [
    {
      id: 'alert-1',
      title: 'Multiple failed login attempts detected',
      severity: 'high',
      type: 'authentication',
      tenant: 'startup-inc',
      description: 'User account showing suspicious login patterns from multiple IP addresses',
      timestamp: '2024-01-12 14:30',
      status: 'open',
      assignedTo: 'security-team',
      riskScore: 85,
    },
    {
      id: 'alert-2',
      title: 'Unusual API access pattern detected',
      severity: 'medium',
      type: 'api_security',
      tenant: 'acme-corp',
      description: 'API endpoint /api/v1/users accessed 50x more than usual in last hour',
      timestamp: '2024-01-12 14:25',
      status: 'investigating',
      assignedTo: 'security-team',
      riskScore: 65,
    },
    {
      id: 'alert-3',
      title: 'Database connection from unauthorized IP',
      severity: 'critical',
      type: 'data_security',
      tenant: 'global-ind',
      description: 'Database connection attempt from IP 192.168.100.50 (not in whitelist)',
      timestamp: '2024-01-12 14:20',
      status: 'open',
      assignedTo: 'security-team',
      riskScore: 95,
    },
    {
      id: 'alert-4',
      title: 'Suspicious file upload detected',
      severity: 'medium',
      type: 'malware',
      tenant: 'healthcare-sol',
      description: 'File upload with potential malicious content patterns',
      timestamp: '2024-01-12 14:15',
      status: 'resolved',
      assignedTo: 'security-team',
      riskScore: 70,
    },
  ];

  const complianceChecks = [
    {
      id: 'check-1',
      name: 'GDPR Data Protection',
      status: 'compliant',
      lastCheck: '2024-01-12 14:00',
      nextCheck: '2024-01-19 14:00',
      score: 98.5,
      issues: 2,
      critical: 0,
    },
    {
      id: 'check-2',
      name: 'HIPAA Compliance',
      status: 'compliant',
      lastCheck: '2024-01-12 14:00',
      nextCheck: '2024-01-19 14:00',
      score: 96.2,
      issues: 5,
      critical: 1,
    },
    {
      id: 'check-3',
      name: 'SOC 2 Type II',
      status: 'warning',
      lastCheck: '2024-01-12 14:00',
      nextCheck: '2024-01-19 14:00',
      score: 87.8,
      issues: 12,
      critical: 3,
    },
    {
      id: 'check-4',
      name: 'ISO 27001',
      status: 'non_compliant',
      lastCheck: '2024-01-12 14:00',
      nextCheck: '2024-01-19 14:00',
      score: 72.1,
      issues: 28,
      critical: 8,
    },
  ];

  const auditLogs = [
    {
      id: 'audit-1',
      action: 'User login',
      user: 'admin@company.com',
      tenant: 'platform',
      ip: '192.168.1.100',
      timestamp: '2024-01-12 14:30:15',
      status: 'success',
      details: 'Login successful from authorized IP',
    },
    {
      id: 'audit-2',
      action: 'Permission change',
      user: 'admin@company.com',
      tenant: 'platform',
      ip: '192.168.1.100',
      timestamp: '2024-01-12 14:25:30',
      status: 'success',
      details: 'Changed user role from user to admin for john.doe@acme.com',
    },
    {
      id: 'audit-3',
      action: 'Data export',
      user: 'analyst@globalind.com',
      tenant: 'global-ind',
      ip: '192.168.1.101',
      timestamp: '2024-01-12 14:20:45',
      status: 'success',
      details: 'Exported user data for compliance reporting',
    },
    {
      id: 'audit-4',
      action: 'Failed login attempt',
      user: 'unknown@example.com',
      tenant: 'startup-inc',
      ip: '192.168.1.200',
      timestamp: '2024-01-12 14:15:20',
      status: 'failed',
      details: 'Invalid credentials from suspicious IP',
    },
  ];

  const filteredAlerts = securityAlerts.filter(alert => {
    const matchesSearch = alert.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         alert.tenant.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         alert.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesSeverity = severityFilter === 'all' || alert.severity === severityFilter;
    return matchesSearch && matchesSeverity;
  });

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'critical':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Critical
          </span>
        );
      case 'high':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            High
          </span>
        );
      case 'medium':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Medium
          </span>
        );
      case 'low':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Low
          </span>
        );
      default:
        return null;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'compliant':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-3 w-3 mr-1" />
            Compliant
          </span>
        );
      case 'warning':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Warning
          </span>
        );
      case 'non_compliant':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <ExclamationTriangleIcon className="h-3 w-3 mr-1" />
            Non-Compliant
          </span>
        );
      default:
        return null;
    }
  };

  const getAlertStatusBadge = (status: string) => {
    switch (status) {
      case 'open':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            Open
          </span>
        );
      case 'investigating':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            Investigating
          </span>
        );
      case 'resolved':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            Resolved
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
            <h1 className="text-2xl font-bold text-gray-900">Security & Compliance</h1>
            <p className="mt-2 text-sm text-gray-600">
              Monitor security threats, manage compliance, and audit system access
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
              Export Report
            </button>
            <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <ShieldCheckIcon className="h-4 w-4 mr-2" />
              Run Security Scan
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
                <ExclamationTriangleIcon className="h-6 w-6 text-red-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Alerts</dt>
                  <dd className="text-lg font-medium text-gray-900">{securityStats.totalAlerts}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ShieldCheckIcon className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Compliance Score</dt>
                  <dd className="text-lg font-medium text-gray-900">{securityStats.complianceScore}%</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <ClockIcon className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Last Scan</dt>
                  <dd className="text-lg font-medium text-gray-900">{securityStats.lastScan}</dd>
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
                  <dt className="text-sm font-medium text-gray-500 truncate">Critical Alerts</dt>
                  <dd className="text-lg font-medium text-gray-900">{securityStats.criticalAlerts}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Security Alerts */}
      <div className="bg-white shadow rounded-lg mb-8">
        <div className="px-4 py-5 sm:p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg leading-6 font-medium text-gray-900">Security Alerts</h3>
            <div className="flex items-center space-x-4">
              <input
                type="text"
                placeholder="Search alerts..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              />
              <select
                value={severityFilter}
                onChange={(e) => setSeverityFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="all">All Severities</option>
                <option value="critical">Critical</option>
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </select>
            </div>
          </div>
          <div className="space-y-4">
            {filteredAlerts.map((alert) => (
              <div key={alert.id} className="border border-gray-200 rounded-lg p-4">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-2">
                      <h4 className="text-sm font-medium text-gray-900">{alert.title}</h4>
                      {getSeverityBadge(alert.severity)}
                      {getAlertStatusBadge(alert.status)}
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{alert.description}</p>
                    <div className="flex items-center space-x-4 text-xs text-gray-500">
                      <span>Tenant: {alert.tenant}</span>
                      <span>Type: {alert.type}</span>
                      <span>Risk Score: {alert.riskScore}</span>
                      <span>Assigned: {alert.assignedTo}</span>
                      <span>{alert.timestamp}</span>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button className="text-blue-600 hover:text-blue-800 text-sm">
                      Investigate
                    </button>
                    <button className="text-gray-400 hover:text-gray-600">
                      ⋯
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Compliance Status */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Compliance Status</h3>
            <div className="space-y-4">
              {complianceChecks.map((check) => (
                <div key={check.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center">
                      <Cog6ToothIcon className="h-5 w-5 text-gray-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{check.name}</span>
                    </div>
                    {getStatusBadge(check.status)}
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Score:</span>
                      <span className="ml-2 text-gray-900">{check.score}%</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Issues:</span>
                      <span className="ml-2 text-gray-900">{check.issues}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Critical:</span>
                      <span className="ml-2 text-gray-900">{check.critical}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Next Check:</span>
                      <span className="ml-2 text-gray-900">{check.nextCheck}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Audit Logs */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Recent Audit Logs</h3>
            <div className="space-y-4">
              {auditLogs.map((log) => (
                <div key={log.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center">
                      <Cog6ToothIcon className="h-5 w-5 text-gray-400 mr-2" />
                      <span className="text-sm font-medium text-gray-900">{log.action}</span>
                    </div>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      log.status === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {log.status}
                    </span>
                  </div>
                  <div className="space-y-1 text-sm">
                    <div>
                      <span className="text-gray-500">User:</span>
                      <span className="ml-2 text-gray-900">{log.user}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Tenant:</span>
                      <span className="ml-2 text-gray-900">{log.tenant}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">IP:</span>
                      <span className="ml-2 text-gray-900">{log.ip}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Details:</span>
                      <span className="ml-2 text-gray-900">{log.details}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Time:</span>
                      <span className="ml-2 text-gray-900">{log.timestamp}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Security Metrics */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Security Metrics</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Alerts by Severity (24h)</h4>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-red-600">Critical</span>
                  <span className="font-medium">{securityStats.criticalAlerts}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-orange-600">High</span>
                  <span className="font-medium">{securityStats.highAlerts}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-yellow-600">Medium</span>
                  <span className="font-medium">{securityStats.mediumAlerts}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-blue-600">Low</span>
                  <span className="font-medium">{securityStats.lowAlerts}</span>
                </div>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Compliance Trend</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Compliance score over time</p>
              </div>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Security Events</h4>
              <div className="h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart placeholder - Security events timeline</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Threat Intelligence */}
      <div className="mt-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Threat Intelligence</h3>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <div className="border border-gray-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-900 mb-3">Recent Threats</h4>
              <div className="space-y-3 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Brute Force Attacks</span>
                  <span className="text-red-600 font-medium">+15%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">SQL Injection Attempts</span>
                  <span className="text-green-600 font-medium">-8%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">DDoS Attacks</span>
                  <span className="text-yellow-600 font-medium">+3%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Malware Detection</span>
                  <span className="text-green-600 font-medium">-12%</span>
                </div>
              </div>
            </div>
            <div className="border border-gray-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-gray-900 mb-3">Security Recommendations</h4>
              <div className="space-y-3 text-sm">
                <div className="flex items-start">
                  <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 mt-0.5" />
                  <span className="text-gray-600">Enable MFA for all admin accounts</span>
                </div>
                <div className="flex items-start">
                  <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 mt-0.5" />
                  <span className="text-gray-600">Update API rate limiting rules</span>
                </div>
                <div className="flex items-start">
                  <ExclamationTriangleIcon className="h-4 w-4 text-yellow-500 mr-2 mt-0.5" />
                  <span className="text-gray-600">Review database access permissions</span>
                </div>
                <div className="flex items-start">
                  <ExclamationTriangleIcon className="h-4 w-4 text-yellow-500 mr-2 mt-0.5" />
                  <span className="text-gray-600">Implement IP whitelisting for admin access</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SecurityCompliance;
