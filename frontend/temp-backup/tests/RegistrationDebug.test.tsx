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

describe('Registration Debug Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('should debug form submission flow', async () => {
    // Mock a successful response
    const mockResponse = {
      data: {
        success: true,
        message: 'User registered successfully',
        data: {
          id: 'test-id',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          token: 'test-token'
        }
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {} as any
    };

    mockAuthAPI.register.mockResolvedValue(mockResponse);

    renderWithProviders(<Register />);
    
    // Check if form is rendered
    expect(screen.getByText(/Create your account/i)).toBeInTheDocument();
    
    // Fill in the form step by step
    const firstNameField = screen.getByLabelText(/First Name/i);
    const lastNameField = screen.getByLabelText(/Last Name/i);
    const emailField = screen.getByLabelText(/Email/i);
    const passwordField = screen.getByPlaceholderText(/Create a strong password/i);
    const confirmPasswordField = screen.getByPlaceholderText(/Confirm your password/i);
    const termsCheckbox = screen.getByRole('checkbox');
    
    // Fill form data
    fireEvent.change(firstNameField, { target: { value: 'Test' } });
    fireEvent.change(lastNameField, { target: { value: 'User' } });
    fireEvent.change(emailField, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordField, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });
    fireEvent.click(termsCheckbox);
    
    // Verify form data is set
    expect(firstNameField).toHaveValue('Test');
    expect(lastNameField).toHaveValue('User');
    expect(emailField).toHaveValue('test@example.com');
    expect(passwordField).toHaveValue('password123');
    expect(confirmPasswordField).toHaveValue('password123');
    expect(termsCheckbox).toBeChecked();
    
    // Submit the form
    const submitButton = screen.getByRole('button', { name: /Create account/i }) as HTMLButtonElement;
    console.log('Submit button found:', submitButton);
    console.log('Submit button disabled:', submitButton.disabled);
    
    fireEvent.click(submitButton);
    
    // Wait for API call
    await waitFor(() => {
      console.log('API register called:', mockAuthAPI.register.mock.calls);
      console.log('API register called times:', mockAuthAPI.register.mock.calls.length);
    }, { timeout: 5000 });
    
    // Check if API was called
    expect(mockAuthAPI.register).toHaveBeenCalledWith({
      firstName: 'Test',
      lastName: 'User',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123'
    });
  });

  test('should show validation error for missing terms agreement', async () => {
    renderWithProviders(<Register />);
    
    // Fill in the form but don't check terms
    const firstNameField = screen.getByLabelText(/First Name/i);
    const lastNameField = screen.getByLabelText(/Last Name/i);
    const emailField = screen.getByLabelText(/Email/i);
    const passwordField = screen.getByPlaceholderText(/Create a strong password/i);
    const confirmPasswordField = screen.getByPlaceholderText(/Confirm your password/i);
    
    fireEvent.change(firstNameField, { target: { value: 'Test' } });
    fireEvent.change(lastNameField, { target: { value: 'User' } });
    fireEvent.change(emailField, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordField, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });
    
    // Submit without agreeing to terms
    const submitButton = screen.getByRole('button', { name: /Create account/i });
    fireEvent.click(submitButton);
    
    // Should show terms error
    await waitFor(() => {
      expect(screen.getByText(/You must agree to the Terms of Service and Privacy Policy/i)).toBeInTheDocument();
    });
    
    // API should not be called
    expect(mockAuthAPI.register).not.toHaveBeenCalled();
  });

  test('should check environment configuration', () => {
    // Check environment variables
    console.log('REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
    console.log('Default API URL:', 'http://localhost:8085');
    
    const expectedBaseURL = process.env.REACT_APP_API_URL || 'http://localhost:8085';
    expect(expectedBaseURL).toBe('http://localhost:8085');
  });
});
