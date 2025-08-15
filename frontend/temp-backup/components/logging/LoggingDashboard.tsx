import React, { useState, useEffect } from 'react';
import loggingService, { LogLevel, LogEntry } from '../../services/loggingService';
import { 
  MagnifyingGlassIcon, 
  TrashIcon, 
  DocumentArrowDownIcon,
  FunnelIcon,
  EyeIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon
} from '@heroicons/react/24/outline';

interface LoggingDashboardProps {
  isOpen: boolean;
  onClose: () => void;
}

const LoggingDashboard: React.FC<LoggingDashboardProps> = ({ isOpen, onClose }) => {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [filteredLogs, setFilteredLogs] = useState<LogEntry[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedLevels, setSelectedLevels] = useState<LogLevel[]>([
    LogLevel.ERROR, LogLevel.WARN, LogLevel.INFO, LogLevel.DEBUG
  ]);
  const [selectedComponents, setSelectedComponents] = useState<string[]>([]);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [expandedLogs, setExpandedLogs] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (isOpen) {
      loadLogs();
      if (autoRefresh) {
        const interval = setInterval(loadLogs, 5000);
        return () => clearInterval(interval);
      }
    }
  }, [isOpen, autoRefresh]);

  useEffect(() => {
    filterLogs();
  }, [logs, searchTerm, selectedLevels, selectedComponents]);

  const loadLogs = () => {
    const storedLogs = loggingService.getStoredLogs();
    setLogs(storedLogs);
  };

  const filterLogs = () => {
    let filtered = logs.filter(log => {
      // Level filter
      if (!selectedLevels.includes(log.level)) return false;
      
      // Component filter
      if (selectedComponents.length > 0 && log.context.component) {
        if (!selectedComponents.includes(log.context.component)) return false;
      }
      
      // Search term filter
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        return (
          log.message.toLowerCase().includes(searchLower) ||
          log.context.component?.toLowerCase().includes(searchLower) ||
          log.context.userId?.toLowerCase().includes(searchLower) ||
          log.context.tenantId?.toLowerCase().includes(searchLower)
        );
      }
      
      return true;
    });
    
    setFilteredLogs(filtered);
  };

  const clearLogs = () => {
    loggingService.clearStoredLogs();
    setLogs([]);
    setFilteredLogs([]);
  };

  const exportLogs = () => {
    const dataStr = JSON.stringify(logs, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `app-logs-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    URL.revokeObjectURL(url);
  };

  const toggleLogExpansion = (logId: string) => {
    const newExpanded = new Set(expandedLogs);
    if (newExpanded.has(logId)) {
      newExpanded.delete(logId);
    } else {
      newExpanded.add(logId);
    }
    setExpandedLogs(newExpanded);
  };

  const getLevelIcon = (level: LogLevel) => {
    switch (level) {
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        return <XCircleIcon className="w-4 h-4 text-red-500" />;
      case LogLevel.WARN:
        return <ExclamationTriangleIcon className="w-4 h-4 text-yellow-500" />;
      case LogLevel.INFO:
        return <InformationCircleIcon className="w-4 h-4 text-blue-500" />;
      default:
        return <EyeIcon className="w-4 h-4 text-gray-500" />;
    }
  };

  const getLevelColor = (level: LogLevel) => {
    switch (level) {
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        return 'bg-red-50 border-red-200';
      case LogLevel.WARN:
        return 'bg-yellow-50 border-yellow-200';
      case LogLevel.INFO:
        return 'bg-blue-50 border-blue-200';
      case LogLevel.DEBUG:
        return 'bg-gray-50 border-gray-200';
      default:
        return 'bg-gray-50 border-gray-200';
    }
  };

  const getUniqueComponents = () => {
    const components = new Set<string>();
    logs.forEach(log => {
      if (log.context.component) {
        components.add(log.context.component);
      }
    });
    return Array.from(components).sort();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-11/12 h-5/6 flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-900">Application Logs</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircleIcon className="w-6 h-6" />
          </button>
        </div>

        {/* Controls */}
        <div className="p-6 border-b bg-gray-50">
          <div className="flex flex-wrap gap-4 items-center">
            {/* Search */}
            <div className="flex-1 min-w-64">
              <div className="relative">
                <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search logs..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            {/* Level Filter */}
            <div className="flex items-center gap-2">
              <FunnelIcon className="w-5 h-5 text-gray-500" />
              <span className="text-sm font-medium text-gray-700">Levels:</span>
              {Object.values(LogLevel).filter(level => typeof level === 'number').map((level) => (
                <label key={level} className="flex items-center gap-1">
                  <input
                    type="checkbox"
                    checked={selectedLevels.includes(level as LogLevel)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedLevels([...selectedLevels, level as LogLevel]);
                      } else {
                        setSelectedLevels(selectedLevels.filter(l => l !== level));
                      }
                    }}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-700">{LogLevel[level as LogLevel]}</span>
                </label>
              ))}
            </div>

            {/* Component Filter */}
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-700">Components:</span>
              <select
                multiple
                value={selectedComponents}
                onChange={(e) => {
                  const values = Array.from(e.target.selectedOptions, option => option.value);
                  setSelectedComponents(values);
                }}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">All Components</option>
                {getUniqueComponents().map(component => (
                  <option key={component} value={component}>{component}</option>
                ))}
              </select>
            </div>

            {/* Auto-refresh */}
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={autoRefresh}
                onChange={(e) => setAutoRefresh(e.target.checked)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">Auto-refresh</span>
            </label>

            {/* Actions */}
            <div className="flex gap-2">
              <button
                onClick={loadLogs}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500"
              >
                Refresh
              </button>
              <button
                onClick={exportLogs}
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-500 flex items-center gap-2"
              >
                <DocumentArrowDownIcon className="w-4 h-4" />
                Export
              </button>
              <button
                onClick={clearLogs}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 focus:ring-2 focus:ring-red-500 flex items-center gap-2"
              >
                <TrashIcon className="w-4 h-4" />
                Clear
              </button>
            </div>
          </div>
        </div>

        {/* Logs Display */}
        <div className="flex-1 overflow-auto p-6">
          <div className="space-y-3">
            {filteredLogs.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                No logs found matching the current filters.
              </div>
            ) : (
              filteredLogs.map((log, index) => (
                <div
                  key={`${log.timestamp}-${index}`}
                  className={`border rounded-lg p-4 ${getLevelColor(log.level)} hover:shadow-md transition-shadow`}
                >
                  {/* Log Header */}
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-3">
                      {getLevelIcon(log.level)}
                      <span className="font-mono text-sm text-gray-600">
                        {new Date(log.timestamp).toLocaleTimeString()}
                      </span>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        log.level === LogLevel.ERROR || log.level === LogLevel.FATAL
                          ? 'bg-red-100 text-red-800'
                          : log.level === LogLevel.WARN
                          ? 'bg-yellow-100 text-yellow-800'
                          : log.level === LogLevel.INFO
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {LogLevel[log.level]}
                      </span>
                      {log.context.component && (
                        <span className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs">
                          {log.context.component}
                        </span>
                      )}
                    </div>
                    <button
                      onClick={() => toggleLogExpansion(`${log.timestamp}-${index}`)}
                      className="text-gray-500 hover:text-gray-700"
                    >
                      {expandedLogs.has(`${log.timestamp}-${index}`) ? '▼' : '▶'}
                    </button>
                  </div>

                  {/* Log Message */}
                  <div className="text-gray-900 mb-2">
                    {log.message}
                  </div>

                  {/* Expanded Details */}
                  {expandedLogs.has(`${log.timestamp}-${index}`) && (
                    <div className="mt-3 pt-3 border-t border-gray-200">
                      <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                          <h4 className="font-medium text-gray-700 mb-2">Context</h4>
                          <div className="space-y-1">
                            {Object.entries(log.context).map(([key, value]) => (
                              <div key={key} className="flex justify-between">
                                <span className="text-gray-600">{key}:</span>
                                <span className="text-gray-900 font-mono">{String(value)}</span>
                              </div>
                            ))}
                          </div>
                        </div>
                        <div>
                          <h4 className="font-medium text-gray-700 mb-2">Additional Info</h4>
                          <div className="space-y-1">
                            {log.error && (
                              <div className="text-red-600">
                                <strong>Error:</strong> {log.error.message}
                              </div>
                            )}
                            {log.data && (
                              <div>
                                <strong>Data:</strong>
                                <pre className="text-xs bg-gray-100 p-2 rounded mt-1 overflow-auto">
                                  {JSON.stringify(log.data, null, 2)}
                                </pre>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t bg-gray-50 text-sm text-gray-600">
          Showing {filteredLogs.length} of {logs.length} total logs
        </div>
      </div>
    </div>
  );
};

export default LoggingDashboard;
