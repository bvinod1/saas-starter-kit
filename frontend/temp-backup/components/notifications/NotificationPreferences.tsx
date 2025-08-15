import React, { useState } from 'react';
import { useNotifications, NotificationPreferences } from '../../contexts/NotificationContext';

const NotificationPreferencesComponent: React.FC = () => {
  const { state, updatePreferences } = useNotifications();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<Partial<NotificationPreferences>>({});

  // =============================================
  // HANDLERS
  // =============================================

  const handleEdit = () => {
    if (state.preferences) {
      setFormData(state.preferences);
      setIsEditing(true);
    }
  };

  const handleSave = async () => {
    try {
      await updatePreferences(formData);
      setIsEditing(false);
      setFormData({});
    } catch (error) {
      console.error('Failed to update preferences:', error);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setFormData({});
  };

  const handleToggleChannel = (channel: keyof Pick<NotificationPreferences, 'emailEnabled' | 'smsEnabled' | 'pushEnabled' | 'inAppEnabled' | 'webhookEnabled'>) => {
    setFormData(prev => ({
      ...prev,
      [channel]: !prev[channel]
    }));
  };

  const handleInputChange = (field: keyof NotificationPreferences, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  // =============================================
  // RENDER
  // =============================================

  if (!state.preferences) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="text-gray-500">Loading preferences...</div>
      </div>
    );
  }

  const preferences = isEditing ? formData : state.preferences;

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Notification Preferences</h2>
        {!isEditing && (
          <button
            onClick={handleEdit}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Edit Preferences
          </button>
        )}
      </div>

      {state.error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {state.error}
        </div>
      )}

      <div className="space-y-6">
        {/* Channel Preferences */}
        <div className="border-b pb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Notification Channels</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="email"
                checked={preferences.emailEnabled}
                onChange={() => handleToggleChannel('emailEnabled')}
                disabled={!isEditing}
                className="h-4 w-4 text-blue-600 rounded border-gray-300"
              />
              <label htmlFor="email" className="text-gray-700">Email Notifications</label>
            </div>
            
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="sms"
                checked={preferences.smsEnabled}
                onChange={() => handleToggleChannel('smsEnabled')}
                disabled={!isEditing}
                className="h-4 w-4 text-blue-600 rounded border-gray-300"
              />
              <label htmlFor="sms" className="text-gray-700">SMS Notifications</label>
            </div>
            
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="push"
                checked={preferences.pushEnabled}
                onChange={() => handleToggleChannel('pushEnabled')}
                disabled={!isEditing}
                className="h-4 w-4 text-blue-600 rounded border-gray-300"
              />
              <label htmlFor="push" className="text-gray-700">Push Notifications</label>
            </div>
            
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="inApp"
                checked={preferences.inAppEnabled}
                onChange={() => handleToggleChannel('inAppEnabled')}
                disabled={!isEditing}
                className="h-4 w-4 text-blue-600 rounded border-gray-300"
              />
              <label htmlFor="inApp" className="text-gray-700">In-App Notifications</label>
            </div>
            
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="webhook"
                checked={preferences.webhookEnabled}
                onChange={() => handleToggleChannel('webhookEnabled')}
                disabled={!isEditing}
                className="h-4 w-4 text-blue-600 rounded border-gray-300"
              />
              <label htmlFor="webhook" className="text-gray-700">Webhook Notifications</label>
            </div>
          </div>
        </div>

        {/* Language and Timezone */}
        <div className="border-b pb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Language & Timezone</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="language" className="block text-sm font-medium text-gray-700 mb-1">
                Preferred Language
              </label>
              <select
                id="language"
                value={preferences.preferredLanguage}
                onChange={(e) => handleInputChange('preferredLanguage', e.target.value)}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="en">English</option>
                <option value="es">Spanish</option>
                <option value="fr">French</option>
                <option value="de">German</option>
                <option value="it">Italian</option>
                <option value="pt">Portuguese</option>
                <option value="ru">Russian</option>
                <option value="zh">Chinese</option>
                <option value="ja">Japanese</option>
                <option value="ko">Korean</option>
              </select>
            </div>
            
            <div>
              <label htmlFor="timezone" className="block text-sm font-medium text-gray-700 mb-1">
                Timezone
              </label>
              <select
                id="timezone"
                value={preferences.timezone}
                onChange={(e) => handleInputChange('timezone', e.target.value)}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="UTC">UTC</option>
                <option value="America/New_York">Eastern Time</option>
                <option value="America/Chicago">Central Time</option>
                <option value="America/Denver">Mountain Time</option>
                <option value="America/Los_Angeles">Pacific Time</option>
                <option value="Europe/London">London</option>
                <option value="Europe/Paris">Paris</option>
                <option value="Europe/Berlin">Berlin</option>
                <option value="Asia/Tokyo">Tokyo</option>
                <option value="Asia/Shanghai">Shanghai</option>
              </select>
            </div>
          </div>
        </div>

        {/* Quiet Hours */}
        <div className="border-b pb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Quiet Hours</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="quietStart" className="block text-sm font-medium text-gray-700 mb-1">
                Start Time
              </label>
              <input
                type="time"
                id="quietStart"
                value={preferences.quietHoursStart || ''}
                onChange={(e) => handleInputChange('quietHoursStart', e.target.value)}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            
            <div>
              <label htmlFor="quietEnd" className="block text-sm font-medium text-gray-700 mb-1">
                End Time
              </label>
              <input
                type="time"
                id="quietEnd"
                value={preferences.quietHoursEnd || ''}
                onChange={(e) => handleInputChange('quietHoursEnd', e.target.value)}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          <p className="text-sm text-gray-600 mt-2">
            During quiet hours, only urgent notifications will be sent.
          </p>
        </div>

        {/* Digest Settings */}
        <div className="border-b pb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Digest Settings</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="digestFrequency" className="block text-sm font-medium text-gray-700 mb-1">
                Digest Frequency
              </label>
              <select
                id="digestFrequency"
                value={preferences.digestFrequency}
                onChange={(e) => handleInputChange('digestFrequency', e.target.value)}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="IMMEDIATE">Immediate</option>
                <option value="HOURLY">Hourly</option>
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="NEVER">Never</option>
              </select>
            </div>
            
            <div>
              <label htmlFor="maxPerDay" className="block text-sm font-medium text-gray-700 mb-1">
                Max Notifications per Day
              </label>
              <input
                type="number"
                id="maxPerDay"
                min="1"
                max="100"
                value={preferences.maxNotificationsPerDay}
                onChange={(e) => handleInputChange('maxNotificationsPerDay', parseInt(e.target.value))}
                disabled={!isEditing}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>

        {/* Urgent Notifications */}
        <div className="pb-6">
          <div className="flex items-center space-x-3">
            <input
              type="checkbox"
              id="urgentAlways"
              checked={preferences.urgentNotificationsAlways}
              onChange={(e) => handleInputChange('urgentNotificationsAlways', e.target.checked)}
              disabled={!isEditing}
              className="h-4 w-4 text-blue-600 rounded border-gray-300"
            />
            <label htmlFor="urgentAlways" className="text-gray-700">
              Always send urgent notifications (even during quiet hours)
            </label>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      {isEditing && (
        <div className="flex justify-end space-x-3 mt-6 pt-6 border-t">
          <button
            onClick={handleCancel}
            className="px-4 py-2 text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Save Changes
          </button>
        </div>
      )}
    </div>
  );
};

export default NotificationPreferencesComponent;
