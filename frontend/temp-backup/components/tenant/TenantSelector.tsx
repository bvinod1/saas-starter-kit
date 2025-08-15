import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { userAPI } from '../../services/api';

interface Tenant {
  id: string;
  name: string;
  slug: string;
  domain?: string;
  status: string;
}

const TenantSelector: React.FC = () => {
  const { currentTenant, setCurrentTenant, user } = useAuth();
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    if (user) {
      loadUserTenants();
    }
  }, [user]);

  const loadUserTenants = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getUserTenants();
      // Type the response properly
      const responseData = response as any;
      setTenants(responseData.data?.data || responseData.data || []);
    } catch (error) {
      console.error('Failed to load tenants:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTenantChange = (tenantSlug: string) => {
    setCurrentTenant(tenantSlug);
    setIsOpen(false);
    // Optionally reload the page or update the application state
    window.location.reload();
  };

  const currentTenantData = tenants.find(t => t.slug === currentTenant);

  if (loading) {
    return (
      <div className="flex items-center space-x-2">
        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
        <span className="text-sm text-gray-600">Loading tenants...</span>
      </div>
    );
  }

  if (tenants.length === 0) {
    return null;
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
      >
        <span className="w-2 h-2 bg-green-400 rounded-full"></span>
        <span>{currentTenantData?.name || 'Select Tenant'}</span>
        <svg
          className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-56 bg-white rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
          <div className="py-1">
            {tenants.map((tenant) => (
              <button
                key={tenant.id}
                onClick={() => handleTenantChange(tenant.slug)}
                className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-100 ${
                  tenant.slug === currentTenant ? 'bg-blue-50 text-blue-700' : 'text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <span className="w-2 h-2 bg-green-400 rounded-full"></span>
                  <span>{tenant.name}</span>
                  {tenant.slug === currentTenant && (
                    <svg className="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  )}
                </div>
                {tenant.domain && (
                  <div className="text-xs text-gray-500 ml-4">{tenant.domain}</div>
                )}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default TenantSelector;
