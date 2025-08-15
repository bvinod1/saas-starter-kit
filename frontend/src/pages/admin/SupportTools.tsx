import React, { useState } from 'react';
import {
  LifebuoyIcon,
  TicketIcon,
  UserIcon,
  WrenchIcon,
  BookOpenIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  ChatBubbleLeftRightIcon,
  DocumentTextIcon,
  CogIcon,
} from '@heroicons/react/24/outline';

const SupportTools: React.FC = () => {
  const [activeTab, setActiveTab] = useState('tickets');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [priorityFilter, setPriorityFilter] = useState('all');
  const [showAddTicket, setShowAddTicket] = useState(false);
  const [showImpersonationModal, setShowImpersonationModal] = useState(false);
  const [showMaintenanceModal, setShowMaintenanceModal] = useState(false);

  // Mock data - in real app, this would come from API calls
  const supportTickets = [
    {
      id: 'TICKET-001',
      title: 'Cannot access billing dashboard',
      description: 'User is unable to view billing information and getting 403 error',
      tenant: 'acme-corp',
      requester: 'john.doe@acme.com',
      assignee: 'support-team',
      status: 'open',
      priority: 'high',
      category: 'billing',
      createdAt: '2024-01-12 14:30',
      updatedAt: '2024-01-12 15:45',
      lastResponse: '2024-01-12 15:45',
      responseTime: '1h 15m',
    },
    {
      id: 'TICKET-002',
      title: 'API rate limiting too aggressive',
      description: 'Customer experiencing 429 errors even with normal usage patterns',
      tenant: 'startup-inc',
      requester: 'dev@startup.com',
      assignee: 'engineering-team',
      status: 'in_progress',
      priority: 'medium',
      category: 'api',
      createdAt: '2024-01-12 13:00',
      updatedAt: '2024-01-12 14:30',
      lastResponse: '2024-01-12 14:30',
      responseTime: '1h 30m',
    },
    {
      id: 'TICKET-003',
      title: 'Data export failing',
      description: 'GDPR data export request failing with timeout error',
      tenant: 'healthcare-sol',
      requester: 'compliance@healthcare.com',
      assignee: 'data-team',
      status: 'resolved',
      priority: 'critical',
      category: 'compliance',
      createdAt: '2024-01-12 10:00',
      updatedAt: '2024-01-12 12:00',
      lastResponse: '2024-01-12 12:00',
      responseTime: '2h 0m',
    },
    {
      id: 'TICKET-004',
      title: 'SSO integration not working',
      description: 'SAML authentication failing after recent update',
      tenant: 'global-ind',
      requester: 'it@globalind.com',
      assignee: 'security-team',
      status: 'waiting',
      priority: 'high',
      category: 'authentication',
      createdAt: '2024-01-12 09:00',
      updatedAt: '2024-01-12 11:00',
      lastResponse: '2024-01-12 11:00',
      responseTime: '2h 0m',
    },
  ];

  const knowledgeBase = [
    {
      id: 'KB-001',
      title: 'Getting Started with Multi-Tenancy',
      category: 'onboarding',
      author: 'admin@company.com',
      status: 'published',
      views: 1250,
      lastUpdated: '2024-01-10',
      tags: ['multi-tenancy', 'onboarding', 'setup'],
    },
    {
      id: 'KB-002',
      title: 'API Rate Limiting Best Practices',
      category: 'api',
      author: 'dev@company.com',
      status: 'published',
      views: 890,
      lastUpdated: '2024-01-08',
      tags: ['api', 'rate-limiting', 'best-practices'],
    },
    {
      id: 'KB-003',
      title: 'Troubleshooting Common Login Issues',
      category: 'authentication',
      author: 'support@company.com',
      status: 'draft',
      views: 0,
      lastUpdated: '2024-01-12',
      tags: ['authentication', 'login', 'troubleshooting'],
    },
    {
      id: 'KB-004',
      title: 'GDPR Compliance Guide',
      category: 'compliance',
      author: 'legal@company.com',
      status: 'published',
      views: 567,
      lastUpdated: '2024-01-05',
      tags: ['gdpr', 'compliance', 'data-protection'],
    },
  ];

  const maintenanceWindows = [
    {
      id: 'MW-001',
      tenant: 'acme-corp',
      type: 'scheduled',
      status: 'upcoming',
      startTime: '2024-01-15 02:00',
      endTime: '2024-01-15 04:00',
      description: 'Database maintenance and optimization',
      affectedServices: ['database', 'api'],
      createdBy: 'admin@company.com',
    },
    {
      id: 'MW-002',
      tenant: 'startup-inc',
      type: 'emergency',
      status: 'active',
      startTime: '2024-01-12 16:00',
      endTime: '2024-01-12 18:00',
      description: 'Security patch deployment',
      affectedServices: ['all'],
      createdBy: 'security@company.com',
    },
  ];

  const filteredTickets = supportTickets.filter(ticket => {
    const matchesSearch = ticket.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         ticket.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         ticket.tenant.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         ticket.requester.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || ticket.status === statusFilter;
    const matchesPriority = priorityFilter === 'all' || ticket.priority === priorityFilter;
    return matchesSearch && matchesStatus && matchesPriority;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'open':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            Open
          </span>
        );
      case 'in_progress':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            In Progress
          </span>
        );
      case 'waiting':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
            Waiting
          </span>
        );
      case 'resolved':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-3 w-3 mr-1" />
            Resolved
          </span>
        );
      case 'closed':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
            Closed
          </span>
        );
      default:
        return null;
    }
  };

  const getPriorityBadge = (priority: string) => {
    switch (priority) {
      case 'low':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            Low
          </span>
        );
      case 'medium':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            Medium
          </span>
        );
      case 'high':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
            High
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

  const getCategoryBadge = (category: string) => {
    const colors = {
      billing: 'bg-purple-100 text-purple-800',
      api: 'bg-blue-100 text-blue-800',
      compliance: 'bg-green-100 text-green-800',
      authentication: 'bg-orange-100 text-orange-800',
      onboarding: 'bg-indigo-100 text-indigo-800',
    };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colors[category as keyof typeof colors] || 'bg-gray-100 text-gray-800'}`}>
        {category.charAt(0).toUpperCase() + category.slice(1)}
      </span>
    );
  };

  return (
    <div>
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Support Tools</h1>
            <p className="mt-2 text-sm text-gray-600">
              Manage support tickets, user impersonation, and maintenance windows
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <WrenchIcon className="h-4 w-4 mr-2" />
              System Status
            </button>
            <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <PlusIcon className="h-4 w-4 mr-2" />
              Create Ticket
            </button>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-8">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'tickets', name: 'Support Tickets', icon: TicketIcon },
            { id: 'impersonation', name: 'User Impersonation', icon: UserIcon },
            { id: 'maintenance', name: 'Maintenance Mode', icon: WrenchIcon },
            { id: 'knowledge', name: 'Knowledge Base', icon: BookOpenIcon },
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

      {/* Support Tickets Tab */}
      {activeTab === 'tickets' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Support Tickets</h3>
                <div className="flex items-center space-x-4">
                  <input
                    type="text"
                    placeholder="Search tickets..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  />
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="all">All Statuses</option>
                    <option value="open">Open</option>
                    <option value="in_progress">In Progress</option>
                    <option value="waiting">Waiting</option>
                    <option value="resolved">Resolved</option>
                    <option value="closed">Closed</option>
                  </select>
                  <select
                    value={priorityFilter}
                    onChange={(e) => setPriorityFilter(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="all">All Priorities</option>
                    <option value="low">Low</option>
                    <option value="medium">Medium</option>
                    <option value="high">High</option>
                    <option value="critical">Critical</option>
                  </select>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ticket</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tenant</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Priority</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Assignee</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Response Time</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredTickets.map((ticket) => (
                      <tr key={ticket.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div>
                            <div className="text-sm font-medium text-gray-900">{ticket.id}</div>
                            <div className="text-sm text-gray-900">{ticket.title}</div>
                            <div className="text-sm text-gray-500">{ticket.category}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">{ticket.tenant}</div>
                          <div className="text-sm text-gray-500">{ticket.requester}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(ticket.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getPriorityBadge(ticket.priority)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {ticket.assignee}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">{ticket.responseTime}</div>
                          <div className="text-sm text-gray-500">{ticket.lastResponse}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex items-center space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              <EyeIcon className="h-4 w-4" />
                            </button>
                            <button className="text-blue-600 hover:text-blue-900">
                              <PencilIcon className="h-4 w-4" />
                            </button>
                            <button className="text-green-600 hover:text-green-900">
                              <ChatBubbleLeftRightIcon className="h-4 w-4" />
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

      {/* User Impersonation Tab */}
      {activeTab === 'impersonation' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">User Impersonation</h3>
                <button
                  onClick={() => setShowImpersonationModal(true)}
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <UserIcon className="h-4 w-4 mr-2" />
                  Impersonate User
                </button>
              </div>
              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-6">
                <div className="flex">
                  <ExclamationTriangleIcon className="h-5 w-5 text-yellow-400" />
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-yellow-800">Security Notice</h3>
                    <div className="mt-2 text-sm text-yellow-700">
                      <p>
                        User impersonation allows you to access the system as another user for support purposes. 
                        All actions performed while impersonating will be logged and audited. Use this feature responsibly.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-gray-900 mb-3">Recent Impersonation Sessions</h4>
                  <div className="space-y-3 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">admin@company.com → john.doe@acme.com</span>
                      <span className="text-gray-500">2 hours ago</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">admin@company.com → dev@startup.com</span>
                      <span className="text-gray-500">1 day ago</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">admin@company.com → compliance@healthcare.com</span>
                      <span className="text-gray-500">3 days ago</span>
                    </div>
                  </div>
                </div>
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-gray-900 mb-3">Impersonation Guidelines</h4>
                  <div className="space-y-3 text-sm text-gray-600">
                    <div className="flex items-start">
                      <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 mt-0.5" />
                      <span>Only impersonate users for legitimate support purposes</span>
                    </div>
                    <div className="flex items-start">
                      <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 mt-0.5" />
                      <span>Always inform the user after resolving their issue</span>
                    </div>
                    <div className="flex items-start">
                      <CheckCircleIcon className="h-4 w-4 text-green-500 mr-2 mt-0.5" />
                      <span>Log all actions performed during impersonation</span>
                    </div>
                    <div className="flex items-start">
                      <ExclamationTriangleIcon className="h-4 w-4 text-yellow-500 mr-2 mt-0.5" />
                      <span>Never access sensitive data without proper authorization</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Maintenance Mode Tab */}
      {activeTab === 'maintenance' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Maintenance Mode Management</h3>
                <button
                  onClick={() => setShowMaintenanceModal(true)}
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                >
                  <WrenchIcon className="h-4 w-4 mr-2" />
                  Schedule Maintenance
                </button>
              </div>
              <div className="bg-blue-50 border border-blue-200 rounded-md p-4 mb-6">
                <div className="flex">
                  <CogIcon className="h-5 w-5 text-blue-400" />
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-blue-800">Maintenance Windows</h3>
                    <div className="mt-2 text-sm text-blue-700">
                      <p>
                        Schedule maintenance windows to minimize disruption to tenants. 
                        You can set tenant-specific or global maintenance periods.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="space-y-4">
                {maintenanceWindows.map((window) => (
                  <div key={window.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center">
                        <span className="text-lg font-medium text-gray-900">{window.tenant}</span>
                        <span className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          window.type === 'scheduled' ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'
                        }`}>
                          {window.type.charAt(0).toUpperCase() + window.type.slice(1)}
                        </span>
                        {getStatusBadge(window.status)}
                      </div>
                      <div className="flex items-center space-x-2">
                        <button className="text-blue-600 hover:text-blue-900 text-sm">
                          Edit
                        </button>
                        <button className="text-red-600 hover:text-red-900 text-sm">
                          Cancel
                        </button>
                      </div>
                    </div>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 text-sm">
                      <div>
                        <span className="text-gray-500">Start:</span>
                        <span className="ml-2 text-gray-900">{window.startTime}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">End:</span>
                        <span className="ml-2 text-gray-900">{window.endTime}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Description:</span>
                        <span className="ml-2 text-gray-900">{window.description}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">Services:</span>
                        <span className="ml-2 text-gray-900">{window.affectedServices.join(', ')}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Knowledge Base Tab */}
      {activeTab === 'knowledge' && (
        <div className="space-y-6">
          <div className="bg-white shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Knowledge Base Management</h3>
                <button className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                  <PlusIcon className="h-4 w-4 mr-2" />
                  Add Article
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Article</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Category</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Views</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Updated</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {knowledgeBase.map((article) => (
                      <tr key={article.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div>
                            <div className="text-sm font-medium text-gray-900">{article.title}</div>
                            <div className="text-sm text-gray-500">by {article.author}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getCategoryBadge(article.category)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(article.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {article.views.toLocaleString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {article.lastUpdated}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex items-center space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              <EyeIcon className="h-4 w-4" />
                            </button>
                            <button className="text-blue-600 hover:text-blue-900">
                              <PencilIcon className="h-4 w-4" />
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

      {/* Impersonation Modal */}
      {showImpersonationModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Impersonate User</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Tenant</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option value="">Select tenant</option>
                    <option value="acme-corp">Acme Corp</option>
                    <option value="startup-inc">Startup Inc</option>
                    <option value="healthcare-sol">Healthcare Solutions</option>
                    <option value="global-ind">Global Industries</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">User Email</label>
                  <input
                    type="email"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    placeholder="user@example.com"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Reason</label>
                  <textarea
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    rows={3}
                    placeholder="Brief description of why you need to impersonate this user"
                  />
                </div>
              </div>
              <div className="mt-6 flex items-center justify-end space-x-3">
                <button
                  onClick={() => setShowImpersonationModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button className="px-4 py-2 border border-transparent rounded-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
                  Start Impersonation
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Maintenance Modal */}
      {showMaintenanceModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Schedule Maintenance Window</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Tenant (or Global)</label>
                  <select className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option value="global">Global (All Tenants)</option>
                    <option value="acme-corp">Acme Corp</option>
                    <option value="startup-inc">Startup Inc</option>
                    <option value="healthcare-sol">Healthcare Solutions</option>
                    <option value="global-ind">Global Industries</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Start Time</label>
                  <input
                    type="datetime-local"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">End Time</label>
                  <input
                    type="datetime-local"
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Description</label>
                  <textarea
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    rows={3}
                    placeholder="What maintenance will be performed?"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Affected Services</label>
                  <div className="mt-2 space-y-2">
                    <label className="flex items-center">
                      <input type="checkbox" className="h-4 w-4 text-blue-600" />
                      <span className="ml-2 text-sm text-gray-700">All Services</span>
                    </label>
                    <label className="flex items-center">
                      <input type="checkbox" className="h-4 w-4 text-blue-600" />
                      <span className="ml-2 text-sm text-gray-700">API</span>
                    </label>
                    <label className="flex items-center">
                      <input type="checkbox" className="h-4 w-4 text-blue-600" />
                      <span className="ml-2 text-sm text-gray-700">Database</span>
                    </label>
                    <label className="flex items-center">
                      <input type="checkbox" className="h-4 w-4 text-blue-600" />
                      <span className="ml-2 text-sm text-gray-700">File Storage</span>
                    </label>
                  </div>
                </div>
              </div>
              <div className="mt-6 flex items-center justify-end space-x-3">
                <button
                  onClick={() => setShowMaintenanceModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button className="px-4 py-2 border border-transparent rounded-md text-sm font-medium text-white bg-red-600 hover:bg-red-700">
                  Schedule Maintenance
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SupportTools;
