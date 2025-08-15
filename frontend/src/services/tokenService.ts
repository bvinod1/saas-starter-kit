import { authAPI } from './api';

export interface TokenData {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface StoredToken {
  token: string;
  refreshToken: string;
  expiresAt: number;
}

class TokenService {
  private readonly TOKEN_KEY = 'token';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private readonly TOKEN_EXPIRY_KEY = 'tokenExpiry';
  private readonly REFRESH_THRESHOLD = 5 * 60 * 1000; // 5 minutes in milliseconds

  /**
   * Store tokens in localStorage
   */
  storeTokens(tokenData: TokenData): void {
    const expiresAt = Date.now() + (tokenData.expiresIn * 1000);
    
    localStorage.setItem(this.TOKEN_KEY, tokenData.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, tokenData.refreshToken);
    localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiresAt.toString());
  }

  /**
   * Get stored access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get stored refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if token is expired
   */
  isTokenExpired(): boolean {
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiry) return true;
    
    const expiresAt = parseInt(expiry);
    return Date.now() >= expiresAt;
  }

  /**
   * Check if token needs refresh (within threshold)
   */
  shouldRefreshToken(): boolean {
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiry) return true;
    
    const expiresAt = parseInt(expiry);
    const timeUntilExpiry = expiresAt - Date.now();
    return timeUntilExpiry <= this.REFRESH_THRESHOLD;
  }

  /**
   * Get time until token expires in milliseconds
   */
  getTimeUntilExpiry(): number {
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiry) return 0;
    
    const expiresAt = parseInt(expiry);
    return Math.max(0, expiresAt - Date.now());
  }

  /**
   * Refresh access token
   */
  async refreshAccessToken(): Promise<string> {
    try {
      const refreshToken = this.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await authAPI.refreshToken();
      // Type the response properly
      const responseData = response as any;
      const { accessToken, refreshToken: newRefreshToken, expiresIn } = responseData.data;
      
      // Store new tokens
      this.storeTokens({
        accessToken,
        refreshToken: newRefreshToken || refreshToken,
        expiresIn,
        tokenType: 'Bearer'
      });

      return accessToken;
    } catch (error) {
      this.clearTokens();
      throw error;
    }
  }

  /**
   * Validate stored token
   */
  async validateStoredToken(): Promise<boolean> {
    try {
      const token = this.getAccessToken();
      if (!token) return false;

      const response = await authAPI.validate(token);
      // Type the response properly
      const responseData = response as any;
      return responseData.data.valid && !responseData.data.expired;
    } catch (error) {
      return false;
    }
  }

  /**
   * Get token info for debugging
   */
  getTokenInfo(): {
    hasToken: boolean;
    hasRefreshToken: boolean;
    isExpired: boolean;
    shouldRefresh: boolean;
    timeUntilExpiry: number;
    expiresAt: string | null;
  } {
    const token = this.getAccessToken();
    const refreshToken = this.getRefreshToken();
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    
    return {
      hasToken: !!token,
      hasRefreshToken: !!refreshToken,
      isExpired: this.isTokenExpired(),
      shouldRefresh: this.shouldRefreshToken(),
      timeUntilExpiry: this.getTimeUntilExpiry(),
      expiresAt: expiry ? new Date(parseInt(expiry)).toISOString() : null
    };
  }

  /**
   * Clear all stored tokens
   */
  clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
  }

  /**
   * Set up automatic token refresh
   */
  setupAutoRefresh(): () => void {
    const interval = setInterval(async () => {
      try {
        if (this.shouldRefreshToken() && this.getAccessToken()) {
          await this.refreshAccessToken();
          console.log('Token automatically refreshed');
        }
      } catch (error) {
        console.error('Auto token refresh failed:', error);
        this.clearTokens();
        // Redirect to login if auto-refresh fails
        window.location.href = '/login';
      }
    }, 60000); // Check every minute

    // Return cleanup function
    return () => clearInterval(interval);
  }
}

// Export singleton instance
export const tokenService = new TokenService();
export default tokenService;
