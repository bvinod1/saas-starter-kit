import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import Register from '../pages/auth/Register';
import { authAPI } from '../services/api';

// Mock the API service
jest.mock('../services/api', () => ({
  authAPI: {
    register: jest.fn(),
  },
}));

const mockAuthAPI = authAPI as jest.Mocked<typeof authAPI>;

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Registration Simple Test', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('should submit form successfully with valid data', async () => {
    // Mock successful API response
    const mockResponse = {
      data: {
        success: true,
        data: {
          token: 'mock-token',
          user: { id: 1, email: 'test@example.com' }
        }
      }
    };
    mockAuthAPI.register.mockResolvedValue(mockResponse as any);

    renderWithProviders(<Register />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText(/First name/i), {
      target: { value: 'Test' }
    });
    fireEvent.change(screen.getByLabelText(/Last name/i), {
      target: { value: 'User' }
    });
    fireEvent.change(screen.getByLabelText(/Email address/i), {
      target: { value: 'test@example.com' }
    });
    
    // Get password fields by their IDs
    const passwordFields = screen.getAllByDisplayValue('') as HTMLInputElement[];
    const passwordField = passwordFields[0]; // First empty field
    const confirmPasswordField = passwordFields[1]; // Second empty field
    
    fireEvent.change(passwordField, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });

    // Check terms agreement
    fireEvent.click(screen.getByLabelText(/I agree to the Terms of Service/i));

    // Submit the form
    const submitButton = screen.getByRole('button', { name: /Create account/i });
    fireEvent.click(submitButton);

    // Wait for API call
    await waitFor(() => {
      expect(mockAuthAPI.register).toHaveBeenCalledWith({
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        password: 'password123',
        confirmPassword: 'password123'
      });
    });
  });

  test('should show validation error for missing terms agreement', async () => {
    renderWithProviders(<Register />);

    // Fill in the form but don't check terms
    fireEvent.change(screen.getByLabelText(/First name/i), {
      target: { value: 'Test' }
    });
    fireEvent.change(screen.getByLabelText(/Last name/i), {
      target: { value: 'User' }
    });
    fireEvent.change(screen.getByLabelText(/Email address/i), {
      target: { value: 'test@example.com' }
    });
    
    // Get password fields by their IDs
    const passwordFields = screen.getAllByDisplayValue('') as HTMLInputElement[];
    const passwordField = passwordFields[0]; // First empty field
    const confirmPasswordField = passwordFields[1]; // Second empty field
    
    fireEvent.change(passwordField, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });

    // Submit without agreeing to terms
    const submitButton = screen.getByRole('button', { name: /Create account/i });
    fireEvent.click(submitButton);

    // Should show terms error
    expect(screen.getByText(/You must agree to the Terms of Service and Privacy Policy/i)).toBeInTheDocument();
  });

  test('should show loading state during submission', async () => {
    // Mock API that takes time to respond
    mockAuthAPI.register.mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve({ data: { success: true } } as any), 100))
    );

    renderWithProviders(<Register />);

    // Fill form and check terms
    fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
    fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
    
    // Get password fields by their IDs
    const passwordFields = screen.getAllByDisplayValue('') as HTMLInputElement[];
    const passwordField = passwordFields[0]; // First empty field
    const confirmPasswordField = passwordFields[1]; // Second empty field
    
    fireEvent.change(passwordField, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });
    
    fireEvent.click(screen.getByLabelText(/I agree to the Terms of Service/i));

    // Submit form
    const submitButton = screen.getByRole('button', { name: /Create account/i });
    fireEvent.click(submitButton);

    // Should show loading state
    expect(screen.getByText(/Creating account.../i)).toBeInTheDocument();
    expect(submitButton).toBeDisabled();
  });
});
