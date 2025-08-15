import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useNotifications } from '../../contexts/NotificationContext';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  
  const { showToast } = useNotifications();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      showToast('error', 'Please enter your email address');
      return;
    }

    try {
      setIsLoading(true);
      // TODO: Implement actual password reset API call
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API call
      setIsSubmitted(true);
      showToast('success', 'Password reset email sent! Please check your inbox.');
    } catch (error) {
      showToast('error', 'Failed to send password reset email. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isSubmitted) {
    return (
      <div className="font-gilroy min-h-screen relative overflow-hidden">
        {/* Background with 3 brand colors */}
        <div className="absolute inset-0 bg-gradient-to-br from-green via-dark-red to-red"></div>
        
        {/* Subtle geometric accents */}
        <div className="absolute inset-0 opacity-5">
          <div className="absolute top-20 left-20 w-32 h-32 border border-red transform rotate-45"></div>
          <div className="absolute bottom-20 right-20 w-24 h-24 border border-green transform -rotate-12"></div>
          <div className="absolute top-1/2 left-1/2 w-16 h-16 border border-red transform rotate-45"></div>
        </div>
        
        {/* Content */}
        <div className="relative z-10 min-h-screen flex items-center justify-center p-6">
          <div className="w-full max-w-md text-center">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-white/20 backdrop-blur-sm rounded-3xl shadow-2xl mb-8">
              <svg className="w-16 h-16 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h3 className="text-4xl font-bold text-white mb-6">Check your email</h3>
            <p className="text-white/80 text-xl mb-8">
              We've sent a password reset link to <strong className="text-white">{email}</strong>
            </p>
            <Link
              to="/auth/login"
              className="inline-flex items-center px-8 py-4 bg-white/20 backdrop-blur-sm hover:bg-white/30 text-white text-xl font-bold rounded-2xl shadow-2xl transform hover:scale-105 transition-all duration-300"
            >
              ← Back to sign in
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="font-gilroy min-h-screen relative overflow-hidden">
      {/* Background with 3 brand colors */}
      <div className="absolute inset-0 bg-gradient-to-br from-green via-dark-red to-red"></div>
      
      {/* Subtle geometric accents */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute top-20 left-20 w-32 h-32 border border-red transform rotate-45"></div>
        <div className="absolute bottom-20 right-20 w-24 h-24 border border-green transform -rotate-12"></div>
        <div className="absolute top-1/2 left-1/2 w-16 h-16 border border-red transform rotate-45"></div>
      </div>
      
      {/* Content */}
      <div className="relative z-10 min-h-screen flex items-center justify-center p-6">
        <div className="w-full max-w-md">
          {/* Header Section */}
          <div className="text-center mb-12">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-white/20 backdrop-blur-sm rounded-3xl shadow-2xl mb-8 transform hover:scale-105 transition-transform duration-300">
              <img 
                src="/mindsprint-logo.png" 
                alt="Mindsprint Logo" 
                className="w-16 h-16 object-contain"
              />
            </div>
            <h1 className="text-5xl font-bold text-white mb-4">Reset your password</h1>
            <p className="text-white/80 text-xl">
              Enter your email address and we'll send you a link to reset your password.
            </p>
          </div>
          
          {/* Form */}
          <form className="space-y-8" onSubmit={handleSubmit}>
            <div className="space-y-3">
              <label htmlFor="email" className="block text-lg font-semibold text-white">
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
                className="w-full px-6 py-4 bg-white/20 backdrop-blur-sm border border-white/30 rounded-2xl text-white text-lg placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-white/50 focus:border-transparent transition-all duration-300"
                placeholder="Enter your email"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-4 px-8 bg-white/20 backdrop-blur-sm hover:bg-white/30 text-white text-xl font-bold rounded-2xl shadow-2xl transform hover:scale-105 transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-white/50 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {isLoading ? 'Sending...' : 'Send reset link'}
            </button>

            <div className="text-center pt-6">
              <Link
                to="/auth/login"
                className="text-white/80 hover:text-white transition-colors duration-300 font-bold text-lg"
              >
                ← Back to sign in
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
