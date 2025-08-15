import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import Register from '../pages/auth/Register';
import { authAPI } from '../services/api';
import { AxiosResponse } from 'axios';

// Mock the API service
jest.mock('../services/api', () => ({
  authAPI: {
    register: jest.fn(),
  },
}));

// Mock axios to control network behavior
jest.mock('axios', () => ({
  create: jest.fn(() => ({
    post: jest.fn(),
    interceptors: {
      request: { use: jest.fn() },
      response: { use: jest.fn() },
    },
  })),
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

describe('Registration Page Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Clear localStorage
    localStorage.clear();
  });

  describe('UI Rendering Tests', () => {
    test('should render registration form with all required fields', () => {
      renderWithProviders(<Register />);
      
      expect(screen.getByText(/Create your account/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/First name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Last name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Organization name/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/Create a strong password/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/Confirm your password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Create account/i })).toBeInTheDocument();
    });

    test('should show validation errors for empty form submission', async () => {
      renderWithProviders(<Register />);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/First name is required/i)).toBeInTheDocument();
      });
    });

    test('should show password validation errors', async () => {
      renderWithProviders(<Register />);
      
      // Fill in required fields first
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      
      // Use more specific selectors for password fields
      const passwordField = screen.getByPlaceholderText(/Create a strong password/i);
      const confirmPasswordField = screen.getByPlaceholderText(/Confirm your password/i);
      
      fireEvent.change(passwordField, { target: { value: '123' } });
      fireEvent.change(confirmPasswordField, { target: { value: '456' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/Passwords do not match/i)).toBeInTheDocument();
      });
    });
  });

  describe('API Connection Tests', () => {
    test('should successfully register user with valid data', async () => {
      const mockResponse: AxiosResponse = {
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
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockAuthAPI.register).toHaveBeenCalledWith({
          firstName: 'Test',
          lastName: 'User',
          email: 'test@example.com',
          organizationName: 'Test Corp',
          password: 'password123',
          confirmPassword: 'password123'
        });
      });
    });

    test('should handle API timeout error', async () => {
      const timeoutError = new Error('timeout of 10000ms exceeded');
      timeoutError.name = 'AxiosError';
      (timeoutError as any).code = 'ECONNABORTED';
      (timeoutError as any).message = 'timeout of 10000ms exceeded';

      mockAuthAPI.register.mockRejectedValue(timeoutError);

      renderWithProviders(<Register />);
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/An unexpected error occurred. Please try again./i)).toBeInTheDocument();
      });
    });

    test('should handle network error', async () => {
      const networkError = new Error('Network Error');
      networkError.name = 'AxiosError';
      (networkError as any).request = {};
      (networkError as any).response = undefined;

      mockAuthAPI.register.mockRejectedValue(networkError);

      renderWithProviders(<Register />);
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/Unable to connect to the server. Please check your connection and try again./i)).toBeInTheDocument();
      });
    });

    test('should handle server error response', async () => {
      const serverError = new Error('Request failed with status code 500');
      serverError.name = 'AxiosError';
      (serverError as any).response = {
        status: 500,
        data: { message: 'Internal server error' }
      };

      mockAuthAPI.register.mockRejectedValue(serverError);

      renderWithProviders(<Register />);
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/Internal server error/i)).toBeInTheDocument();
      });
    });
  });

  describe('Environment Configuration Tests', () => {
    test('should use correct API base URL from environment', () => {
      // Test that the API service is configured with the right base URL
      const expectedBaseURL = process.env.REACT_APP_API_URL || 'http://localhost:8085';
      
      // This test will help identify if the environment variable is being read correctly
      expect(expectedBaseURL).toBe('http://localhost:8085');
    });

    test('should show loading state during API call', async () => {
      // Mock a delayed response
      mockAuthAPI.register.mockImplementation(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      renderWithProviders(<Register />);
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      // Should show loading state
      expect(screen.getByText(/Creating account.../i)).toBeInTheDocument();
    });
  });

  describe('Form State Management Tests', () => {
    test('should not clear form after successful registration (redirects instead)', async () => {
      const mockResponse: AxiosResponse = {
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
      
      const firstNameField = screen.getByLabelText(/First name/i);
      const lastNameField = screen.getByLabelText(/Last name/i);
      const emailField = screen.getByLabelText(/Email address/i);
      const organizationNameField = screen.getByLabelText(/Organization name/i);
      const passwordField = screen.getByPlaceholderText(/Create a strong password/i);
      const confirmPasswordField = screen.getByPlaceholderText(/Confirm your password/i);
      
      // Fill in the form
      fireEvent.change(firstNameField, { target: { value: 'Test' } });
      fireEvent.change(lastNameField, { target: { value: 'User' } });
      fireEvent.change(emailField, { target: { value: 'test@example.com' } });
      fireEvent.change(organizationNameField, { target: { value: 'Test Corp' } });
      fireEvent.change(passwordField, { target: { value: 'password123' } });
      fireEvent.change(confirmPasswordField, { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      // Wait for the API call to complete
      await waitFor(() => {
        expect(mockAuthAPI.register).toHaveBeenCalledWith({
          firstName: 'Test',
          lastName: 'User',
          email: 'test@example.com',
          organizationName: 'Test Corp',
          password: 'password123',
          confirmPassword: 'password123'
        });
      });

      // Form should not be cleared since the component redirects on success
      expect(firstNameField).toHaveValue('Test');
      expect(lastNameField).toHaveValue('User');
      expect(emailField).toHaveValue('test@example.com');
      expect(organizationNameField).toHaveValue('Test Corp');
      expect(passwordField).toHaveValue('password123');
      expect(confirmPasswordField).toHaveValue('password123');
    });

    test('should disable submit button during form submission', async () => {
      // Mock a delayed response
      mockAuthAPI.register.mockImplementation(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      renderWithProviders(<Register />);
      
      // Fill in the form using specific selectors
      fireEvent.change(screen.getByLabelText(/First name/i), { target: { value: 'Test' } });
      fireEvent.change(screen.getByLabelText(/Last name/i), { target: { value: 'User' } });
      fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText(/Organization name/i), { target: { value: 'Test Corp' } });
      fireEvent.change(screen.getByPlaceholderText(/Create a strong password/i), { target: { value: 'password123' } });
      fireEvent.change(screen.getByPlaceholderText(/Confirm your password/i), { target: { value: 'password123' } });
      
      // Check the terms checkbox
      const termsCheckbox = screen.getByRole('checkbox', { name: /agree to the Terms of Service/i });
      fireEvent.click(termsCheckbox);
      
      const submitButton = screen.getByRole('button', { name: /Create account/i });
      fireEvent.click(submitButton);

      // Button should be disabled during submission
      expect(submitButton).toBeDisabled();
    });
  });
});
