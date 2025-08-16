import React from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { 
  ArrowRightIcon, 
  ShieldCheckIcon,
  ChartBarIcon,
  RocketLaunchIcon,
  Bars3Icon,
  XMarkIcon,
  BuildingOfficeIcon,
  KeyIcon,
  CreditCardIcon,
  BellIcon,
  EyeIcon,
  PuzzlePieceIcon,
  CodeBracketIcon
} from '@heroicons/react/24/outline';

const LandingPage: React.FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false);

  const features = [
    {
      icon: <BuildingOfficeIcon className="w-8 h-8" />,
      title: "Tenant Management",
      description: "Tenant onboarding, metadata configuration, logical data isolation, and lifecycle management."
    },
    {
      icon: <KeyIcon className="w-8 h-8" />,
      title: "User Management & RBAC",
      description: "User registration, authentication, role-based access control, and multi-user collaboration."
    },
    {
      icon: <CreditCardIcon className="w-8 h-8" />,
      title: "Billing & Subscriptions",
      description: "Subscription plans, payment integrations, automated lifecycle management, and usage-based billing."
    },
    {
      icon: <ShieldCheckIcon className="w-8 h-8" />,
      title: "API Layer & Security",
      description: "Multi-tenant REST/GraphQL APIs, rate limiting, OAuth2, MFA, and comprehensive security features."
    },
    {
      icon: <BellIcon className="w-8 h-8" />,
      title: "Notification System",
      description: "Email/SMS templating, event-driven hooks, and third-party communication service integrations."
    },
    {
      icon: <EyeIcon className="w-8 h-8" />,
      title: "Monitoring & Compliance",
      description: "Multi-tenant logs, metrics, audit logging, and compliance reporting (GDPR, SOC2)."
    },
    {
      icon: <PuzzlePieceIcon className="w-8 h-8" />,
      title: "Configuration & Extensibility",
      description: "Dynamic feature flags, plugin system, webhooks, and tenant-specific customization."
    },
    {
      icon: <CodeBracketIcon className="w-8 h-8" />,
      title: "Developer Tools",
      description: "SDKs, sample applications, and comprehensive documentation for faster development."
    }
  ];



  return (
    <div className="min-h-screen bg-white font-gilroy landing-page">
      {/* Navigation */}
      <nav className="fixed top-0 w-full bg-white/95 backdrop-blur-sm border-b border-gray-100 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <div className="w-10 h-10 flex items-center justify-center">
                <img 
                  src="/mindsprint-logo.png" 
                  alt="Mindsprint Logo" 
                  className="w-8 h-8 object-contain"
                />
              </div>
              <span className="ml-3 text-xl font-bold text-green">Mindsprint SaaS Starter Kit</span>
            </div>
            <div className="hidden md:flex items-center space-x-8">
              <a href="#features" className="text-green hover:text-red transition-colors">Features</a>
              <Link to="/auth/login" className="text-green hover:text-red transition-colors">Sign in</Link>
              <Link to="/auth/register" className="bg-red text-white px-6 py-2 rounded-lg hover:bg-dark-red transition-colors">
                Sign Up
              </Link>
            </div>
            
            {/* Mobile menu button */}
            <div className="md:hidden">
              <button
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="text-green hover:text-red transition-colors"
              >
                {isMobileMenuOpen ? (
                  <XMarkIcon className="w-6 h-6" />
                ) : (
                  <Bars3Icon className="w-6 h-6" />
                )}
              </button>
            </div>
          </div>
          
          {/* Mobile menu */}
          {isMobileMenuOpen && (
            <div className="md:hidden bg-white border-t border-gray-100">
              <div className="px-2 pt-2 pb-3 space-y-1">
                <a href="#features" className="block px-3 py-2 text-green hover:text-red transition-colors">Features</a>
                <Link to="/auth/login" className="block px-3 py-2 text-green hover:text-red transition-colors">Sign in</Link>
                <Link to="/auth/register" className="block px-3 py-2 bg-red text-white rounded-lg hover:bg-dark-red transition-colors">Sign Up</Link>
              </div>
            </div>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-24 pb-16" style={{ backgroundColor: '#1B414A' }}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
            >
              <h1 className="text-5xl md:text-6xl font-bold text-white mb-6 leading-tight">
                Mindsprint
                <span className="text-red block">SaaS Starter Kit</span>
                <span className="text-white block">Enterprise Ready</span>
              </h1>
              <p className="text-xl text-gray-200 mb-8 max-w-3xl mx-auto">
                A comprehensive starter kit by Mindsprint with multi-tenant architecture, enterprise security, 
                and scalable infrastructure. Launch your SaaS platform in weeks, not months.
              </p>

            </motion.div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2 className="text-4xl font-bold text-green mb-4">
                Everything You Need to Build
              </h2>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                Our starter kit provides all the essential components for building 
                enterprise-grade SaaS applications with modern best practices.
              </p>
            </motion.div>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: index * 0.1 }}
                viewport={{ once: true }}
                className="text-center p-6 rounded-xl hover:shadow-lg transition-all duration-300 group"
              >
                <div className="w-16 h-16 bg-gradient-to-br from-red to-dark-red rounded-full flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300">
                  <div className="text-white">
                    {feature.icon}
                  </div>
                </div>
                <h3 className="text-xl font-semibold text-green mb-2">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2 className="text-4xl font-bold text-green mb-4">
                Why Choose Our SaaS Starter Kit?
              </h2>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                Built by enterprise developers, for enterprise developers. 
                Save months of development time with battle-tested solutions.
              </p>
            </motion.div>
          </div>
          
          <div className="grid md:grid-cols-3 gap-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              viewport={{ once: true }}
              className="text-center p-8 bg-white rounded-xl shadow-sm hover:shadow-lg transition-shadow duration-300"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-red to-dark-red rounded-full flex items-center justify-center mx-auto mb-6">
                <RocketLaunchIcon className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-semibold text-green mb-4">Fast Development</h3>
              <p className="text-gray-600">
                Get to market in weeks, not months. Our pre-built components handle the complex 
                infrastructure so you can focus on your core business logic.
              </p>
            </motion.div>
            
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              viewport={{ once: true }}
              className="text-center p-8 bg-white rounded-xl shadow-sm hover:shadow-lg transition-shadow duration-300"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-red to-dark-red rounded-full flex items-center justify-center mx-auto mb-6">
                <ShieldCheckIcon className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-semibold text-green mb-4">Enterprise Security</h3>
              <p className="text-gray-600">
                Built with security-first principles. Multi-tenant isolation, OAuth2, MFA, 
                and compliance-ready features out of the box.
              </p>
            </motion.div>
            
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              viewport={{ once: true }}
              className="text-center p-8 bg-white rounded-xl shadow-sm hover:shadow-lg transition-shadow duration-300"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-red to-dark-red rounded-full flex items-center justify-center mx-auto mb-6">
                <ChartBarIcon className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-semibold text-green mb-4">Scalable Architecture</h3>
              <p className="text-gray-600">
                Designed to grow with your business. Microservices architecture, 
                containerized deployment, and cloud-native design patterns.
              </p>
            </motion.div>
          </div>
        </div>
      </section>



      {/* Call to Action Section */}
      <section className="py-20 bg-gradient-to-r from-green to-dark-red">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true }}
          >
            <h2 className="text-4xl font-bold text-white mb-6">
              Ready to Build Your SaaS Platform?
            </h2>
            <p className="text-xl text-gray-200 mb-8 max-w-2xl mx-auto">
              Join thousands of developers who are building scalable SaaS applications 
              with our enterprise-ready starter kit.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link 
                to="/auth/register" 
                className="bg-red text-white px-8 py-4 rounded-lg text-lg font-semibold hover:bg-dark-red transition-colors inline-flex items-center"
              >
                Get Started Free
                <ArrowRightIcon className="ml-2 w-5 h-5" />
              </Link>
              <Link 
                to="/auth/login" 
                className="border-2 border-white text-white px-8 py-4 rounded-lg text-lg font-semibold hover:bg-white hover:text-green transition-colors"
              >
                Sign In
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-green text-white py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex items-center justify-center mb-4">
                            <div className="w-10 h-10 flex items-center justify-center">
                  <img 
                    src="/mindsprint-logo.png" 
                    alt="Mindsprint Logo" 
                    className="w-8 h-8 object-contain"
                  />
                </div>
            <span className="ml-3 text-xl font-bold">Mindsprint SaaS Starter Kit</span>
          </div>
          <p className="text-gray-300 mb-4">
            Mindsprint - Building the future of SaaS development, one platform at a time.
          </p>
          <div className="border-t border-gray-700 pt-4">
            <p>&copy; 2024 Mindsprint. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
