const API_BASE_URL = '/api/auth';

class ApiService {
    
    // Helper to get JWT token
    getToken() {
        return localStorage.getItem('jwt_token');
    }

    // Helper to set JWT token
    setToken(token) {
        localStorage.setItem('jwt_token', token);
    }

    // Helper to remove JWT token
    removeToken() {
        localStorage.removeItem('jwt_token');
    }

    // Generic fetch wrapper
    async request(endpoint, method = 'GET', body = null) {
        const headers = {
            'Content-Type': 'application/json'
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            method,
            headers
        };

        if (body) {
            config.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            const data = await response.json();
            
            if (!response.ok) {
                let errorMsg = data.message || 'An error occurred';
                
                // Append validation errors if they exist
                if (data.error && typeof data.error === 'object') {
                    const validationErrors = Object.values(data.error).join(', ');
                    if (validationErrors) {
                        errorMsg += ': ' + validationErrors;
                    } else if (typeof data.error === 'string') {
                        errorMsg += ': ' + data.error;
                    }
                }
                
                throw new Error(errorMsg);
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // Register User
    async register(name, email, password) {
        return this.request('/register', 'POST', { name, email, password });
    }

    // Login User
    async login(email, password) {
        return this.request('/login', 'POST', { email, password });
    }

    // Request OTP (for Email/SMS 2FA)
    async requestOtp(email) {
        return this.request('/request-otp', 'POST', { email });
    }

    // Verify OTP
    async verifyOtp(email, otpCode) {
        return this.request('/verify-otp', 'POST', { email, otpCode });
    }

    // Get Current User Profile
    async getProfile() {
        return this.request('/me', 'GET');
    }

    // Enable TOTP (Get QR Code)
    async enableTotp() {
        return this.request('/enable-totp', 'POST');
    }

    // Verify TOTP Setup
    async verifyTotp(totpCode) {
        return this.request('/verify-totp', 'POST', { totpCode });
    }

    // Logout
    async logout() {
        try {
            await this.request('/logout', 'POST');
        } catch (e) {
            // Ignore error on logout
        } finally {
            this.removeToken();
        }
    }
}

const api = new ApiService();
