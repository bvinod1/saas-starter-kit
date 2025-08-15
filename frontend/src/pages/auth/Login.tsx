import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNotifications } from '../../contexts/NotificationContext';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const { login } = useAuth();
  const { showToast } = useNotifications();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/app/dashboard';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      showToast('error', 'Please fill in all fields');
      return;
    }

    try {
      setIsLoading(true);
      await login(email, password);
      showToast('success', 'Login successful!');
      navigate(from, { replace: true });
    } catch (error) {
      showToast('error', 'Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div 
      className="font-gilroy h-screen relative overflow-hidden auth-page" 
      style={{ 
        scrollbarWidth: 'none', 
        msOverflowStyle: 'none',
        WebkitOverflowScrolling: 'touch'
      }}
    >
      {/* Background with 3 brand colors */}
      <div className="absolute inset-0 bg-gradient-to-br from-green via-dark-red to-red"></div>
      
      {/* Subtle geometric accents */}
      
      
      {/* Content */}
      <div className="relative z-10 h-screen flex items-center justify-center p-6 overflow-hidden">
        <div className="w-full max-w-md">
          {/* Header Section */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-white/20 backdrop-blur-sm rounded-3xl shadow-2xl mb-6 transform hover:scale-105 transition-transform duration-300">
              <img 
                src="/mindsprint-logo.png" 
                alt="Mindsprint Logo" 
                className="w-14 h-14 object-contain"
              />
            </div>
            <h1 className="text-4xl font-bold text-white mb-3">Welcome Back</h1>
            <p className="text-white/80 text-lg">Sign in to your account</p>
          </div>
          
          {/* Form */}
          <form className="space-y-6" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <label htmlFor="email" className="block text-base font-semibold text-white">
                Email address
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-5 py-3 bg-white/20 backdrop-blur-sm border border-white/30 rounded-xl text-white text-base placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-transparent transition-all duration-300"
                placeholder="Enter your email"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="password" className="block text-base font-semibold text-white">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-5 py-3 bg-white/20 backdrop-blur-sm border border-white/30 rounded-xl text-white text-base placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-transparent transition-all duration-300"
                placeholder="Enter your password"
              />
            </div>

            <div className="flex items-center justify-end">
              <Link
                to="/auth/forgot-password"
                className="text-base font-medium text-white/80 hover:text-white transition-colors duration-300"
              >
                Forgot your password?
              </Link>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-6 bg-white/20 backdrop-blur-sm hover:bg-white/30 text-white text-lg font-bold rounded-xl shadow-2xl transform hover:scale-105 transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-white/50 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {isLoading ? 'Signing in...' : 'Sign in'}
            </button>

            <div className="text-center pt-4">
              <p className="text-white/80 text-base">
                Don't have an account?{' '}
                <Link
                  to="/auth/register"
                  className="font-bold text-white hover:text-white/80 transition-colors duration-300"
                >
                  Sign up
                </Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
